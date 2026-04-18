export interface TaskItem {
  title: string;
  priority: 'low' | 'medium' | 'high';
  dueLabel: string;
  done: boolean;
}
