import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';
import { AuthenticationRequest } from '../classes/authentication-request';
import { User } from '../classes/user';

export var authInterceptor = axios.interceptors.request.use(
  config => {
    var authToken = localStorage.getItem('authToken');
    if (authToken) {
      config.headers.Authorization = `${authToken}`;
    }

    return config;
  });



export var errorInterceptor = axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response.status === 401 || error.response.status === 403) {
      localStorage.clear();
      window.location.reload();
    }

    console.log(`Error`);

    return Promise.reject(error);

  });

export var suspensionInterceptor = axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response.status === 451) {
      alert(`Account suspended`);
      localStorage.clear();
      window.location.reload();
    }

    return Promise.reject(error);

  });


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authRequest: AuthenticationRequest = new AuthenticationRequest;
  private authUser: User = new User;
  private router = inject(Router);
  private targetUrl = `http://localhost:8080/`;


  isAdmin(): boolean {
    var decoded = this.decodeToken();
    return decoded?.roles?.includes('ROLE_ADMIN') ?? false;
  }


  isUser(): boolean {
    var decoded = this.decodeToken();
    return decoded?.roles?.includes('ROLE_USER') ?? false;
  }

  isAuthenticated() {
    var returnValue = false;
    var authToken = localStorage.getItem('authToken');
    if (authToken) {
      returnValue = true;
    }

    return returnValue;
  }

  setAuthRequest(tempRequest: AuthenticationRequest) {
    this.authRequest = tempRequest;
  }

  logout() {
    localStorage.clear();
    window.location.reload();
  }

  redirectRegister() {
    this.router.navigate([`registration`]);
  }

  getTargetUrl(): string {
    return this.targetUrl;
  }

  decodeToken() {
    var token = localStorage.getItem(`authToken`);
    if (token) {
      const base64Url = token.split(`.`)[1]; // Uzimamo payload deo tokena
      var base64 = decodeURIComponent(atob(base64Url)); // Dekodiramo ga
      return JSON.parse(base64); // Parsiramo JSON sadržaj tokena
    }
    return null;
  }



}
