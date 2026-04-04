import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Reservations } from '../Service/reservations';
import { MyReservation } from '../Interface/my-reservation';
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
    private router: Router
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = (navigation?.extras.state || history.state) as {
      paymentReminder?: boolean;
      createdReservationId?: number;
    };

    if (state?.paymentReminder) {
      const suffix = state.createdReservationId ? ` (id: ${state.createdReservationId})` : '';
      this.infoMessage = `Reservation creee${suffix}. Vous pouvez maintenant payer dans cette liste.`;
    }

    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    this.service.getMyReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => { this.reservations = data; this.loading = false; },
        error: ()    => { this.loading = false; }
      });
  }

  toggle(id: number): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  pay(id: number): void {
    this.service.payReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: () => this.load() });
  }

  cancel(id: number): void {
    if (!confirm('Annuler cette réservation ?')) return;
    this.service.cancelReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: () => this.load() });
  }

  leave(id: number): void {
    if (!confirm('Quitter cette réservation ?')) return;
    this.service.leaveReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({ next: () => this.load() });
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
