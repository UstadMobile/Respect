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

      cy.wait(1000);
      return waitForOtp(attempts + 1);
    });
  };

  it('Login and save video URLs to text file', {
    defaultCommandTimeout: 60000,
    pageLoadTimeout: 60000,
  }, () => {

    // Clear file up front, from the top-level (non-origin) context.
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

    // --- Step 2: Collect tests + video URLs inside app.maestro.dev ---
    // IMPORTANT: cy.writeFile / cy.task / cy.readFile do NOT work inside
    // cy.origin() — it runs in an isolated browser context with no Node
    // bridge. So we gather everything into a plain array and `return` it,
    // then write the file from the outer (non-origin) context afterwards.
    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {

      cy.get('body', { timeout: 60000 }).should('be.visible');
      cy.log('Navigating to Project URL...');
      cy.visit(projectUrl);

      cy.url({ timeout: 20000 }).should('include', '/project/');

      cy.get('a[href*="/flow/run_"]', { timeout: 30000 }).should('have.length.gt', 0);
      cy.screenshot('00_Main_Dashboard', { capture: 'fullPage', timeout: 120000 });

      // --- Step 3: Collect list of tests, filtering out unusable links ---
      cy.get('a[href*="/flow/run_"]').then(($links) => {
        const tests = [];
        const skipped = [];

        $links.each((_, el) => {
          const name = (el.querySelector('p')?.textContent || el.innerText || 'Unknown Test')
            .trim()
            .replace(/\n/g, ' ');
          const url = el.href;

          // Guard against anchors with no real href (icon buttons, "#",
          // client-router placeholders) — this was the root cause of the
          // `cy.visit() failed trying to load: undefined` crash.
          if (url && typeof url === 'string' && url.includes('/flow/run_')) {
            tests.push({ name, url });
          } else {
            skipped.push(name);
          }
        });

        // console.log (not cy.log) is what actually shows up in Jenkins/CI
        // stdout, so use it for anything you need visible in build logs.
        console.log(`Found ${tests.length} usable test link(s).`);
        if (skipped.length) {
          console.log(`Skipped ${skipped.length} link(s) with no usable href:`, skipped);
        }

        // --- Step 4: Visit each test, capture video src + screenshot ---
        const results = [];

        cy.wrap(tests).each((test, index) => {
          console.log(`Processing ${index + 1}/${tests.length}: ${test.name} -> ${test.url}`);

          // Defensive assertion: fail fast with a clear message instead of
          // a cryptic "cy.visit(): undefined" error deep in the stack.
          expect(test.url, `url for "${test.name}"`).to.be.a('string').and.not.be.empty;

          cy.visit(test.url);

          cy.get('video', { timeout: 20000 })
            .should('have.prop', 'src')
            .then((videoUrl) => {
              cy.screenshot(test.name, { capture: 'fullPage', timeout: 120000 });

              if (videoUrl) {
                results.push({ name: test.name, url: videoUrl });
              } else {
                console.log(`No video src found for: ${test.name}`);
              }
            });
        }).then(() => {
          // Hand the collected results back out of cy.origin().
          return results;
        });
      });

    }).then((results) => {
      // Back in the top-level context — safe to use cy.writeFile here.
      if (!results || results.length === 0) {
        console.log('No video URLs were collected.');
        return;
      }

      results.forEach((r) => {
        cy.writeFile(
          'cypress/downloads/video_urls.txt',
          `${r.name}: ${r.url}\n`,
          { flag: 'a+' }
        );
      });

      console.log(`Wrote ${results.length} video URL(s) to cypress/downloads/video_urls.txt`);
    });
  });
});