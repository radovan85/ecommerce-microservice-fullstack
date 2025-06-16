import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

export var unidentifiedGuard: CanActivateFn = (route, state) => {
  
  var authService = inject(AuthService);
  var router = inject(Router);
  var returnValue = false;

  if (authService.isAuthenticated()) {
    //console.log(`Unidentified guard: You are identified!Navigating home`);
    router.navigate([`home`]);
  } else {
    //console.log(`Unidentified guard: You are undentified!Everything is clear!`);
    returnValue = true;
  }

  return returnValue;
};
