import { ChangeDetectorRef, Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TerrainService, TerrainDto } from '../../services/terrain.service';

@Component({
  selector: 'app-terrain-selector',
  templateUrl: './terrain-selector.html',
  styleUrl: './terrain-selector.css',
  standalone: true,
  imports: [CommonModule]
})
export class TerrainSelector implements OnChanges {

  @Input() siteId: number | null = null;
  @Output() terrainChange = new EventEmitter<number | null>();

  terrains: TerrainDto[] = [];
  loading = true;
  loadError: string | null = null;
  selectedTerrainId: number | null = null;

  constructor(
    private terrainService: TerrainService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['siteId']) {
      return;
    }

    const prev = changes['siteId'].previousValue;
    const curr = changes['siteId'].currentValue;
    console.log(`🗺️ [TerrainSelector] ngOnChanges siteId: ${prev} → ${curr}`);

    if (!this.siteId) {
      this.loading = false;
      this.terrains = [];
      this.selectedTerrainId = null;
      this.terrainChange.emit(null);
      console.warn('⚠️ [TerrainSelector] siteId null → emit(null)');
      return;
    }

    this.loading = true;
    this.loadError = null;
    console.log(`📡 [TerrainSelector] GET /terrains?siteId=${this.siteId}`);

    this.terrainService.getTerrains(this.siteId).subscribe({
      next: (terrains) => {
        this.terrains = terrains;
        this.loading = false;
        this.loadError = null;
        console.log('✅ [TerrainSelector] Terrains reçus :', terrains.map(t => `[${t.terrainId}] ${t.nom}`));

        if (terrains.length > 0) {
          this.selectedTerrainId = terrains[0].terrainId;
          this.terrainChange.emit(terrains[0].terrainId);
          console.log('➡️ [TerrainSelector] terrainChange émis → terrainId:', terrains[0].terrainId);
        } else {
          this.selectedTerrainId = null;
          this.terrainChange.emit(null);
          console.warn('⚠️ [TerrainSelector] Aucun terrain pour siteId', this.siteId, '→ emit(null)');
        }

        // Force le re-render du <select> terrain sans zone.js
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.loadError = 'Impossible de charger les terrains.';
        this.selectedTerrainId = null;
        this.terrainChange.emit(null);
        console.error('❌ [TerrainSelector] Erreur GET /terrains :', err);
        this.cdr.detectChanges();
      }
    });
  }

  selectTerrain(event: Event): void {
    const value = +(event.target as HTMLSelectElement).value;
    this.selectedTerrainId = value || null;
    this.terrainChange.emit(this.selectedTerrainId);
    console.log('🖱️ [TerrainSelector] Sélection manuelle → terrainId:', this.selectedTerrainId);
  }
}
