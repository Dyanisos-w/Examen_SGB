describe('Login page', () => {
  beforeEach(() => {
    cy.visit('/login');
  });

  it('affiche le formulaire de connexion', () => {
    cy.contains('Sign in to your account').should('be.visible');
    cy.get('input[formControlName="matricule"]').should('exist');
    cy.get('input[formControlName="password"]').should('exist');
    cy.get('button[type="submit"]').should('be.visible');
  });

  it('le bouton est désactivé si le formulaire est vide', () => {
    cy.get('button[type="submit"]').should('be.disabled');
  });

  it('le bouton est actif avec des données valides', () => {
    cy.get('input[formControlName="matricule"]').type('L12345');
    cy.get('input[formControlName="password"]').type('motdepasse');
    cy.get('button[type="submit"]').should('not.be.disabled');
  });
});
