import api from '../lib/axios';
import type { AuthUser } from '../types/auth';

export async function fetchMe(): Promise<AuthUser> {
  return api.get('/auth/me');
}

export async function login(email: string, password: string): Promise<AuthUser> {
  return api.post('/auth/login', { email, password });
}

export async function signup(email: string, password: string, name: string): Promise<void> {
  return api.post('/auth/signup', { email, password, name });
}

export async function logout(): Promise<void> {
  return api.post('/auth/logout');
}
