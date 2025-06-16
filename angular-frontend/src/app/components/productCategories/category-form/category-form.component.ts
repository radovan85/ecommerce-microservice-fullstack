import { AfterViewInit, Component, inject } from '@angular/core';
import axios from 'axios';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ValidationService } from '../../../services/validation.service';

@Component({
  selector: 'app-category-form',
  standalone: true,
  imports: [],
  templateUrl: './category-form.component.html',
  styleUrl: './category-form.component.css'
})
export class CategoryFormComponent implements AfterViewInit {

  private categoryService = inject(ProductCategoryService);
  private validationService = inject(ValidationService);

  ngAfterViewInit() {
    // Get the form element
    var form = document.getElementById('categoryForm') as HTMLFormElement;

    // Add an event listener for form submission
    form.addEventListener('submit', async (event) => {
      event.preventDefault(); // Prevent the default form submission

      // Serialize the form data manually
      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });

      if (this.validationService.validateProductCategory()) {
        await axios.post(`${this.categoryService.getTargetUrl()}`, {
          name: serializedData[`categoryName`]
        })
          .then(() => {
            this.categoryService.redirectAllCategories();
          })

          .catch((error) => {

            if (error.response.status === 409) {
              alert(error.response.data);
            } else {
              console.log(error);
            }
          });
      }
    });
  }

}
