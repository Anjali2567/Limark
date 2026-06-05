export function generateWorkspaceUser() {
  const timestamp = Date.now();

  return {
    name: 'Net User',
    email: `testuser${timestamp}@example.com`,
  };
}