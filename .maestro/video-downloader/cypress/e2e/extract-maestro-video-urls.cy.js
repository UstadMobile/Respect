describe('Login, collect tests & Save Video URLs', {}, () => {

  const email = Cypress.env('maestroEmail');
  const projectUrl = Cypress.env('projectUrl');
  const recivoOrgId = Cypress.env('recivoOrgId');
  const recivoApiKey = Cypress.env('recivoApiKey');
  // Logs via cy.task so it actually shows up in Jenkins/CI stdout.
  const log = (message) => cy.task('log', message, { log: false });

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
    defaultCommandTimeout: 60000,
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
      cy.url({ timeout: 60000 }).should('include', 'app.maestro.dev');
    });

    // --- Step 2: Collect tests + video URLs inside app.maestro.dev ---
    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {

      cy.get('body', { timeout: 60000 }).should('be.visible');
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

          if (url && typeof url === 'string' && url.includes('/flow/run_')) {
            tests.push({ name, url });
          } else {
            skipped.push(name);
          }
        });

        cy.task('log', `Found ${tests.length} usable test link(s). Skipped ${skipped.length}.`, { log: false });
        if (skipped.length) {
          cy.task('log', `Skipped (no usable href): ${JSON.stringify(skipped)}`, { log: false });
        }

        const getVideoSrcOrNull = (attempts = 0) => {
          return cy.document().then((doc) => {
            const videoEl = doc.querySelector('video');
            const src = videoEl ? videoEl.src : null;

            if (src) return src;
            if (attempts >= 10) return null;
            return cy.wait(1000, { log: false }).then(() => getVideoSrcOrNull(attempts + 1));
          });
        };

        // --- Step 4: Visit each test, capture video src + screenshot ---
        const results = [];

        cy.wrap(tests).each((test, index) => {
          if (!test || !test.url) {
            cy.task('log', `[${index + 1}/${tests.length}] Skipping "${test?.name || 'unknown'}" — no valid URL.`, { log: false });
            return; // move to next item in .each(), no throw
          }

          cy.task('log', `[${index + 1}/${tests.length}] Visiting: ${test.name}`, { log: false });

          // failOnStatusCode:false — a flaky/slow report page shouldn't
          // abort the whole extraction run either.
          cy.visit(test.url, { failOnStatusCode: false, timeout: 30000 });

          getVideoSrcOrNull().then((videoUrl) => {
            cy.screenshot(test.name, { capture: 'fullPage', timeout: 120000 });

            if (videoUrl) {
              results.push({ name: test.name, url: videoUrl });
              cy.task('log', `[${index + 1}/${tests.length}] Captured video for "${test.name}".`, { log: false });
            } else {
              cy.task('log', `[${index + 1}/${tests.length}] No video found for "${test.name}" after waiting — skipping.`, { log: false });
            }
          });
        }).then(() => {
          return results;
        });
      });

    }).then((results) => {
      // Back in the top-level context — safe to use cy.writeFile here.
      if (!results || results.length === 0) {
        cy.task('log', 'No video URLs were collected.', { log: false });
        return;
      }

      results.forEach((r) => {
        cy.writeFile(
          'cypress/downloads/video_urls.txt',
          `${r.name}: ${r.url}\n`,
          { flag: 'a+' }
        );
      });

      cy.task('log', `Wrote ${results.length} video URL(s) to cypress/downloads/video_urls.txt`, { log: false });
    });
  });
});