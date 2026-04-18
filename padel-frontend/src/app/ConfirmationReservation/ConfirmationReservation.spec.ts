import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';

import { ConfirmationReservation } from './ConfirmationReservation';
import { ReservationService } from '../services/reservation.service';

describe('ConfirmationReservation', () => {
  let component: ConfirmationReservation;
  let fixture: ComponentFixture<ConfirmationReservation>;

  beforeEach(async () => {
    const routerMock = {
      getCurrentNavigation: () => ({
        extras: {
          state: {
            siteId: 1,
            terrainId: 1,
            date: '2026-04-12',
            heureDebut: '10:00',
            heureFin: '11:30'
          }
        }
      }),
      navigate: () => Promise.resolve(true)
    };

    const reservationServiceMock = {
      validateMatriculeExists: () => of(true),
      createReservationRequest: () => of(1)
    };

    await TestBed.configureTestingModule({
      imports: [ConfirmationReservation],
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: ReservationService, useValue: reservationServiceMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ConfirmationReservation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
