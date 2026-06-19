export interface AuthUser {
  id: number;
  email: string;
  name: string;
  role: 'USER' | 'ADMIN';
}
