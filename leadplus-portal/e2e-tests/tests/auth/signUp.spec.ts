import { test, expect } from '../../fixtures';
import { signUpData } from '../../test-data/signup.data';

test.describe('Sign Up flow', { tag: '@auth' }, () => {

  // EMPTY FIELDS
  test('Empty fields validation', async ({ signUpPage }) => {

    await signUpPage.goto();
    await signUpPage.isLoaded();

    await signUpPage.expectEmptyFieldErrors();
  });

  // INVALID EMAIL
  test('Invalid email validation', async ({ signUpPage }) => {

    await signUpPage.goto();
    await signUpPage.isLoaded();

    await signUpPage.expectInvalidEmail(
      signUpData.invalidUser.invalidEmail
    );
  });

  // WEAK PASSWORD
  test('Weak password validation', async ({ signUpPage }) => {

    await signUpPage.goto();
    await signUpPage.isLoaded();

    await signUpPage.expectWeakPassword(
      signUpData.invalidUser.weakPassword
    );
  });

  // SUCCESS
  test('Successful signup', async ({ signUpPage }) => {

    const uniqueUser = {
      ...signUpData.validUser,
      email: signUpData.validUser.email(Date.now()),
    };

    await signUpPage.goto();
    await signUpPage.isLoaded();

    await signUpPage.signUp(uniqueUser);

    await expect(signUpPage.page).not.toHaveURL(/signup/);
  });

});