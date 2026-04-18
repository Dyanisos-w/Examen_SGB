import { Component } from '@angular/core';
import {Header} from '../header/header';
import {RouterLink} from '@angular/router';

@Component({
  standalone: true,
  selector: 'app-home',
  imports: [
    Header,
    RouterLink
  ],
  templateUrl: './home.html'
})
export class HomeComponent {}

