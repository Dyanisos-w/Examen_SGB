import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SiteDto {
  siteId: number;
  nom: string;
}

@Injectable({ providedIn: 'root' })
export class SiteService {
  private readonly http = inject(HttpClient);

  getSites() {
    return this.http.get<Array<{ siteId: number; nom: string }>>(`${environment.apiBaseUrl}/sites`).pipe(
      map((sites) => sites
        .map((site) => ({ siteId: site.siteId, nom: site.nom }))
        .sort((a, b) => a.nom.localeCompare(b.nom)))
    );
  }
}

