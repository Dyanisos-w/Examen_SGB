import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AdminRegisterLocalAdminComponent } from './admin-register-local-admin';
import { AdminUserManagementService } from '../services/admin-user-management.service';

describe('AdminRegisterLocalAdminComponent', () => {
  let component: AdminRegisterLocalAdminComponent;
  let fixture: ComponentFixture<AdminRegisterLocalAdminComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminRegisterLocalAdminComponent],
      providers: [
        {
          provide: AdminUserManagementService,
          useValue: {
            createLocalAdmin: () => of({ matricule: 'LA00001' })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminRegisterLocalAdminComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

