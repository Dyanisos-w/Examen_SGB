import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Reservations } from '../Service/reservations';
import { MyReservation } from '../Interface/my-reservation';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './my-reservation.html',
  styleUrls: ['./my-reservation.css']
})
export class MyReservationComponent implements OnInit, OnDestroy {
  reservations: MyReservation[] = [];
  loading = true;
  expandedId: number | null = null;
  infoMessage = '';
  private destroy$ = new Subject<void>();

  constructor(
    private service: Reservations,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = (navigation?.extras.state || history.state) as {
      paymentReminder?: boolean;
      createdReservationId?: number;
    };

    console.log('[MyReservation] ngOnInit state', state);

    if (state?.paymentReminder) {
      const suffix = state.createdReservationId ? ` (id: ${state.createdReservationId})` : '';
      this.infoMessage = `Reservation creee${suffix}. Vous pouvez maintenant payer dans cette liste.`;
      console.log('[MyReservation] paymentReminder actif', { createdReservationId: state.createdReservationId });
    }

    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    console.log('[MyReservation] load() → GET /api/reservations/me');
    this.service.getMyReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          console.log('[MyReservation] GET /api/reservations/me OK', { count: data.length, data });
          this.reservations = data;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[MyReservation] GET /api/reservations/me ERR', {
            status: err.status,
            url: err.url,
            message: err.message,
            body: err.error
          });
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  toggle(id: number): void {
    this.expandedId = this.expandedId === id ? null : id;
    this.cdr.detectChanges();
  }

  pay(id: number): void {
    console.log('[MyReservation] pay()', { reservationId: id });
    this.service.payReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('[MyReservation] pay() OK', { reservationId: id });
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[MyReservation] pay() ERR', { reservationId: id, status: err.status, body: err.error });
        }
      });
  }

  cancel(id: number): void {
    if (!confirm('Annuler cette réservation ?')) return;
    console.log('[MyReservation] cancel()', { reservationId: id });
    this.service.cancelReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('[MyReservation] cancel() OK', { reservationId: id });
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[MyReservation] cancel() ERR', { reservationId: id, status: err.status, body: err.error });
        }
      });
  }

  leave(id: number): void {
    if (!confirm('Quitter cette réservation ?')) return;
    console.log('[MyReservation] leave()', { reservationId: id });
    this.service.leaveReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('[MyReservation] leave() OK', { reservationId: id });
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[MyReservation] leave() ERR', { reservationId: id, status: err.status, body: err.error });
        }
      });
  }

  hasToPay(r: MyReservation): boolean {
    return r.participants.some(p => p.isMe && p.paymentStatus === 'A_PAYER');
  }

  statusClass(statut: string): string {
    switch (statut?.toUpperCase()) {
      case 'OPEN':      return 'bg-green-900 text-green-300';
      case 'FULL':      return 'bg-orange-900 text-orange-300';
      case 'PRIVATE':   return 'bg-purple-900 text-purple-300';
      case 'CANCELLED': return 'bg-red-900 text-red-300';
      default:          return 'bg-gray-700 text-gray-300';
    }
  }

  trackById(_: number, r: MyReservation): number { return r.reservationId; }
}
