import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TerrainSelector } from './terrain-selector';

describe('TerrainSelector', () => {
  let component: TerrainSelector;
  let fixture: ComponentFixture<TerrainSelector>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TerrainSelector]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TerrainSelector);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
