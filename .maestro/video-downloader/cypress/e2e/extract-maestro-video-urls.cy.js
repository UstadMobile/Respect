describe('Login, collect tests & Save Video URLs', {}, () => {

  const email = Cypress.env('maestroEmail');
  const projectUrl = Cypress.env('projectUrl');
  const recivoOrgId = Cypress.env('recivoOrgId');
  const recivoApiKey = Cypress.env('recivoApiKey');

    const waitForOtp = (attempts = 0) => {
      if (attempts > 60) throw new Error('OTP not received within 60 seconds');

      return cy.request({
        method: 'GET',
        url: `https://recivo.email/api/v1/organizations/${recivoOrgId}/inbox`,
        headers: { Authorization: `Bearer ${recivoApiKey}` },
        failOnStatusCode: false
      }).then((res) => {
        const now = Date.now();
        const emails = Array.isArray(res.body) ? res.body : [];

        const freshEmail = emails.find(m =>
          m.subject && m.subject.includes("Sign in to Maestro Cloud") &&
          m.createdAt && (now - new Date(m.createdAt).getTime() <= 15000)
        );

        if (freshEmail) {
          const match = freshEmail.text?.match(/\b\d{6}\b/);
          if (match) return match[0];
        }

        // --- PAUSE ADDED HERE ---
        cy.wait(1000); // Wait 1 second before trying again
        return waitForOtp(attempts + 1);
      });
    };

  it('Login and save video URLs to text file', {
    defaultCommandTimeout: 120000,
    pageLoadTimeout: 120000,
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
      cy.url({ timeout: 60000 }).should('include', 'app.maestro.dev');
    });

    // --- Step 2: Enter App Domain ---
    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {   //To use the projectUrl variable inside the cy.origin block we explicitly pass it in via the args object

      cy.get('body', { timeout: 60000 }).should('be.visible');
      cy.log('Navigating to Project URL...');
      cy.visit(projectUrl);

      // Verify we are on the correct page (Project view) before looking for links
      cy.url({ timeout: 60000 }).should('include', '/project/');

      // Wait for the list of runs to appear
      cy.get('a[href*="/flow/run_"]', { timeout: 60000 }).should('have.length.gt', 0);
      cy.screenshot('00_Main_Dashboard', { capture: 'fullPage', timeout: 120000 });
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

          // Extract URL
          cy.get('video', { timeout: 60000 })
            .should('have.prop', 'src')
            .then((videoUrl) => {
              if (videoUrl) {
                cy.writeFile('cypress/downloads/video_urls.txt', `${test.name}: ${videoUrl}\n`, { flag: 'a+' });
                cy.screenshot(test.name, { capture: 'fullPage', timeout: 120000 });
              }
            });
        });
      });
    });
  });
});