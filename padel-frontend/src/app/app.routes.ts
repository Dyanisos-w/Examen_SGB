import { Routes } from '@angular/router';
import { HomeComponent } from './layout/home/home';
import { LoginComponent } from './layout/login/login';
import { RegisterComponent } from './layout/register/register';
import { Reservation } from './reservation/reservation';
import { MyReservationComponent } from './my-reservation/my-reservation';
import { JoinReservationComponent } from './join-reservation/join-reservation';
import { ConfirmationReservation } from './ConfirmationReservation/ConfirmationReservation';
import { AdminDashboard } from './admin/admin-dashboard/admin-dashboard';
import { AdminRegisterLocalAdminComponent } from './admin/admin-register-local-admin/admin-register-local-admin';
import { AdminCreateSiteComponent } from './admin/admin-create-site/admin-create-site';
import { AdminCreateTerrainComponent } from './admin/admin-create-terrain/admin-create-terrain';
import {AdminOpeningHoursComponent} from './admin/admin-opening-hours/admin-opening-hours';
import { AdminClosuresComponent } from './admin/admin-closures/admin-closures';
import { authGuard, adminGuard, globalAdminGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '',                 redirectTo: 'home', pathMatch: 'full' },
  { path: 'home',             component: HomeComponent },
  { path: 'login',            component: LoginComponent },
  { path: 'register',         component: RegisterComponent },
  { path: 'reservation',      component: Reservation, canActivate: [authGuard] },
  { path: 'reservation/confirmation', component: ConfirmationReservation, canActivate: [authGuard] },
  { path: 'join-reservation', component: JoinReservationComponent, canActivate: [authGuard] },
  { path: 'my-reservations',  component: MyReservationComponent, canActivate: [authGuard] },
  { path: 'admin' , component: AdminDashboard, canActivate: [adminGuard] },
  { path: 'admin/local-admins/new', component: AdminRegisterLocalAdminComponent, canActivate: [globalAdminGuard] },
  { path: 'admin/sites/new', component: AdminCreateSiteComponent, canActivate: [globalAdminGuard] },
  { path: 'admin/terrains/new', component: AdminCreateTerrainComponent, canActivate: [adminGuard] },
  { path: 'admin/opening-hours', component: AdminOpeningHoursComponent, canActivate: [adminGuard] },
  { path: 'admin/closures', component: AdminClosuresComponent, canActivate: [adminGuard] },
  { path: '**',               redirectTo: 'home' }
];
