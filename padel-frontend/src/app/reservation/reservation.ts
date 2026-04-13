import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import { CalendarGrid } from './calendar-grid/calendar-grid';
import { TerrainSelector } from './terrain-selector/terrain-selector';
import { WeekNavigator } from './week-navigator/week-navigator';
import { ReservationService, PlanningSlot } from '../services/reservation.service';
import { SiteDto, SiteService } from '../services/site.service';

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
export class Reservation implements OnInit, OnDestroy {
  currentDate: Date = new Date();
  weekDates: Date[] = [];
  slotsByDate: Map<string, string[]> = new Map();
  sites: SiteDto[] = [];
  loadingSites = false;
  canChooseSite = false;
  selectedSiteId: number | null = null;
  selectedTerrainId: number | null = null;
  occupiedSlots: Set<string> = new Set();
  availableSlotKeys: Set<string> = new Set();

  /** Subject qui alimente le switchMap : chaque push annule la requête précédente */
  private readonly planningRequest$ = new Subject<{ userId: string; siteId: number; date: string }>();
  private readonly destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private reservationService: ReservationService,
    private siteService: SiteService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.generateWeek();

    console.group('🚀 [Reservation] ngOnInit');
    console.log('weekDates générées :', this.weekDates.map(d => this.toLocalIsoDate(d)));
    console.log('userId token :', this.readUserIdFromToken());
    console.log('siteId token :', this.readSiteIdFromToken());
    console.log('role token   :', this.readRoleFromToken());
    console.groupEnd();

    this.loadSites();

    // switchMap : si une nouvelle requête arrive avant la fin de l'ancienne,
    // l'ancienne est automatiquement annulée (unsubscribe HTTP).
    this.planningRequest$.pipe(
      switchMap(({ userId, siteId, date }) => {
        console.log('📡 [Reservation] HTTP GET /api/planning →', { userId, siteId, date });
        return this.reservationService.getPlanning(userId, siteId, date);
      }),
      takeUntil(this.destroy$)
    ).subscribe({
      next: (slots: PlanningSlot[]) => this.processPlanning(slots),
      error: (err) => {
        console.error('❌ [Reservation] Erreur /api/planning :', err);
        this.slotsByDate = new Map();
        this.occupiedSlots = new Set();
        this.availableSlotKeys = new Set();
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSiteChange(siteIdValue: string): void {
    const siteId = Number(siteIdValue);
    this.selectedSiteId = Number.isFinite(siteId) && siteId > 0 ? siteId : null;
    this.selectedTerrainId = null;
    this.slotsByDate = new Map();
    this.occupiedSlots = new Set();
    this.availableSlotKeys = new Set();
    console.log('🏢 [Reservation] onSiteChange → selectedSiteId:', this.selectedSiteId);
  }

  getSelectedSiteName(): string {
    if (!this.selectedSiteId) {
      return 'Site non defini';
    }

    return this.sites.find((site) => site.siteId === this.selectedSiteId)?.nom || 'Site non defini';
  }

  onTerrainChange(terrainId: number | null): void {
    this.selectedTerrainId = terrainId;
    console.log('🏟️ [Reservation] onTerrainChange → terrainId:', terrainId);

    if (!terrainId) {
      this.slotsByDate = new Map();
      this.occupiedSlots = new Set();
      this.availableSlotKeys = new Set();
      console.warn('⚠️ [Reservation] terrainId null → planning annulé');
      return;
    }

    this.loadOccupiedSlots();
  }

  private loadOccupiedSlots(): void {
    const userId = this.readUserIdFromToken();

    console.group('🔄 [Reservation] loadOccupiedSlots');
    console.log('userId          :', userId);
    console.log('selectedSiteId  :', this.selectedSiteId);
    console.log('selectedTerrainId:', this.selectedTerrainId);
    console.log('weekDates[0]    :', this.weekDates.length > 0 ? this.toLocalIsoDate(this.weekDates[0]) : 'vide');
    console.groupEnd();

    if (!userId || !this.selectedTerrainId || !this.selectedSiteId) {
      console.warn('⛔ [Reservation] loadOccupiedSlots bloqué — données manquantes :', { userId, siteId: this.selectedSiteId, terrainId: this.selectedTerrainId });
      this.slotsByDate = new Map();
      this.occupiedSlots = new Set();
      this.availableSlotKeys = new Set();
      return;
    }

    const weekStartDate =
      this.weekDates.length > 0
        ? this.toLocalIsoDate(this.weekDates[0])
        : this.toLocalIsoDate(this.getStartOfWeek(this.currentDate));

    console.log('➡️ [Reservation] planningRequest$.next →', { userId, siteId: this.selectedSiteId, date: weekStartDate });
    this.planningRequest$.next({ userId, siteId: this.selectedSiteId, date: weekStartDate });
  }

  private processPlanning(slots: PlanningSlot[]): void {
    console.group('📦 [Reservation] processPlanning');
    console.log('slots total reçus   :', slots.length);
    console.log('selectedTerrainId   :', this.selectedTerrainId);

    const terrainSlots = slots.filter((s) => s.terrainId === this.selectedTerrainId);
    console.log('terrainSlots filtrés :', terrainSlots.length);

    if (terrainSlots.length === 0) {
      console.warn('⚠️ Aucun slot pour ce terrain. terrainIds présents dans la réponse :',
        [...new Set(slots.map(s => s.terrainId))]);
    }

    const nextSlotsByDate = new Map<string, string[]>();
    for (const slot of terrainSlots) {
      const uiTime = this.toUiTime(slot.heure);
      const daySlots = nextSlotsByDate.get(slot.date) ?? [];
      if (!daySlots.includes(uiTime)) {
        daySlots.push(uiTime);
        daySlots.sort((a, b) => a.localeCompare(b));
        nextSlotsByDate.set(slot.date, daySlots);
      }
    }

    this.slotsByDate = nextSlotsByDate;
    console.log('slotsByDate (date → créneaux) :');
    this.slotsByDate.forEach((times, date) => console.log('  ', date, '→', times));

    this.availableSlotKeys = new Set(
      terrainSlots.map((s) => `${s.date}_${this.toUiTime(s.heure)}`)
    );
    this.occupiedSlots = new Set(
      terrainSlots
        .filter((s) => !s.disponible)
        .map((s) => `${s.date}_${this.toUiTime(s.heure)}`)
    );

    console.log('availableSlotKeys :', this.availableSlotKeys.size, 'clés');
    console.log('occupiedSlots     :', this.occupiedSlots.size, 'clés');
    console.groupEnd();

    // Force le re-render du CalendarGrid pour afficher les nouveaux créneaux
    this.cdr.detectChanges();
  }

  isOccupied(day: Date, slot: string): boolean {
    const dateStr = this.toLocalIsoDate(day);
    const key = `${dateStr}_${slot}`;

    if (!this.availableSlotKeys.has(key)) {
      return true;
    }

    return this.occupiedSlots.has(key);
  }

  getSlotsForDay(day: Date): string[] {
    const dateStr = this.toLocalIsoDate(day);
    const slots = this.slotsByDate.get(dateStr) ?? [];
    if (slots.length > 0) {
      console.log(`📅 [Reservation] getSlotsForDay(${dateStr}) →`, slots);
    }
    return slots;
  }

  previousWeek(): void {
    this.currentDate = new Date(this.currentDate);
    this.currentDate.setDate(this.currentDate.getDate() - 7);
    this.generateWeek();
    console.log('⬅️ [Reservation] previousWeek → semaine du', this.toLocalIsoDate(this.weekDates[0]));
    this.loadOccupiedSlots();
  }

  nextWeek(): void {
    this.currentDate = new Date(this.currentDate);
    this.currentDate.setDate(this.currentDate.getDate() + 7);
    this.generateWeek();
    console.log('➡️ [Reservation] nextWeek → semaine du', this.toLocalIsoDate(this.weekDates[0]));
    this.loadOccupiedSlots();
  }

  onSlotSelected(event: { day: Date; slot: string }): void {
    if (this.isOccupied(event.day, event.slot) || !this.selectedTerrainId || !this.selectedSiteId) {
      return;
    }

    const heureDebut = event.slot;
    const heureFin = this.addMinutesToTime(event.slot, 90);

    this.router.navigate(['/reservation/confirmation'], {
      state: {
        siteId: this.selectedSiteId,
        terrainId: this.selectedTerrainId,
        date: this.toLocalIsoDate(event.day),
        heureDebut,
        heureFin
      }
    });
  }

  private generateWeek(): void {
    const start = this.getStartOfWeek(this.currentDate);
    this.weekDates = [];

    for (let i = 0; i < 7; i++) {
      const day = new Date(start);
      day.setDate(start.getDate() + i);
      this.weekDates.push(day);
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

  private addMinutesToTime(time: string, minutesToAdd: number): string {
    const [hours, minutes] = time.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes + minutesToAdd;
    const endHours = Math.floor((totalMinutes / 60) % 24);
    const endMinutes = totalMinutes % 60;

    return `${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}`;
  }

  toLocalIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  private toUiTime(time: string): string {
    return time.length >= 5 ? time.slice(0, 5) : time;
  }

  private readUserIdFromToken(): string | null {
    const token = sessionStorage.getItem('access_token');

    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.sub ?? null;
    } catch {
      return null;
    }
  }

  private readSiteIdFromToken(): number | null {
    const token = sessionStorage.getItem('access_token');

    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.siteId ?? null;
    } catch {
      return null;
    }
  }

  private readRoleFromToken(): string | null {
    const token = sessionStorage.getItem('access_token');

    if (!token) {
      return null;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.role ?? null;
    } catch {
      return null;
    }
  }

  private loadSites(): void {
    this.loadingSites = true;
    const role = this.readRoleFromToken();
    const tokenSiteId = this.readSiteIdFromToken();
    this.canChooseSite = role === 'ROLE_FREEUSER' || role === 'ROLE_GLOBALUSER';
    console.log('🏢 [Reservation] loadSites → role:', role, '| tokenSiteId:', tokenSiteId, '| canChooseSite:', this.canChooseSite);

    this.siteService.getSites().subscribe({
      next: (sites) => {
        this.sites = sites;

        if (this.canChooseSite) {
          this.selectedSiteId = tokenSiteId ?? (sites.length > 0 ? sites[0].siteId : null);
        } else {
          this.selectedSiteId = tokenSiteId;
        }

        this.loadingSites = false;
        console.log('✅ [Reservation] Sites chargés :', sites.map(s => s.nom), '→ selectedSiteId:', this.selectedSiteId);

        // Sans zone.js le template ne se re-rend pas automatiquement après un callback HTTP.
        // detectChanges() force Angular à propager selectedSiteId vers TerrainSelector.
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('❌ [Reservation] Erreur chargement sites :', err);
        this.sites = [];
        this.selectedSiteId = tokenSiteId;
        this.loadingSites = false;
        this.cdr.detectChanges();
      }
    });
  }
}
