import { TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { AdminCreateSiteComponent } from './admin-create-site';

describe('AdminCreateSiteComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminCreateSiteComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(AdminCreateSiteComponent);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });
});

