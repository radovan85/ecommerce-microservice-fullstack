import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Product } from '../../../classes/product';
import { ProductCategory } from '../../../classes/product-category';
import { ProductImage } from '../../../classes/product-image';
import { AuthService } from '../../../services/auth.service';
import { CartService } from '../../../services/cart.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductService } from '../../../services/product.service';
import { Location } from '@angular/common';

@Component({
  selector: 'app-product-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './product-details.component.html',
  styleUrl: './product-details.component.css'
})
export class ProductDetailsComponent implements OnInit {


  private product: Product = new Product;
  private hasAuthorityUser = false;
  private hasAuthorityAdmin = false;
  private allCategories: ProductCategory[] = [];
  private categoryService = inject(ProductCategoryService);
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private authService = inject(AuthService);
  private allImages: ProductImage[] = [];
  private cartService = inject(CartService);
  private location = inject(Location);



  ngOnInit(): void {
    Promise.all([
      this.listAllCategories(),
      this.getProductDetails(this.route.snapshot.params[`productId`]),
      this.hasAuthorityAdmin = this.authService.isAdmin(),
      this.hasAuthorityUser = this.authService.isUser(),
      this.listAllImages()
    ])

      .then((error) => {
        console.log(`Error loading functions ${error}`);
      })
  }

  listAllCategories(): Promise<any> {
    return new Promise(() => {
      this.categoryService.collectAllCategories()
        .then((response) => {
          this.allCategories = response.data;
        })
    })
  }

  listAllImages(): Promise<any> {
    return new Promise(() => {
      this.productService.collectAllImages()
        .then((response) => {
          this.allImages = response.data;
        })
    })
  }

  getProductDetails(productId: any): Promise<any> {
    return new Promise(() => {
      this.productService.getProductDetails(productId)
        .then((response) => {
          this.product = response.data;
        })
    })
  }

  public getAllCategories(): ProductCategory[] {
    return this.allCategories;
  }

  public getProduct(): Product {
    return this.product;
  }

  public getHasAuthorityUser(): boolean {
    return this.hasAuthorityUser;
  }

  public getHasAuthorityAdmin(): boolean {
    return this.hasAuthorityAdmin;
  }



  deleteProduct(productId: any) {
    if (confirm(`Remove this product?It will affect all related data!`)) {
      this.productService.deleteProduct(productId)
        .then(() => {
          this.productService.redirectAllProducts();
        })

        .catch(() => {
          alert(`Failed!`);
        })
    }
  }

  getProductImage(product: Product): string {
    var image = this.allImages.find(img => img.productId === product.productId);
    if (image) {
      return `data:image/jpg;base64,${image.data}`;
    } else {
      // Return URL for default image
      return `https://t4.ftcdn.net/jpg/04/99/93/31/360_F_499933117_ZAUBfv3P1HEOsZDrnkbNCt4jc3AodArl.jpg`;
    }
  }

  getProductCategory(product: Product): ProductCategory {
    var category = this.allCategories.find(tempCategory => tempCategory.productCategoryId === product.productCategoryId);
    return category || new ProductCategory;
  }

  addToCart(productId: any) {
    this.cartService.addToCart(productId)
      .then(() => {
        alert(`The item has been added to your cart!`);
      })

      .catch((error) => {
        if (error.response.status === 406) {
          alert(error.response.data);
        } else {
          console.log(error);
          alert(`Error!`);
        }
      })
  }

  goBack() {
    this.location.back();
  }

}