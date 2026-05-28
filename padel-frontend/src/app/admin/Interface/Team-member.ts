export interface TeamMember {
  matricule: string;
  name: string;
  role: string;
  status: 'online' | 'busy' | 'offline';
  isBanned: boolean;
  avatarUrl?: string;
}
