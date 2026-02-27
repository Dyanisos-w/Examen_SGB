import { Component, OnInit, OnDestroy } from '@angular/core';
import { Reservations } from '../Service/reservations';
import { MyReservation, ParticipantPayment } from '../Interface/my-reservation';
import { CommonModule } from '@angular/common';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-my-reservation',
  imports: [
    CommonModule,
    MatExpansionModule,
    MatChipsModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './my-reservation.html',
  standalone: true,
  styleUrls: ['./my-reservation.css']
})
export class MyReservationComponent implements OnInit, OnDestroy {
  reservations: MyReservation[] = [];
  loading = true;
  private destroy$ = new Subject<void>();

  constructor(private reservationService: Reservations) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadReservations(): void {
    this.reservationService.getMyReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: MyReservation[]) => {
          this.reservations = data;
          this.loading = false;
        },
        error: (error: any) => {
          console.error('Erreur lors du chargement des réservations:', error);
          this.loading = false;
        }
      });
  }

  pay(reservationId: number): void {
    this.reservationService.payReservation(reservationId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.loadReservations();
        },
        error: (error: any) => {
          console.error('Erreur lors du paiement:', error);
        }
      });
  }

  hasToPay(reservation: MyReservation): boolean {
    return reservation.participants.some(
      (p: ParticipantPayment) => p.isMe && p.paymentStatus === 'A_PAYER'
    );
  }

  trackByReservationId(index: number, reservation: MyReservation): number {
    return reservation.reservationId;
  }

  trackByParticipantId(index: number, participant: ParticipantPayment): string {
    return participant.matricule;
  }
}
