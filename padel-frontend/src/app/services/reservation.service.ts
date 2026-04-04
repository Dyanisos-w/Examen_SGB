import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
 import { catchError, map, Observable, of } from 'rxjs';
import { Reservation } from '../models/reservation.model';

export interface CreateReservationRequest {
  siteId: number;
  terrainId: number;
  date: string;
  heureDebut: string;
  typeReservation: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8080/api/reservations';
  private usersApiUrl = 'http://localhost:8080/utilisateurs';

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

