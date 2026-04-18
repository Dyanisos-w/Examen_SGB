import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminMobileOverlay } from './admin-mobile-overlay';

describe('AdminMobileOverlay', () => {
  let component: AdminMobileOverlay;
  let fixture: ComponentFixture<AdminMobileOverlay>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminMobileOverlay]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminMobileOverlay);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
