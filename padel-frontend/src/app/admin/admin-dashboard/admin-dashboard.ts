import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-6">
      <h1 class="text-2xl font-bold">Tableau de bord administrateur</h1>
    </div>
  `
})
export class AdminDashboard {
  constructor(private router: Router) {}
}
