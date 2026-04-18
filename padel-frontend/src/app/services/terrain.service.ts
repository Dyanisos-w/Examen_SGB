import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TerrainDto {
  terrainId: number;
  nom: string;
  siteId: number | null;
}

@Injectable({ providedIn: 'root' })
export class TerrainService {
  private readonly http = inject(HttpClient);
  private readonly terrainsApiUrl = `${environment.apiBaseUrl}/terrains`;

  getTerrains(siteId?: number): Observable<TerrainDto[]> {
    const params = siteId != null
      ? new HttpParams().set('siteId', siteId.toString())
      : undefined;

    return this.http.get<TerrainDto[]>(this.terrainsApiUrl, { params });
  }
}

