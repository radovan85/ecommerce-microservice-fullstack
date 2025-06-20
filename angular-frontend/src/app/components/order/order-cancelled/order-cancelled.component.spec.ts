import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderCancelledComponent } from './order-cancelled.component';

describe('OrderCancelledComponent', () => {
  let component: OrderCancelledComponent;
  let fixture: ComponentFixture<OrderCancelledComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrderCancelledComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrderCancelledComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
