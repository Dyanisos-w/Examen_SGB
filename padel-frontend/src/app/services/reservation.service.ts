import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation } from '../models/reservation.model';

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = 'http://localhost:8080/api/reservations';

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

  updateReservation(id: number, reservation: Partial<Reservation>): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.apiUrl}/${id}`, reservation);
  }

  deleteReservation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}

