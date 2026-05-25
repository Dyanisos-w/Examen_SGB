import { Component, OnInit, OnDestroy, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Reservations } from '../Service/reservations';
import { PublicReservation } from '../Interface/my-reservation';
import { HttpErrorResponse } from '@angular/common/http';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-join-reservation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './join-reservation.html',
})
export class JoinReservationComponent implements OnInit, OnDestroy {
  private notification = inject(NotificationService);

  reservations: PublicReservation[] = [];
  loading = true;
  joiningId: number | null = null;
  private destroy$ = new Subject<void>();

  constructor(
    private service: Reservations,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.load();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  load(): void {
    this.loading = true;
    this.service.getPublicReservations()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.reservations = data;
          this.loading = false;
          this.cdr.detectChanges();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[JoinReservation] GET /api/reservations/public ERR', err);
          this.loading = false;
          this.cdr.detectChanges();
        }
      });
  }

  join(id: number): void {
    this.joiningId = id;
    this.cdr.detectChanges();
    this.service.joinReservation(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.notification.success('Vous avez rejoint la réservation.');
          this.joiningId = null;
          this.load();
        },
        error: (err: HttpErrorResponse) => {
          console.error('[JoinReservation] join() ERR', err);
          this.notification.error(err.error?.message || 'Impossible de rejoindre cette réservation.');
          this.joiningId = null;
          this.cdr.detectChanges();
        }
      });
  }

  slots(): number[] { return [1, 2, 3, 4]; }

  trackById(_: number, r: PublicReservation): number { return r.reservationId; }
}
