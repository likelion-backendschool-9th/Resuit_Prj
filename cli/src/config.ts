import { homedir } from 'node:os';
import { join } from 'node:path';

export const API_BASE_URL = (process.env.RESUIT_API_URL ?? 'http://localhost:8080').replace(/\/+$/, '');
export const SESSION_FILE = join(homedir(), '.config', 'resuit', 'session.json');
