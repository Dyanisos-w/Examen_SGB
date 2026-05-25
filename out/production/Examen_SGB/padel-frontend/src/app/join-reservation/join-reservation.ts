import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reservations } from '../Service/reservations';
import { PublicReservation } from '../Interface/my-reservation';
import { HttpErrorResponse } from '@angular/common/http';
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

  constructor(
    private service: Reservations,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    console.log('[JoinReservation] ngOnInit → load()');
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    console.log('[JoinReservation] load() → GET /api/reservations/public');
    this.service.getPublicReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          console.log('[JoinReservation] GET /api/reservations/public OK', { count: data.length, data });
          this.reservations = data;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[JoinReservation] GET /api/reservations/public ERR', {
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

  join(id: number): void {
    console.log('[JoinReservation] join()', { reservationId: id });
    this.joiningId = id;
    this.cdr.detectChanges();
    this.service.joinReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          console.log('[JoinReservation] join() OK', { reservationId: id });
          this.joiningId = null;
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[JoinReservation] join() ERR', {
            reservationId: id,
            status: err.status,
            body: err.error
          });
          this.joiningId = null;
          this.cdr.detectChanges();
          alert(err.error?.message || 'Impossible de rejoindre cette réservation.');
        }
      });
  }

  slots(): number[] { return [1, 2, 3, 4]; }

  trackById(_: number, r: PublicReservation): number { return r.reservationId; }
}

