describe('Login, collect tests & Play Videos', {
  testIsolation: false,
}, () => {

  const email = Cypress.env('maestroEmail');
  const projectUrl = Cypress.env('projectUrl');
  const recivoOrgId = Cypress.env('recivoOrgId');
  const recivoApiKey = Cypress.env('recivoApiKey');
  const stopwatchStart = Date.now();

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
        m.subject && m.subject.includes("Sign in to Maestro Cloud") &&
        m.createdAt && (now - new Date(m.createdAt).getTime() <= 10000)
      );
      if (freshEmail) {
        const match = freshEmail.text?.match(/\b\d{6}\b/);
        if (match) return match[0];
      }
      return waitForOtp(attempts + 1);
    });
  };

  it('Login, find tests, and play videos', {
    defaultCommandTimeout: 30000,
    pageLoadTimeout: 60000,
  }, () => {

    cy.writeFile('cypress/test_start_times.txt', `Video Timestamps\n----------------\n`);

    // --- Step 1: Login ---
    cy.visit('https://signin.maestro.dev');
    cy.get('input[name="email"]').type(email);
    cy.get('button[type="submit"]').click();

    cy.log('Polling Recivo API for OTP...');
    waitForOtp().then((otp) => {
      cy.log(`OTP Received: ${otp}`);
      cy.get('input[data-test="otp-input"]').first().type(otp, { delay: 100 });
    });

    // --- Step 2: Enter Maestro Dashboard ---
    cy.origin('https://app.maestro.dev', { args: { projectUrl, stopwatchStart } }, ({ projectUrl, stopwatchStart }) => {

       cy.visit(projectUrl);
       cy.get('body', { timeout: 30000 }).should('be.visible');

       // --- Step 3: Collect list of tests ---
       cy.log('Looking for test links...');
       cy.get('a[href*="/flow/run_"]').then(($links) => {
        const tests = [];
        $links.each((_, el) => {
          const name = el.querySelector('p')?.textContent.trim();
          const url = el.href;
          if (name && url) tests.push({ name, url });
        });

        cy.log(`Found ${tests.length} tests`);

        cy.wrap(tests).each((test, index) => {
          cy.then(() => {
              const timePassed = Date.now() - stopwatchStart;
              const minutes = Math.floor(timePassed / 60000);
              const seconds = Math.floor((timePassed % 60000) / 1000);
              const timestamp = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
              const logEntry = `Test ${index + 1} - ${test.name} timestamp ${timestamp}\n`;
              cy.writeFile('cypress/test_start_times.txt', logEntry, { flag: 'a+' });
              cy.log(`Starting test at: ${timestamp}`);
          });

          cy.visit(test.url);

          cy.get('body').then(($body) => {
            const $playBtn = $body.find('button[class*="play"], div[class*="play"], svg[data-icon="play"]');
            if ($playBtn.length > 0 && $playBtn.is(':visible')) {
              cy.wrap($playBtn).first().click({force: true});
            }
          });

          cy.get('video', { timeout: 60000 })
            .should('be.visible')
            .should(($video) => {
              expect($video[0].readyState).to.be.greaterThan(0);
            })
            .then(($video) => {
              const vid = $video[0];
              const durationSeconds = vid.duration;
              vid.muted = true;
              vid.currentTime = 0;
              vid.play().catch(() => {});

              const waitTimeMs = (durationSeconds * 1000) + 10000;
              cy.wait(waitTimeMs); // This wait is mandatory one to play the video complete
            });
        });
      });
    });
  });
});