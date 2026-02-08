import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {RegisterComponent} from './layout/register/register';
import {LoginComponent} from './layout/login/login';
import {HomeComponent} from './layout/home/home';
import {provideRouter} from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RegisterComponent, RegisterComponent, LoginComponent, HomeComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('padel-frontend');
}
