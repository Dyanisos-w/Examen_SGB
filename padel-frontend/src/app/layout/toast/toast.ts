import { ChangeDetectorRef, Component, Input, OnInit, OnDestroy } from '@angular/core';
import { NgClass } from '@angular/common';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { NotificationService, Toast } from '../../services/notification.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [NgClass],
  templateUrl: './toast.html'
})
export class ToastComponent implements OnInit, OnDestroy {
  @Input() inline = false;

  toasts: Toast[] = [];
  private sub!: Subscription;

  constructor(private ns: NotificationService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    if (this.inline) this.ns.registerInline();

    this.sub = this.ns.toast$.pipe(
      filter(() => this.inline || this.ns.inlineCount === 0)
    ).subscribe(t => {
      this.toasts.push(t);
      this.cdr.detectChanges();
      if (t.duration > 0) {
        setTimeout(() => this.dismiss(t.id), t.duration);
      }
    });
  }

  ngOnDestroy(): void {
    if (this.inline) this.ns.unregisterInline();
    this.sub.unsubscribe();
  }

  dismiss(id: number): void {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }

  classes(type: string): Record<string, boolean> {
    return {
      'border-emerald-600 bg-emerald-900/90 text-emerald-200': type === 'success',
      'border-red-700 bg-red-900/90 text-red-300':             type === 'error',
      'border-sky-700 bg-sky-900/90 text-sky-200':             type === 'info',
    };
  }

  trackById(_: number, t: Toast): number { return t.id; }
}
