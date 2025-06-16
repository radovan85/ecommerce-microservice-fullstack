import { inject, Injectable } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';



@Injectable({
  providedIn: 'root'
})
export class ProductCategoryService {

  private router = inject(Router);
  private targetUrl = `http://localhost:8080/api/categories`;

  redirectAllCategories() {
    this.router.navigate([`categories`]);
  }

  collectAllCategories() {
    return axios.get(`${this.targetUrl}`);
  }

  async deleteCategory(categoryId: any) {
    return await axios.delete(`${this.targetUrl}/${categoryId}`);
  }

  getCategoryDetails(categoryId: any) {
    return axios.get(`${this.targetUrl}/${categoryId}`);
  }

  public getTargetUrl(): string {
    return this.targetUrl;
  }



}
