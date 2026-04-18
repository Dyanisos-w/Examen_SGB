import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface CreateTerrainRequest {
  nom: string;
  siteId?: number;
}

export interface TerrainResponse {
  terrainId: number;
  nom: string;
  site: {
    siteId: number;
    nom: string;
  };
}

@Injectable({ providedIn: 'root' })
export class AdminTerrainManagementService {
  private readonly http = inject(HttpClient);

  createTerrain(payload: CreateTerrainRequest) {
    return this.http.post<TerrainResponse>(`${environment.apiBaseUrl}/api/admin/terrains`, payload);
  }
}

