import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import axios from 'axios';
import { Product } from '../../../classes/product';
import { ProductCategory } from '../../../classes/product-category';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductService } from '../../../services/product.service';
import { ValidationService } from '../../../services/validation.service';

@Component({
  selector: 'app-product-update-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-update-form.component.html',
  styleUrl: './product-update-form.component.css',
})
export class ProductUpdateFormComponent implements OnInit, AfterViewInit {
  private validationService = inject(ValidationService);
  private allCategories: ProductCategory[] = [];
  private currentProduct: Product = new Product();
  private productService = inject(ProductService);
  private route = inject(ActivatedRoute);
  private categoryService = inject(ProductCategoryService);

  ngOnInit(): void {
    Promise.all([
      this.getProductDetails(this.route.snapshot.params[`productId`]),
      this.listAllCategories(),
    ])
    .catch((error) => {
      console.log(`Error loading functions ${error}`);
    });
  }

  ngAfterViewInit(): void {
    this.executeProductForm();
  }

  validateNumber(event: any) {
    return this.validationService.validateNumber(event);
  }

  public getAllCategories(): ProductCategory[] {
    return this.allCategories;
  }

  getProductDetails(productId: any): Promise<any> {
    return this.productService.getProductDetails(productId).then((response) => {
      this.currentProduct = response.data;
    });
  }

  executeProductForm() {
    var form = document.getElementById('productForm') as HTMLFormElement;

    form.addEventListener('submit', async (event) => {
      event.preventDefault();

      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });

      if (this.validationService.validateProduct()) {
        await axios
          .put(
            `${this.productService.getTargetUrl()}/${
              this.currentProduct.productId
            }`,
            {
              productName: serializedData[`product-name`],
              productDescription: serializedData[`product-description`],
              productBrand: serializedData[`product-brand`],
              productModel: serializedData[`product-model`],
              productPrice: serializedData[`product-price`],
              unitStock: serializedData[`unit-stock`],
              discount: serializedData[`discount`],
              productCategoryId: serializedData[`categoryId`],
            }
          )
          .then(() => {
            this.productService.redirectAllProducts();
          })

          .catch((error) => {
            console.log(error);
            alert(`Failed`);
          });
      }
    });
  }

  public getCurrentProduct(): Product {
    return this.currentProduct;
  }

  listAllCategories(): Promise<any> {
    return this.categoryService.collectAllCategories().then((response) => {
      this.allCategories = response.data;
    });
  }
}
