import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { unidentifiedGuard } from './unidentified.guard';

describe('unidentifiedGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => unidentifiedGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
