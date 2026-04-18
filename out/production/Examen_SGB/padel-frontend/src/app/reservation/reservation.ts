import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CalendarGrid } from './calendar-grid/calendar-grid';
import { TerrainSelector } from './terrain-selector/terrain-selector';
import { WeekNavigator } from './week-navigator/week-navigator';

@Component({
  selector: 'app-reservation',
  templateUrl: './reservation.html',
  styleUrl: './reservation.css',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    CalendarGrid,
    TerrainSelector,
    WeekNavigator
  ]
})
export class Reservation implements OnInit {

  currentDate: Date = new Date();
  weekDates: Date[] = [];
  timeSlots: string[] = [];
  selectedTerrainId!: number;

  ngOnInit(): void {
    this.generateWeek();
    this.generateTimeSlots();
  }

  onTerrainChange(terrainId: number) {
    this.selectedTerrainId = terrainId;
  }

  previousWeek() {
    this.currentDate = new Date(this.currentDate);
    this.currentDate.setDate(this.currentDate.getDate() - 7);
    this.generateWeek();
  }

  nextWeek() {
    this.currentDate = new Date(this.currentDate);
    this.currentDate.setDate(this.currentDate.getDate() + 7);
    this.generateWeek();
  }

  private generateWeek() {
    const start = this.getStartOfWeek(this.currentDate);
    this.weekDates = [];

    for (let i = 0; i < 7; i++) {
      const day = new Date(start);
      day.setDate(start.getDate() + i);
      this.weekDates.push(day);
    }
  }

  private generateTimeSlots() {
    this.timeSlots = [];

    for (let h = 14; h <= 22; h++) {
      this.timeSlots.push(`${h}:00`);
      this.timeSlots.push(`${h}:30`);
    }
  }

  private getStartOfWeek(date: Date): Date {
    const d = new Date(date);
    const day = d.getDay();
    const diff = d.getDate() - day + (day === 0 ? -6 : 1);
    return new Date(d.setDate(diff));
  }

  get weekNumber(): number {
    const oneJan = new Date(this.currentDate.getFullYear(), 0, 1);
    const numberOfDays = Math.floor(
      (this.currentDate.getTime() - oneJan.getTime()) / 86400000
    );
    return Math.ceil((this.currentDate.getDay() + 1 + numberOfDays) / 7);
  }
}
