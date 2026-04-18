import {Component, Input} from '@angular/core';

@Component({
  selector: 'app-ui-card',
  imports: [],
  templateUrl: './ui-card.html',
  standalone: true,
})
export class UiCard {
@Input() title?: string;
@Input() subtitle?: string;
}
