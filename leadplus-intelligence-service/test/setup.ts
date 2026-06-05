import { beforeAll, afterAll } from 'vitest';
import { connectToDatabase, disconnectFromDatabase } from '../src/db/connection';

beforeAll(async () => {
  // Connect to test database
  await connectToDatabase();
});

afterAll(async () => {
  // Disconnect from test database
  await disconnectFromDatabase();
});
