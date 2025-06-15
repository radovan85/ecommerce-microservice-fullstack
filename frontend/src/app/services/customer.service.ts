import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  private targetUrl = `http://localhost:8080/api/customers`;
  private router = inject(Router);

  getCurrentCustomer() {
    return axios.get(`${this.targetUrl}/getCurrentCustomer`);
  }

  redirectAllCustomers() {
    this.router.navigate([`customers`]);
  }

  collectAllCustomers() {
    return axios.get(`${this.targetUrl}`);
  }

  getCustomerDetails(customerId: any) {
    return axios.get(`${this.targetUrl}/${customerId}`);
  }

  async deleteCustomer(customerId: any) {
    return await axios.delete(`${this.targetUrl}/${customerId}`);
  }

  async suspendCustomer(customerId: any) {
    return await axios.put(`${this.targetUrl}/suspend/${customerId}`);
  }

  async reactivateCustomer(customerId: any) {
    return await axios.put(`${this.targetUrl}/reactivate/${customerId}`);
  }

  getTargetUrl() {
    return this.targetUrl;
  }
}
