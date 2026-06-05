export const signUpData = {
  validUser: {
    fullName: 'David Smith',
    email: (id: number) => `david${id}@playwright.com`,
    password: 'Test@1234',
    company: 'Test Company',
    phone: '+1 (555) 000-0000',
  },

  invalidUser: {
    invalidEmail: {
      fullName: 'John Doe',
      email: 'invalid@emailcom',
      password: 'Test@1234',
      company: 'Test Company',
      phone: '+1 (555) 000-0000',
    },

    weakPassword: {
      fullName: 'John Doe',
      email: 'john@test.com',
      password: '1234hjhuhj5',
      company: 'Test Company',
      phone: '+1 (555) 000-0000',
    },
  },
};