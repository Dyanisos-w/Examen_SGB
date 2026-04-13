import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
 import { catchError, map, Observable, of } from 'rxjs';
import { Reservation } from '../models/reservation.model';
import { environment } from '../../environments/environment';

export interface CreateReservationRequest {
  siteId: number;
  terrainId: number;
  date: string;
  heureDebut: string;
  typeReservation: string;
}

export interface PlanningSlot {
  terrainId: number;
  siteId: number;
  date: string;       // "2024-04-15"
  heure: string;      // "08:00:00"
  disponible: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = `${environment.apiBaseUrl}/api/reservations`;
  private usersApiUrl = `${environment.apiBaseUrl}/utilisateurs`;
  private planningApiUrl = `${environment.apiBaseUrl}/api/planning`;

  constructor(private http: HttpClient) {}

  getMyReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/me`);
  }

  getReservationById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.apiUrl}/${id}`);
  }

  createReservation(reservation: Partial<Reservation>): Observable<Reservation> {
    return this.http.post<Reservation>(this.apiUrl, reservation);
  }

  createReservationRequest(payload: CreateReservationRequest): Observable<number> {
    return this.http.post<number>(this.apiUrl, payload);
  }

  getPlanning(userId: string, siteId: number, date: string): Observable<PlanningSlot[]> {
    const params = new HttpParams()
      .set('userId', userId)
      .set('siteId', siteId.toString())
      .set('date', date);

    return this.http.get<PlanningSlot[]>(this.planningApiUrl, {
      params
    });
  }

  validateMatriculeExists(matricule: string): Observable<boolean> {
    return this.http.get(`${this.usersApiUrl}/${encodeURIComponent(matricule)}`).pipe(
      map(() => true),
      catchError(() => of(false))
    );
  }

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.apiUrl}/${id}`, reservation);
  }

  deleteReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

