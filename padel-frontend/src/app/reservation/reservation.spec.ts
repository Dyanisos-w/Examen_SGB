import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { Reservation } from './reservation';
import { ReservationService } from '../services/reservation.service';
import { SiteService } from '../services/site.service';
import { TerrainService } from '../services/terrain.service';
import { Router } from '@angular/router';

describe('Reservation', () => {
  let component: Reservation;
  let fixture: ComponentFixture<Reservation>;

  beforeEach(async () => {
    const routerMock = { navigate: () => Promise.resolve(true) };
    const reservationServiceMock = { getPlanning: () => of([]) };
    const siteServiceMock = { getSites: () => of([{ siteId: 1, nom: 'Site Test' }]) };
    const terrainServiceMock = { getTerrains: () => of([]) };

    await TestBed.configureTestingModule({
      imports: [Reservation],
      providers: [
        { provide: Router, useValue: routerMock },
        { provide: ReservationService, useValue: reservationServiceMock },
        { provide: SiteService, useValue: siteServiceMock },
        { provide: TerrainService, useValue: terrainServiceMock }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Reservation);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
