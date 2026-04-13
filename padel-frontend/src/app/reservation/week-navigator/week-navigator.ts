import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';


@Component({
  selector: 'app-week-navigator',
  imports: [],
  templateUrl: './week-navigator.html',
  styleUrl: './week-navigator.css',
})
export class WeekNavigator implements OnChanges {
  @Input() weekNumber!: number;
  @Output() previous = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['weekNumber']) {
      console.log('🗓️ [WeekNavigator] weekNumber reçu :', this.weekNumber);
    }
  }
}
