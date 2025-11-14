/// <reference types="cypress" />

describe('Collect Maestro test names', () => {
  const email = 'pooja@ustadmobile.com';
 const projectUrl = Cypress.env('projectUrl'); // pass via CLI or Jenkins

  it('Login and collect test names', () => {
    cy.visit('https://signin.maestro.dev');
    cy.get('input[name="email"]').type(email);
    cy.get('button[type="submit"]').click();

    cy.origin('https://app.maestro.dev', { args: { projectUrl } }, ({ projectUrl }) => {
    cy.wait(30000);
    cy.visit(projectUrl);
    cy.wait(5000);

      cy.get('a[href*="/flow/run_"]').then(($links) => {
        const testNames = [];
        $links.each((_, el) => {
          const name = el.querySelector('p')?.textContent.trim();
          if (name) testNames.push(name);
        });

        cy.log(`Found ${testNames.length} tests`);
        cy.task('saveFile', {
          filePath: 'cypress/downloads/testnames.txt',
          content: testNames.join('\n'),
        });
      });
    });
  });
});
