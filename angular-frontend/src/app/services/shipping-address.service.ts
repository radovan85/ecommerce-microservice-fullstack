import { Injectable } from '@angular/core';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class ShippingAddressService {

  private targetUrl = `http://localhost:8080/api/addresses`;

  collectAllAddresses() {
    return axios.get(`${this.targetUrl}`);
  }
}