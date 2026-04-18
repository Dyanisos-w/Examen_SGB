export interface TeamMember {
  name: string;
  role: string;
  status: 'online' | 'busy' | 'offline';
  avatarUrl?: string;
}
