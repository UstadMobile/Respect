/// <reference types="cypress" />

describe('Download Maestro test videos using test list', () => {
  const email = 'pooja@ustadmobile.com';
  const projectUrl = Cypress.env('projectUrl'); // pass via CLI or Jenkins

  it('Login and download videos', () => {
    // Step 1: Login
    cy.visit('https://signin.maestro.dev');
    cy.get('input[name="email"]').type(email);
    cy.get('button[type="submit"]').click();

    // Step 2: Read test names via cy.task
    cy.task('readFile', 'cypress/downloads/testnames.txt').then((content) => {
      const testNames = content.split('\n').filter(Boolean);

        cy.origin('https://app.maestro.dev', { args: { projectUrl, testNames } }, ({ projectUrl, testNames }) => {
        cy.wait(30000);
        cy.visit(projectUrl);
        cy.wait(5000);

        testNames.forEach((name) => {
          cy.contains(name).click();
          cy.wait(5000);
          cy.get('video')
            .invoke('attr', 'src')
            .then((videoUrl) => {
              if (videoUrl) {
                cy.task('downloadVideo', { url: videoUrl, fileName: name });
                cy.log(`Downloaded: ${name}`);
              } else {
                cy.log(`No video found for ${name}`);
              }
            });
          cy.go('back');
          cy.wait(4000);
        });
      });
    });
  });
})
