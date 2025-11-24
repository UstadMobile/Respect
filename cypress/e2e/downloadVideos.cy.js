describe('Login, collect tests & download videos', () => {

  const email = 'respecttester@recivo.email';
  const projectUrl = Cypress.env('projectUrl');

  it('Login, collect test names, download videos', {
    defaultCommandTimeout: 90000,
    execTimeout: 180000,
    taskTimeout: 600000
  }, () => {

    //
    // --- Step 1: Login ---
    //
    cy.visit('https://signin.maestro.dev');
    cy.get('input[name="email"]').type(email);
    cy.get('button[type="submit"]').click();

    // Get *newest* OTP — retry until latest arrives
    cy.task('getMaestroOtp', { retry: true }).then((otp) => {
      expect(otp, 'OTP should not be null').to.not.be.null;
      cy.get('input[data-test="otp-input"]').first().type(otp, { delay: 100 });
    });

    //
    // --- Step 2: Enter Maestro Dashboard ---
    //
    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {

      cy.wait(6000);
      cy.log('Entering project…');
      cy.visit(projectUrl);

      // keep Cypress alive
      cy.wait(2000);
      cy.log('Initial page loaded');

      //
      // --- Step 3: Collect list of tests ---
      //
      cy.get('a[href*="/flow/run_"]').then(($links) => {
        const tests = [];

        $links.each((_, el) => {
          const name = el.querySelector('p')?.textContent.trim();
          const url = el.href;
          if (name && url) tests.push({ name, url });
        });

        cy.log(`Found ${tests.length} tests`);

        cy.task('saveFile', {
          filePath: 'cypress/downloads/testnames.txt',
          content: tests.map(t => t.name).join('\n')
        });

        //
        // --- Step 4: Download Each Test Video ---
        //
        cy.wrap(tests).each((test, index) => {

          // keep-alive ping every iteration
          cy.wait(2000);
          cy.log(`Processing test ${index + 1}/${tests.length}: ${test.name}`);

          cy.visit(test.url);

          cy.wait(4000);
          cy.log(`Opened test page: ${test.name}`);

          // get video url
          cy.get('video', { timeout: 20000 })
            .invoke('attr', 'src')
            .then((videoUrl) => {

              if (!videoUrl) {
                cy.log(`No video found for ${test.name}`);
                return;
              }

              cy.log(`Downloading video for: ${test.name}`);

              cy.task('downloadVideo', {
                url: videoUrl,
                fileName: test.name
              });

              // keep Cypress alive
              cy.wait(2000);
              cy.log('Download task triggered');
            });

        });

      });

    });

  });

});
