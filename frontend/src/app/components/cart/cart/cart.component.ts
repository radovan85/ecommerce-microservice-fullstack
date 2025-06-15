import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Cart } from '../../../classes/cart';
import { CartItem } from '../../../classes/cart-item';
import { Product } from '../../../classes/product';
import { CartService } from '../../../services/cart.service';
import { OrderService } from '../../../services/order.service';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cart.component.html',
  styleUrl: './cart.component.css'
})
export class CartComponent implements OnInit {

  private allItems: CartItem[] = [];
  private allProducts: Product[] = [];
  private productService = inject(ProductService);
  private cartService = inject(CartService);
  private cart: Cart = new Cart;
  private orderService = inject(OrderService);

  ngOnInit(): void {
    Promise.all([
      this.listMyItems(),
      this.listAllProducts(),
      this.getMyCart()
    ])

      .catch((error: any) => {
        console.log(`Error loading functions ${error}`);
      })

  }


  getAllItems(): CartItem[] {
    return this.allItems;
  }

  getMyCart(): Promise<any> {
    return new Promise(() => {
      this.cartService.getMyCart()
        .then((response) => {
          this.cart = response.data;
        })
    })
  }

  listAllProducts(): Promise<any> {
    return new Promise(() => {
      this.productService.collectAllProducts()
        .then((response) => {
          this.allProducts = response.data;
        })
    })
  }


  getProductName(productId: any): string {
    var tempProduct = this.allProducts.find(prod => prod.productId === productId);
    if (tempProduct) {
      return `${tempProduct.productName}`;
    } else {
      return ``;
    }
  }


  listMyItems(): Promise<any> {
    return new Promise(() => {
      this.cartService.collectMyItems()
        .then((response) => {
          setTimeout(() => {
            this.allItems = response.data;
          })
        })
    })
  }

  getCart(): Cart {
    return this.cart;
  }

  clearCart() {
    if (confirm(`Are you sure you want to clear your cart?`)) {
      this.cartService.clearCart()
        .then(() => {
          this.listMyItems();
          this.getMyCart();
          this.cartService.redirectCart();
        })
    }
  }

  removeItem(itemId: any) {
    if (confirm(`Remove this item?`)) {
      this.cartService.deleteItem(itemId)
        .then(() => {
          this.listMyItems();
          this.getMyCart();
          this.cartService.redirectCart();
        })
    }
  }

  redirectCheckout() {
    this.cartService.validateCart()
      .then(() => {
        this.orderService.redirectAddressConfirm();
      })

      .catch((error) => {
        if (error.response.status === 406) {
          this.cartService.redirectInvalidCart();
        } else {
          console.log(error);
          alert(`Failed!`);
        }

      })
  }
}
