export const credentials = {
  vendor: {
    username: process.env.E2E_VENDOR_USERNAME!,
    password: process.env.E2E_VENDOR_PASSWORD!,
    name: process.env.E2E_VENDOR_NAME!,
  },

  invalidVendor: {
    username: 'wrong@test.com',
    password: 'wrongpa@Qw2ss',
  },
};