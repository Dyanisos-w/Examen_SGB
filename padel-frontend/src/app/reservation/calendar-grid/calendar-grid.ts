import { Component, EventEmitter, Input, Output } from '@angular/core';
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
  @Output() slotSelected = new EventEmitter<{ day: Date; slot: string }>();

  get gridColumns(): string {
    return 'repeat(' + (this.weekDates.length ) + ', 1fr)';
  }

  onSlotClick(day: Date, slot: string): void {
    this.slotSelected.emit({ day, slot });
  }
}
