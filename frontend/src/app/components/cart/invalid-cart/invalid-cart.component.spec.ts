import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InvalidCartComponent } from './invalid-cart.component';

describe('InvalidCartComponent', () => {
  let component: InvalidCartComponent;
  let fixture: ComponentFixture<InvalidCartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InvalidCartComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InvalidCartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
