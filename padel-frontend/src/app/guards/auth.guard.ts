import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';

function readRoleFromToken(token: string): string | null {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload?.role ?? null;
  } catch {
    return null;
  }
}

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const token = sessionStorage.getItem('access_token');

  if (token) {
    return true;
  }

  router.navigate(['/login']);
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = sessionStorage.getItem('access_token');

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const role = readRoleFromToken(token);
  const isAdmin = role === 'ROLE_GLOBALADMIN' || role === 'ROLE_LOCALADMIN';

  if (!isAdmin) {
    router.navigate(['/home']);
    return false;
  }

  return true;
};

export const globalAdminGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = sessionStorage.getItem('access_token');

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  const role = readRoleFromToken(token);
  if (role !== 'ROLE_GLOBALADMIN') {
    router.navigate(['/admin']);
    return false;
  }

  return true;
};
