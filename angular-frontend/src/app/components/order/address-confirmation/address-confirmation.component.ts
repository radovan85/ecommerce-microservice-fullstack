import { AfterViewInit, Component, inject, OnInit } from '@angular/core';
import axios from 'axios';
import { ShippingAddress } from '../../../classes/shipping-address';
import { CartService } from '../../../services/cart.service';
import { OrderService } from '../../../services/order.service';
import { ValidationService } from '../../../services/validation.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-address-confirmation',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './address-confirmation.component.html',
  styleUrl: './address-confirmation.component.css',
})
export class AddressConfirmationComponent implements OnInit, AfterViewInit {
  private currentAddress: ShippingAddress = new ShippingAddress();
  private orderService = inject(OrderService);
  private validationService = inject(ValidationService);
  private cartService = inject(CartService);

  ngOnInit(): void {
    Promise.all([this.provideMyAddress()])
    .catch((error) => {
      console.log(`Error loading functions  ${error}`);
    });
  }

  ngAfterViewInit(): void {
    this.executeAddressForm();
  }

  provideMyAddress(): Promise<any> {
    return this.orderService.provideMyAddress().then((response) => {
      this.currentAddress = response.data;
    });
  }

  getCurrentAddress(): ShippingAddress {
    return this.currentAddress;
  }

  executeAddressForm() {
    var form = document.getElementById(
      `shippingAddressForm`
    ) as HTMLFormElement;
    form.addEventListener(`submit`, async (event) => {
      event.preventDefault();

      var formData = new FormData(form);
      var serializedData: { [key: string]: string } = {};
      formData.forEach((value, key) => {
        serializedData[key] = value.toString().trim();
      });

      if (this.validationService.validateShippingAddress()) {
        axios
          .put(`${this.orderService.getTargetUrl()}/confirmShippingAddress`, {
            address: serializedData[`address`],
            city: serializedData[`city`],
            state: serializedData[`state`],
            postcode: serializedData[`postcode`],
            country: serializedData[`country`],
          })

          .then(() => {
            this.orderService.redirectPhoneConfirmation();
          })

          .catch((error) => {
            console.log(error);
            alert(`Failed`);
          });
      }
    });
  }

  redirectCart() {
    this.cartService.redirectCart();
  }
}
