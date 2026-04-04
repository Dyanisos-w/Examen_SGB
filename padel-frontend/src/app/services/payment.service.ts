import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/reservations';

  constructor(private http: HttpClient) {}

  payReservation(reservationId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${reservationId}/pay`, {});
  }
}
