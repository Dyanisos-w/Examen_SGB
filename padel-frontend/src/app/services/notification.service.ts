import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Toast {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info';
  duration: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private _toast$ = new Subject<Toast>();
  toast$ = this._toast$.asObservable();
  inlineCount = 0;
  private counter = 0;

  success(message: string, duration = 4000): void {
    this._toast$.next({ id: ++this.counter, message, type: 'success', duration });
  }

  error(message: string, duration = 5000): void {
    this._toast$.next({ id: ++this.counter, message, type: 'error', duration });
  }

  info(message: string, duration = 4000): void {
    this._toast$.next({ id: ++this.counter, message, type: 'info', duration });
  }

  registerInline(): void { this.inlineCount++; }
  unregisterInline(): void { this.inlineCount = Math.max(0, this.inlineCount - 1); }
}
