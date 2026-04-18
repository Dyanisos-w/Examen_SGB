import { ComponentFixture, TestBed } from '@angular/core/testing';

import { WeekNavigator } from './week-navigator';

describe('WeekNavigator', () => {
  let component: WeekNavigator;
  let fixture: ComponentFixture<WeekNavigator>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [WeekNavigator]
    })
    .compileComponents();

    fixture = TestBed.createComponent(WeekNavigator);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
