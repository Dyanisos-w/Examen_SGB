import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-calendar-grid',
  templateUrl: './calendar-grid.html',
  styleUrl: './calendar-grid.css',
  standalone: true,
  imports: [CommonModule]
})
export class CalendarGrid {
  @Input() weekDates!: Date[];
  @Input() timeSlots!: string[];

  get gridColumns(): string {
    return 'repeat(' + (this.weekDates.length ) + ', 1fr)';
  }
}
