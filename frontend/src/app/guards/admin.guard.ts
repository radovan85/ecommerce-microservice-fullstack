import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export var adminGuard: CanActivateFn = async (route, state) => {
  var authService = inject(AuthService);
  var router = inject(Router);

  await new Promise(resolve => setTimeout(resolve, 200)); // Kratak delay da se role učita
  
  if (authService.isAdmin()) {
    return true;
  } else {
    router.navigate([`home`]);
    return false;
  }
};

