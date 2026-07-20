export function formatBudget(budget: number): string {
  if (budget >= 100_000_000) return `${(budget / 100_000_000).toFixed(1)}억`;
  if (budget >= 10_000) return `${(budget / 10_000).toFixed(0)}만`;
  return `${budget.toLocaleString()}원`;
}

export function formatDate(iso: string): string {
  return iso.slice(0, 10);
}

export function formatDateTime(iso: string): string {
  return iso.slice(0, 16).replace('T', ' ');
}
