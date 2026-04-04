import { Routes } from '@angular/router';
import { HomeComponent } from './layout/home/home';
import { LoginComponent } from './layout/login/login';
import { RegisterComponent } from './layout/register/register';
import { Reservation } from './reservation/reservation';
import { MyReservationComponent } from './my-reservation/my-reservation';
import { JoinReservationComponent } from './join-reservation/join-reservation';
import { ConfirmationReservation } from './ConfirmationReservation/ConfirmationReservation';

export const routes: Routes = [
  { path: '',                 redirectTo: 'home', pathMatch: 'full' },
  { path: 'home',             component: HomeComponent },
  { path: 'login',            component: LoginComponent },
  { path: 'register',         component: RegisterComponent },
  { path: 'reservation',      component: Reservation },
  { path: 'reservation/confirmation', component: ConfirmationReservation },
  { path: 'join-reservation', component: JoinReservationComponent },
  { path: 'my-reservations',  component: MyReservationComponent },
  { path: '**',               redirectTo: 'home' }
];
