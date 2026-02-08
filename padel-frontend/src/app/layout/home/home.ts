import { Component } from '@angular/core';
import {Header} from '../header/header';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [
    Header
  ],
  templateUrl: './home.html'
})
export class HomeComponent {}

