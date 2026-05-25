import { Component, Input } from '@angular/core';
import { KpiItem } from '../../Interface/Kpi-item';
import { UiCard } from '../../ui-card/ui-card';

@Component({
  selector: 'app-kpi-grid-widget',
  standalone: true,
  imports: [UiCard],
  templateUrl: './kpi-grid-widget.html',
})
export class KpiGridWidget {
  @Input() items: KpiItem[] = [];
}


