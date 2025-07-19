import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Product } from '../../../classes/product';
import { ProductImage } from '../../../classes/product-image';
import { AuthService } from '../../../services/auth.service';
import { ProductService } from '../../../services/product.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-list.component.html',
  styleUrl: './product-list.component.css',
})
export class ProductListComponent implements OnInit {
  private hasAuthorityAdmin = false;
  private hasAuthorityUser = false;
  private paginatedProducts: Product[] = [];
  private pageSize = 9;
  private currentPage = 1;
  private totalPages = 1;
  private allProducts: Product[] = [];
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private allImages: ProductImage[] = [];

  ngOnInit(): void {
    Promise.all([
      this.listAllProducts(),
      (this.hasAuthorityAdmin = this.authService.isAdmin()),
      this.listAllImages(),
    ])
    .catch((error) => {
      console.log(`Error loading the functions ${error}`);
    });
  }

  getHasAuthorityAdmin(): boolean {
    return this.hasAuthorityAdmin;
  }

  getHasAuthorityUser(): boolean {
    return this.hasAuthorityUser;
  }

  getPaginatedProducts(): Product[] {
    return this.paginatedProducts;
  }

  getPageSize(): number {
    return this.pageSize;
  }

  getCurrentPage(): number {
    return this.currentPage;
  }

  getTotalPages(): number {
    return this.totalPages;
  }

  getAllProducts(): Product[] {
    return this.allProducts;
  }

  setPage(page: number) {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.paginatedProducts = this.allProducts.slice(
      (page - 1) * this.pageSize,
      page * this.pageSize
    );
  }

  nextPage() {
    this.setPage(this.currentPage + 1);
  }

  prevPage() {
    this.setPage(this.currentPage - 1);
  }

  listAllProducts(): Promise<any> {
    return this.productService.collectAllProducts().then((response) => {
      this.allProducts = response.data;
      this.totalPages = Math.ceil(this.allProducts.length / this.pageSize);
      this.setPage(1);
    });
  }

  listAllImages(): Promise<any> {
    return this.productService.collectAllImages().then((response) => {
      this.allImages = response.data;
    });
  }

  getAllImages(): ProductImage[] {
    return this.allImages;
  }

  getProductImage(product: Product): string {
    var image = this.allImages.find(
      (img) => img.productId === product.productId
    );
    if (image) {
      return `data:image/jpg;base64,${image.data}`;
    } else {
      // Return URL for default image
      return `https://t4.ftcdn.net/jpg/04/99/93/31/360_F_499933117_ZAUBfv3P1HEOsZDrnkbNCt4jc3AodArl.jpg`;
    }
  }
}
