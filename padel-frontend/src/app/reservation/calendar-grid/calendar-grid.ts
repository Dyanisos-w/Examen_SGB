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
  @Input() isOccupiedFn: (day: Date, slot: string) => boolean = () => false;
  @Output() slotSelected = new EventEmitter<{ day: Date; slot: string }>();

  get gridColumns(): string {
    return 'repeat(' + this.weekDates.length + ', 1fr)';
  }

  isOccupied(day: Date, slot: string): boolean {
    return this.isOccupiedFn(day, slot);
  }

  onSlotClick(day: Date, slot: string): void {
    if (!this.isOccupied(day, slot)) {
      this.slotSelected.emit({ day, slot });
    }
  }

  isPast(day: Date, slot: string): boolean {
    const [h, m] = slot.split(':').map(Number);
    const slotDate = new Date(day);
    slotDate.setHours(h, m, 0, 0);
    return slotDate < new Date();
  }
}
