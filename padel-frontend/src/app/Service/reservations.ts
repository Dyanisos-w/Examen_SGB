import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {MyReservation} from '../Interface/my-reservation';

@Injectable({
  providedIn: 'root',
})
export class Reservations {
  private apiUrl = 'http://localhost:8080/api/reservations';

  constructor(private http: HttpClient) {}

  getMyReservations(): Observable<MyReservation[]> {
    return this.http.get<MyReservation[]>(`${this.apiUrl}/me`);
  }

  payReservation(reservationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${reservationId}/pay`, {});
  }
}
