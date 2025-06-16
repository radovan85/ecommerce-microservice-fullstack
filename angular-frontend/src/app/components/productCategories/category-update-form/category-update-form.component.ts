import { AfterViewInit, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import axios from 'axios';
import { ProductCategory } from '../../../classes/product-category';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ValidationService } from '../../../services/validation.service';


@Component({
  selector: 'app-category-update-form',
  standalone: true,
  imports: [],
  templateUrl: './category-update-form.component.html',
  styleUrl: './category-update-form.component.css'
})
export class CategoryUpdateFormComponent implements OnInit, AfterViewInit {


  private category: ProductCategory = new ProductCategory;
  private route = inject(ActivatedRoute);
  private categoryService = inject(ProductCategoryService);
  private validationService = inject(ValidationService);

  ngOnInit(): void {
    this.getCategoryDetails(this.route.snapshot.params[`categoryId`]);
  }

  ngAfterViewInit() {
    // Get the form element
    var form = document.getElementById('categoryForm') as HTMLFormElement;

    // Add an event listener for form submission
    form.addEventListener(`submit`, async (event) => {
      event.preventDefault(); // Prevent the default form submission

      // Serialize the form data manually
      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });

      if (this.validationService.validateProductCategory()) {
        await axios.put(`${this.categoryService.getTargetUrl()}/${this.category.productCategoryId}`, {
          name: serializedData[`categoryName`]
        })
          .then(() => {
            this.categoryService.redirectAllCategories();
          })

          .catch((error) => {

            if (error.response.status === 409) {
              alert(error.response.data);
            }

            else {
              console.log(error);
            }
          });
      }
    });
  }


  getCategoryDetails(categoryId: any) {
    this.categoryService.getCategoryDetails(categoryId)
      .then((response) => {
          this.category = response.data;
      })
  }

  public getCategory(): ProductCategory {
    return this.category;
  }





}
