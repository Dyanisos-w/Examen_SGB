import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reservations } from '../Service/reservations';
import { PublicReservation } from '../Interface/my-reservation';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-join-reservation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './join-reservation.html',
})
export class JoinReservationComponent implements OnInit, OnDestroy {
  reservations: PublicReservation[] = [];
  loading = true;
  joiningId: number | null = null;
  private destroy$ = new Subject<void>();

  constructor(private service: Reservations) {}

  ngOnInit(): void { this.load(); }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    this.service.getPublicReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => { this.reservations = data; this.loading = false; },
        error: ()    => { this.loading = false; }
      });
  }

  join(id: number): void {
    this.joiningId = id;
    this.service.joinReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => { this.joiningId = null; this.load(); },
        error: (err) => {
          this.joiningId = null;
          alert(err.error?.message || 'Impossible de rejoindre cette réservation.');
        }
      });
  }

  slots(): number[] { return [1, 2, 3, 4]; }

  trackById(_: number, r: PublicReservation): number { return r.reservationId; }
}

