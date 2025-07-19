import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Cart } from '../../../classes/cart';
import { CartItem } from '../../../classes/cart-item';
import { Product } from '../../../classes/product';
import { ShippingAddress } from '../../../classes/shipping-address';
import { CartService } from '../../../services/cart.service';
import { OrderService } from '../../../services/order.service';
import { ProductService } from '../../../services/product.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-order-confirmation',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-confirmation.component.html',
  styleUrl: './order-confirmation.component.css',
})
export class OrderConfirmationComponent implements OnInit {
  private orderService = inject(OrderService);
  private address: ShippingAddress = new ShippingAddress();
  private allItems: CartItem[] = [];
  private allProducts: Product[] = [];
  private productService = new ProductService();
  private cartService = inject(CartService);
  private cart = new Cart();

  ngOnInit(): void {
    Promise.all([
      this.provideMyAddress(),
      this.listAllProducts(),
      this.listAllItems(),
      this.getMyCart(),
    ]).catch((error) => {
      console.log(`Error loading functions! ${error}`);
    });
  }

  provideMyAddress(): Promise<any> {
    return this.orderService.provideMyAddress().then((response) => {
      this.address = response.data;
    });
  }

  getAllItems(): CartItem[] {
    return this.allItems;
  }

  getAddress(): ShippingAddress {
    return this.address;
  }

  listAllProducts(): Promise<any> {
    return this.productService.collectAllProducts().then((response) => {
      this.allProducts = response.data;
    });
  }

  getProduct(productId: any): Product {
    var tempProduct = new Product();
    this.allProducts.forEach((product) => {
      if (product.productId === productId) {
        tempProduct = product;
      }
    });

    return tempProduct;
  }

  listAllItems(): Promise<any> {
    return this.cartService.collectMyItems().then((response) => {
      this.allItems = response.data;
    });
  }

  getMyCart(): Promise<any> {
    return this.cartService.getMyCart().then((response) => {
      this.cart = response.data;
    });
  }

  getCart(): Cart {
    return this.cart;
  }

  redirectPhoneConfirmation() {
    this.orderService.redirectPhoneConfirmation();
  }

  placeOrder() {
    this.orderService
      .placeOrder()
      .then(() => {
        this.orderService.redirectOrderCompleted();
      })

      .catch((error) => {
        if (error.response.status === 406) {
          alert(error.response.data);
        } else {
          alert(`Failed!`);
        }
      });
  }
}
