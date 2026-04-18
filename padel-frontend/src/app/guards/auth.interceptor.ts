import { HttpBackend, HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const httpBackend = inject(HttpBackend);
  const rawHttp = new HttpClient(httpBackend);

  if (req.url.includes('/api/auth/login') || req.url.includes('/api/auth/register') || req.url.includes('/api/auth/refresh')) {
    return next(req);
  }

  const token = sessionStorage.getItem('access_token');
  const refreshToken = sessionStorage.getItem('refresh_token');

  const logout = () => {
    sessionStorage.removeItem('access_token');
    sessionStorage.removeItem('refresh_token');
    router.navigate(['/login']);
  };

  const withBearer = (accessToken: string) => req.clone({
    setHeaders: {
      Authorization: `Bearer ${accessToken}`
    }
  });

  const refreshAccessToken = () => rawHttp.post<{ accessToken: string }>(
    `${environment.apiBaseUrl}/api/auth/refresh`,
    { refreshToken }
  );

  if (token && isTokenExpired(token) && refreshToken) {
    return refreshAccessToken().pipe(
      switchMap((response) => {
        sessionStorage.setItem('access_token', response.accessToken);
        return next(withBearer(response.accessToken));
      }),
      catchError((error) => {
        logout();
        return throwError(() => error);
      })
    );
  }

  if (token) {
    req = withBearer(token);
  }

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const shouldRetryWithRefresh = error.status === 403 && !!refreshToken && !!token && isTokenExpired(token);
      if (!shouldRetryWithRefresh) {
        return throwError(() => error);
      }

      return refreshAccessToken().pipe(
        switchMap((response) => {
          sessionStorage.setItem('access_token', response.accessToken);
          return next(withBearer(response.accessToken));
        }),
        catchError((refreshError) => {
          logout();
          return throwError(() => refreshError);
        })
      );
    })
  );
};

function isTokenExpired(token: string): boolean {
  const payload = readJwtPayload(token);
  if (!payload || typeof payload['exp'] !== 'number') {
    return false;
  }

  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload['exp'] <= nowInSeconds;
}

function readJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return null;
  }
}

