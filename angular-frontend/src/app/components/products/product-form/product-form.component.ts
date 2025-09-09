import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject, OnInit } from '@angular/core';
import axios from 'axios';
import { ProductCategory } from '../../../classes/product-category';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductService } from '../../../services/product.service';
import { ValidationService } from '../../../services/validation.service';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css'
})
export class ProductFormComponent implements OnInit, AfterViewInit {


  private validationService = inject(ValidationService);
  private productService = inject(ProductService);
  private allCategories: ProductCategory[] = [];
  private categoryService = inject(ProductCategoryService);

  ngOnInit(): void {
    this.listAllCategories();
  }

  ngAfterViewInit() {
    this.executeProductForm();
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
        await axios.post(`${this.productService.getTargetUrl()}`, {
          productName: serializedData[`product-name`],
          productDescription: serializedData[`product-description`],
          productBrand: serializedData[`product-brand`],
          productModel: serializedData[`product-model`],
          productPrice: serializedData[`product-price`],
          unitStock: serializedData[`unit-stock`],
          discount: serializedData[`discount`],
          productCategoryId: serializedData[`categoryId`]
        })
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

  validateNumber(event: any) {
    return this.validationService.validateNumber(event);
  }

  listAllCategories() {
    this.categoryService.collectAllCategories()
      .then((response) => {
        this.allCategories = response.data;
      })
  }

  public getAllCategories(): ProductCategory[] {
    return this.allCategories;
  }

}