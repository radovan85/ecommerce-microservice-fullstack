import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private targetUrl = `http://localhost:8080/api/cart/`;
  private router = inject(Router);


  async addToCart(productId: any) {
    return await axios.post(`${this.targetUrl}addCartItem/${productId}`);
  }

  redirectCart() {
    this.router.navigate([`cart`]);
  }

  collectMyItems() {
    return axios.get(`${this.targetUrl}getMyItems`);
  }

  getMyCart() {
    return axios.get(`${this.targetUrl}getMyCart`);
  }

  async deleteItem(itemId: any) {
    return await axios.delete(`${this.targetUrl}deleteItem/${itemId}`);
  }

  async clearCart() {
    return await axios.delete(`${this.targetUrl}clearCart`);
  }

  redirectInvalidCart() {
    this.router.navigate([`cart/invalidCart`]);
  }

  validateCart() {
    return axios.get(`${this.targetUrl}validateCart`);
  }

}
