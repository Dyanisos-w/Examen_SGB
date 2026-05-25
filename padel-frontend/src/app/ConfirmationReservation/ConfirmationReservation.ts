import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { firstValueFrom, forkJoin } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { CreateReservationRequest, ReservationService } from '../services/reservation.service';
import { environment } from '../../environments/environment';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-confirmation-reservation',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ConfirmationReservation.html'
})
export class ConfirmationReservation implements OnInit {
  private notification = inject(NotificationService);

  siteId = 0;
  terrainId = 0;
  heureDebut = '';
  heureFin = '';
  reservationDate = '';

  typeReservation: string = 'PUBLIC';

  matricule1: string = '';
  matricule2: string = '';
  matricule3: string = '';

  montant = 15;
  dette = 0;
  isSubmitting = false;

  constructor(
    private router: Router,
    private reservationService: ReservationService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const navigation = this.router.getCurrentNavigation();
    const state = (navigation?.extras.state || history.state) as {
      siteId?: number;
      terrainId?: number;
      date?: string;
      heureDebut?: string;
      heureFin?: string;
    };

    if (!state?.date || !state?.heureDebut || !state?.siteId) {
      this.router.navigate(['/reservation']);
      return;
    }

    this.siteId = state.siteId;
    this.terrainId = state.terrainId ?? 0;
    this.heureDebut = state.heureDebut;
    this.heureFin = state.heureFin ?? this.heureDebut;
    this.reservationDate = state.date;

    const matricule = this.getMatriculeFromToken();
    if (matricule) {
      this.http.get<{ penaliteMontant?: number }>(`${environment.apiBaseUrl}/utilisateurs/${matricule}`)
        .subscribe({
          next: (user) => {
            this.dette = user.penaliteMontant ?? 0;
            this.montant = 15 + this.dette;
          }
        });
    }
  }

  private getMatriculeFromToken(): string | null {
    const token = sessionStorage.getItem('access_token');
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]))?.sub ?? null;
    } catch {
      return null;
    }
  }

  async confirmer() {
    const invites = [this.matricule1, this.matricule2, this.matricule3]
      .map((value) => value.trim().toUpperCase())
      .filter((value) => value !== '');

    if (this.typeReservation === 'PRIVATE') {
      if (invites.length !== 3) {
        this.notification.error('Une reservation privee necessite exactement 3 invites.');
        return;
      }

      const unique = new Set(invites);
      if (unique.size !== 3) {
        this.notification.error('Les invites doivent etre uniques.');
        return;
      }

      this.isSubmitting = true;
      try {
        const checks$ = invites.map((matricule) =>
          this.reservationService.validateMatriculeExists(matricule)
        );
        const checkResults = invites.length > 0
          ? await firstValueFrom(forkJoin(checks$))
          : [];

        const invalidMatricules = invites.filter((_, i) => !checkResults[i]);
        if (invalidMatricules.length > 0) {
          this.notification.error(`Matricule(s) introuvable(s) : ${invalidMatricules.join(', ')}`);
          return;
        }
      } catch {
        this.notification.error('Erreur lors de la verification des invites. Reessayez.');
        return;
      } finally {
        this.isSubmitting = false;
      }
    }

    const reservation: CreateReservationRequest = {
      siteId: this.siteId,
      terrainId: this.terrainId,
      date: this.reservationDate,
      heureDebut: this.heureDebut,
      typeReservation: this.typeReservation
    };

    this.isSubmitting = true;
    let reservationId: number | null = null;

    try {
      reservationId = await firstValueFrom(
        this.reservationService.createReservationRequest(reservation)
      );

      if (this.typeReservation === 'PRIVATE') {
        const addPlayersRequests$ = invites.map((matricule) =>
          this.reservationService.addPlayerToPrivateReservation(reservationId!, matricule)
        );
        await firstValueFrom(forkJoin(addPlayersRequests$));
      }

      this.router.navigate(['/my-reservations'], {
        state: {
          createdReservationId: reservationId,
          paymentReminder: true
        }
      });
    } catch {
      if (this.typeReservation === 'PRIVATE' && reservationId !== null) {
        try {
          await firstValueFrom(this.reservationService.cancelReservation(reservationId));
        } catch {
          // Best effort rollback to avoid leaving an incomplete private reservation.
        }
        this.notification.error('Erreur lors de l\'ajout des invites. La reservation privee a ete annulee.');
      } else {
        this.notification.error('La reservation a echoue. Verifiez les donnees et reessayez.');
      }
    } finally {
      this.isSubmitting = false;
    }
  }

  annuler() {
    this.router.navigate(['/reservation']);
  }
}
