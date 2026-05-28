import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface OpeningHoursDayDto {
  dayOfWeek: string;
  openingTime: string | null;
  closingTime: string | null;
  closed: boolean;
}

export interface OpeningHoursResponseDto {
  siteId: number;
  siteName: string;
  configured: boolean;
  days: OpeningHoursDayDto[];
}

@Injectable({ providedIn: 'root' })
export class AdminOpeningHoursService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/opening-hours`;

  getOpeningHours(siteId: number): Observable<OpeningHoursResponseDto> {
    return this.http.get<OpeningHoursResponseDto>(this.base, { params: { siteId } });
  }

  updateOpeningHours(siteId: number, body: { days: OpeningHoursDayDto[] }): Observable<OpeningHoursResponseDto> {
    return this.http.put<OpeningHoursResponseDto>(this.base, body, { params: { siteId } });
  }

}

export interface SiteClosureDto {
  id: number;
  startDate: string;
  endDate: string;
  reason: string | null;
  global: boolean;
}

export interface SiteClosureCreateDto {
  siteId: number | null;
  applyToAll: boolean;
  startDate: string;
  endDate: string;
  reason: string | null;
}


@Injectable({ providedIn: 'root' })
export class AdminClosureService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/api/admin/closures`;

  getGlobalClosures(): Observable<SiteClosureDto[]> {
    return this.http.get<SiteClosureDto[]>(`${this.base}/global`);
  }

  getSiteClosures(siteId?: number): Observable<SiteClosureDto[]> {
    let params = new HttpParams();
    if (siteId != null) {
      params = params.set('siteId', String(siteId));
    }
    return this.http.get<SiteClosureDto[]>(`${this.base}/site`, { params });
  }

  createClosure(body: SiteClosureCreateDto): Observable<void> {
    return this.http.post<void>(this.base, body);
  }

  deleteClosure(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
