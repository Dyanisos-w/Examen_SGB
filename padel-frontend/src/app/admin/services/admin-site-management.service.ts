import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface CreateSiteRequest {
  nom: string;
  adresse: string;
}

export interface SiteResponse {
  siteId: number;
  nom: string;
  adresse: string;
  nombreTerrains: number;
}

@Injectable({ providedIn: 'root' })
export class AdminSiteManagementService {
  private readonly http = inject(HttpClient);

  createSite(payload: CreateSiteRequest) {
    return this.http.post<SiteResponse>(`${environment.apiBaseUrl}/api/admin/sites`, payload);
  }
}

