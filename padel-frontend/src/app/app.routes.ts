import { Routes } from '@angular/router';
import {HomeComponent} from './layout/home/home';
import {LoginComponent} from './layout/login/login';
import {RegisterComponent} from './layout/register/register';
import {Reservation} from './reservation/reservation';

export const routes: Routes = [
  {path: '', redirectTo: 'home', pathMatch: 'full'},
  {path: 'home', component : HomeComponent},
  {path: 'login' , component: LoginComponent},
  {path: 'register' , component : RegisterComponent},
  {path: 'register/register', component: RegisterComponent},
  {path: 'reservation', component: Reservation}
];
