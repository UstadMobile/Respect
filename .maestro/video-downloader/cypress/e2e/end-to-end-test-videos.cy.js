describe('Login, collect tests & Save Video URLs', {
  testIsolation: false,
}, () => {

  const email = Cypress.env('maestroEmail');
  const projectUrl = Cypress.env('projectUrl');
  const recivoOrgId = Cypress.env('recivoOrgId');
  const recivoApiKey = Cypress.env('recivoApiKey');

  const waitForOtp = (attempts = 0) => {
    if (attempts > 60) throw new Error('OTP not received within 30 seconds');
    return cy.request({
      method: 'GET',
      url: `https://recivo.email/api/v1/organizations/${recivoOrgId}/inbox`,
      headers: { Authorization: `Bearer ${recivoApiKey}` },
      failOnStatusCode: false
    }).then((res) => {
      const now = Date.now();
      const emails = Array.isArray(res.body) ? res.body : [];
      const freshEmail = emails.find(m =>
        m.subject?.includes("Sign in to Maestro Cloud") &&
        m.createdAt && (now - new Date(m.createdAt).getTime() <= 15000)
      );
      const match = freshEmail?.text?.match(/\b\d{6}\b/);
      return match ? match[0] : waitForOtp(attempts + 1);
    });
  };

  it('Login and save video URLs to text file', {
    defaultCommandTimeout: 30000,
    pageLoadTimeout: 60000,
  }, () => {

    // Clear file
    cy.writeFile('cypress/downloads/video_urls.txt', '');

    // --- Step 1: Login ---
    cy.visit('https://signin.maestro.dev');
    cy.get('input[name="email"]').type(email);
    cy.get('button[type="submit"]').click();

    waitForOtp().then((otp) => {
      cy.get('input[data-test="otp-input"]').first().type(otp, { delay: 50 });

      // Wait for domain change to app.maestro.dev
      cy.url({ timeout: 30000 }).should('include', 'app.maestro.dev');
    });

    // --- Step 2: Enter App Domain ---
    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {

      // FIX: Wait for the default dashboard to fully load/settle first.
      // If we visit too fast, the app's post-login redirect overrides our visit.
      cy.get('body', { timeout: 30000 }).should('be.visible');
      //cy.wait(3000); // Give app time to finish internal routing

      // Now visit the specific project URL
      cy.log('Navigating to Project URL...');
      cy.visit(projectUrl);

      // Verify we are on the correct page (Project view) before looking for links
      cy.url({ timeout: 20000 }).should('include', '/project/');

      // Wait for the list of runs to appear
      cy.get('a[href*="/flow/run_"]', { timeout: 30000 }).should('have.length.gt', 0);

      // --- Step 3: Collect list of tests ---
      cy.get('a[href*="/flow/run_"]').then(($links) => {
        const tests = [];
        $links.each((_, el) => {
          const name = el.querySelector('p')?.textContent || el.innerText || 'Unknown Test';
          tests.push({
            name: name.trim().replace(/\n/g, ' '),
            url: el.href
          });
        });

        cy.log(`Found ${tests.length} tests`);

        // --- Step 4: Iterate and Save ---
        cy.wrap(tests).each((test, index) => {
          cy.log(`Processing ${index + 1}: ${test.name}`);
          cy.visit(test.url);

          // Handle video overlay/play button
          cy.get('body').then(($body) => {
            const $playBtn = $body.find('button[class*="play"], svg[data-icon="play"]');
            if ($playBtn.length && $playBtn.is(':visible')) {
              cy.wrap($playBtn).first().click({ force: true });
            } else {
              // Fallback click center screen
              cy.get('body').click('center', { force: true });
            }
          });

          // Extract URL
          cy.get('video', { timeout: 20000 })
            .should('have.prop', 'src')
            .then((videoUrl) => {
              if (videoUrl) {
                cy.writeFile('cypress/downloads/video_urls.txt', `${test.name}: ${videoUrl}\n`, { flag: 'a+' });
              }
            });
        });
      });
    });
  });
});