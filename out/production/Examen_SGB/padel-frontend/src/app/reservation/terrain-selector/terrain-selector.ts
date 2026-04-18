import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-terrain-selector',
  templateUrl: './terrain-selector.html',
  styleUrl: './terrain-selector.css',
  standalone: true,
  imports: [CommonModule]
})
export class TerrainSelector {

  @Output() terrainChange = new EventEmitter<number>();

  terrains = [
    { id: 1, name: 'Terrain 1' },
    { id: 2, name: 'Terrain 2' },
    { id: 3, name: 'Terrain 3' }
  ];

  selectTerrain(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    this.terrainChange.emit(Number(value));
  }
}
