import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PhoneConfirmationComponent } from './phone-confirmation.component';

describe('PhoneConfirmationComponent', () => {
  let component: PhoneConfirmationComponent;
  let fixture: ComponentFixture<PhoneConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PhoneConfirmationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PhoneConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
