import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface CreateAdminRequest {
  nom: string;
  prenom: string;
  password: string;
  accountType: 'LOCALADMIN';
  ville: string;
}

export interface RegisterResponse {
  matricule: string;
}

@Injectable({ providedIn: 'root' })
export class AdminUserManagementService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/api/admin/users`;

  createLocalAdmin(request: Omit<CreateAdminRequest, 'accountType'>) {
    return this.http.post<RegisterResponse>(`${this.baseUrl}/admins`, {
      ...request,
      accountType: 'LOCALADMIN'
    });
  }

  revokeLocalAdmin(matricule: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/admins/${matricule}`);
  }

  banPlayer(matricule: string): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/members/${matricule}/ban`, {});
  }

  unbanPlayer(matricule: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/members/${matricule}/ban`);
  }
}
