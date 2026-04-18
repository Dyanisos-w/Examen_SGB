import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

  createLocalAdmin(request: Omit<CreateAdminRequest, 'accountType'>) {
    return this.http.post<RegisterResponse>(`${environment.apiBaseUrl}/api/admin/users/admins`, {
      ...request,
      accountType: 'LOCALADMIN'
    });
  }
}

