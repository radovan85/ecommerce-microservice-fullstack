import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import axios from 'axios';
import { Customer } from '../../../classes/customer';
import { RegistrationForm } from '../../../classes/registration-form';
import { ShippingAddress } from '../../../classes/shipping-address';
import { User } from '../../../classes/user';
import { ValidationService } from '../../../services/validation.service';

@Component({
  selector: 'app-registration-form',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './registration-form.component.html',
  styleUrl: './registration-form.component.css'
})
export class RegistrationFormComponent implements AfterViewInit {

  validationService = inject(ValidationService);
  router = inject(Router);

  ngAfterViewInit() {
    // Get the form element
    var form = document.getElementById(`registrationForm`) as HTMLFormElement;

    // Add an event listener for form submission
    form.addEventListener(`submit`, async (event) => {
      event.preventDefault(); // Prevent the default form submission

      // Serialize the form data manually
      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });

      // Create user, customer, and shipping address objects
      var user = new User();
      user.firstName = serializedData[`firstName`];
      user.lastName = serializedData[`lastName`];
      user.email = serializedData[`email`];
      user.password = serializedData[`password`];

      var customer = new Customer();
      customer.customerPhone = serializedData[`phone`];

      var shippingAddress = new ShippingAddress();
      shippingAddress.address = serializedData[`address`];
      shippingAddress.city = serializedData[`city`];
      shippingAddress.state = serializedData[`state`];
      shippingAddress.country = serializedData[`country`];
      shippingAddress.postcode = serializedData[`postcode`];

      var regForm = new RegistrationForm();
      regForm.user = user;
      regForm.customer = customer;
      regForm.shippingAddress = shippingAddress;

      if (this.validationService.validateRegForm()) {

        // Make the API call
        await axios.post(`http://localhost:8080/api/customers/register`, {
          user: {
            firstName: serializedData[`firstName`],
            lastName: serializedData[`lastName`],
            email: serializedData[`email`],
            password: serializedData[`password`]
          },
          customer: {
            customerPhone: serializedData[`phone`]
          },
          shippingAddress: {
            address: serializedData[`address`],
            city: serializedData[`city`],
            state: serializedData[`state`],
            country: serializedData[`country`],
            postcode: serializedData[`postcode`]
          }
        })
          .then(() => {
            this.router.navigate([`registration/completed`]);
          })
          .catch((error) => {
            if (error.response.status === 409) {
              this.router.navigate([`registration/failed`]);
            } else {
              alert(`Failed!`);
            }
          });

      }
    });
  }
}


