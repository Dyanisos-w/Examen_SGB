import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { TerrainSelector } from './terrain-selector';
import { TerrainService } from '../../services/terrain.service';

describe('TerrainSelector', () => {
  let component: TerrainSelector;
  let fixture: ComponentFixture<TerrainSelector>;

  beforeEach(async () => {
    const terrainServiceMock = {
      getTerrains: () => of([])
    };

    await TestBed.configureTestingModule({
      imports: [TerrainSelector],
      providers: [{ provide: TerrainService, useValue: terrainServiceMock }]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TerrainSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should clear terrain selection when site is null', () => {
    component.siteId = null;
    component.ngOnChanges({
      siteId: {
        currentValue: null,
        previousValue: 1,
        firstChange: false,
        isFirstChange: () => false
      }
    });

    expect(component.selectedTerrainId).toBeNull();
  });
});
