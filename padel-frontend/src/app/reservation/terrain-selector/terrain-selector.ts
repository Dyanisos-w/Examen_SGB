import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TerrainService, TerrainDto } from '../../services/terrain.service';

@Component({
  selector: 'app-terrain-selector',
  templateUrl: './terrain-selector.html',
  styleUrl: './terrain-selector.css',
  standalone: true,
  imports: [CommonModule]
})
export class TerrainSelector implements OnInit {

  @Output() terrainChange = new EventEmitter<number>();

  terrains: TerrainDto[] = [];
  loading = true;
  selectedTerrainId: number | null = null;

  constructor(private terrainService: TerrainService) {}

  ngOnInit(): void {
    const siteId = this.readSiteIdFromToken();
    this.terrainService.getTerrains(siteId ?? undefined).subscribe({
      next: (terrains) => {
        this.terrains = terrains;
        this.loading = false;
        if (terrains.length > 0) {
          this.selectedTerrainId = terrains[0].terrainId;
          this.terrainChange.emit(terrains[0].terrainId);
        }
      },
      error: () => { this.loading = false; }
    });
  }

  selectTerrain(event: Event): void {
    const value = +(event.target as HTMLSelectElement).value;
    this.selectedTerrainId = value;
    this.terrainChange.emit(value);
  }

  private readSiteIdFromToken(): number | null {
    const token = sessionStorage.getItem('access_token');
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.siteId ?? null;
    } catch {
      return null;
    }
  }
}
