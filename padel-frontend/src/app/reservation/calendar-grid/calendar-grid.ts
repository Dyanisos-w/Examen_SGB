import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-calendar-grid',
  templateUrl: './calendar-grid.html',
  styleUrl: './calendar-grid.css',
  standalone: true,
  imports: [CommonModule]
})
export class CalendarGrid implements OnChanges {
  @Input() weekDates!: Date[];
  @Input() getSlotsForDayFn: (day: Date) => string[] = () => [];
  @Input() isOccupiedFn: (day: Date, slot: string) => boolean = () => false;
  @Output() slotSelected = new EventEmitter<{ day: Date; slot: string }>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['weekDates'] && this.weekDates) {
      console.log('📅 [CalendarGrid] weekDates reçus :', this.weekDates.map(d =>
        `${d.toLocaleDateString('fr-BE', { weekday: 'short' })} ${d.getDate()}/${d.getMonth() + 1}`
      ));
    }
    if (changes['getSlotsForDayFn'] && this.weekDates?.length) {
      console.log('🔧 [CalendarGrid] getSlotsForDayFn mise à jour — slots par jour :');
      let total = 0;
      this.weekDates.forEach(day => {
        const slots = this.getSlotsForDayFn(day);
        const label = `${day.getDate()}/${day.getMonth() + 1}`;
        if (slots.length > 0) {
          console.log(`  📅 ${label} →`, slots);
        } else {
          console.log(`  📅 ${label} → (vide)`);
        }
        total += slots.length;
      });
      console.log('  Total créneaux visibles :', total);
    }
  }

  get gridColumns(): string {
    return 'repeat(' + this.weekDates.length + ', 1fr)';
  }

  isOccupied(day: Date, slot: string): boolean {
    return this.isOccupiedFn(day, slot);
  }

  slotsForDay(day: Date): string[] {
    return this.getSlotsForDayFn(day);
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
