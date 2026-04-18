import { Component, Input } from '@angular/core';
import { NgClass } from '@angular/common';
import { KpiItem } from '../../Interface/Kpi-item';
import { UiCard } from '../../ui-card/ui-card';

@Component({
  selector: 'app-kpi-grid-widget',
  standalone: true,
  imports: [UiCard, NgClass],
  templateUrl: './kpi-grid-widget.html',
})
export class KpiGridWidget {
  @Input() items: KpiItem[] = [];

  trendClass(trend: KpiItem['trend']): string {
	switch (trend) {
	  case 'up':
		return 'bg-emerald-500/15 text-emerald-300 border border-emerald-500/30';
	  case 'down':
		return 'bg-rose-500/15 text-rose-300 border border-rose-500/30';
	  default:
		return 'bg-slate-500/20 text-slate-300 border border-slate-500/30';
	}
  }

  trendPrefix(delta: number): string {
	return delta > 0 ? '+' : '';
  }
}


