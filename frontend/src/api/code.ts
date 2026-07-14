import api from '../lib/axios';
import type { CodeItem } from '../types/code';

export async function fetchTechTags(): Promise<CodeItem[]> {
  return api.get('/codes/tech-tags');
}

export async function fetchBusinessAreas(): Promise<CodeItem[]> {
  return api.get('/codes/business-areas');
}
