import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminContent } from './admin-content';

describe('AdminContent', () => {
  let component: AdminContent;
  let fixture: ComponentFixture<AdminContent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminContent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminContent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
