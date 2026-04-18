import {Component, EventEmitter, Output,Input} from '@angular/core';


@Component({
  selector: 'app-week-navigator',
  imports: [],
  templateUrl: './week-navigator.html',
  styleUrl: './week-navigator.css',
})
export class WeekNavigator {
  @Input() weekNumber!: number;
  @Output() previous = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();

}
