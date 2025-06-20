import { Injectable } from '@angular/core';
import axios from 'axios';


@Injectable({
  providedIn: 'root'
})
export class UserService {

  private targetUrl = `http://localhost:8080/api/auth/`;

  getCurrentUser() {
    return axios.get(`${this.targetUrl}me`);
  }

  collectAllUsers() {
    return axios.get(`${this.targetUrl}users`);
  }

  


}
