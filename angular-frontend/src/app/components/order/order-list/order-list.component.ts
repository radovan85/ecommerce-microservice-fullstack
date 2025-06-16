import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Customer } from '../../../classes/customer';
import { Order } from '../../../classes/order';
import { User } from '../../../classes/user';
import { CustomerService } from '../../../services/customer.service';
import { OrderService } from '../../../services/order.service';
import { UserService } from '../../../services/user.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-order-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './order-list.component.html',
  styleUrl: './order-list.component.css'
})
export class OrderListComponent implements OnInit {

  private allOrders: Order[] = [];
  private paginatedOrders: Order[] = [];
  private allCustomers: Customer[] = [];
  private allUsers: User[] = [];
  private orderService = inject(OrderService);
  private userService = inject(UserService);
  private customerService = inject(CustomerService);
  private pageSize = 15;
  private currentPage = 1;
  private totalPages = 1;

  ngOnInit(): void {
    Promise.all([
      this.listAllOrders(),
      this.listAllCustomers(),
      this.listAllUsers()
    ])

      .catch((error) => {
        console.log(`Error loading functions  ${error}`);
      })
  }

  listAllCustomers(): Promise<any> {
    return new Promise(() => {
      this.customerService.collectAllCustomers()
        .then((response) => {
          this.allCustomers = response.data;
        })
    })
  }

  listAllUsers(): Promise<any> {
    return new Promise(() => {
      this.userService.collectAllUsers()
        .then((response) => {
          this.allUsers = response.data;
        })
    })
  }

  listAllOrders(): Promise<any> {
    return new Promise(() => {
      this.orderService.collectAllOrders()
        .then((response) => {
          this.allOrders = response.data;
          this.totalPages = Math.ceil(this.allOrders.length / this.pageSize);
          this.setPage(1);
        })
    })
  }

  getAllCustomers(): Customer[] {
    return this.allCustomers;
  }

  getAllOrders(): Order[] {
    return this.allOrders;
  }

  getUserById(userId: any): User {
    var returnValue: User = new User;
    this.allUsers.forEach((tempUser) => {
      if (tempUser.id === userId) {
        returnValue = tempUser;
      }
    })
    return returnValue
  }

  setPage(page: number) {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.paginatedOrders = this.allOrders.slice((page - 1) * this.pageSize, page * this.pageSize);
  }

  nextPage() {
    this.setPage(this.currentPage + 1);
  }

  prevPage() {
    this.setPage(this.currentPage - 1);
  }

  getPageSize() {
    return this.pageSize;
  }

  getCurrentPage() {
    return this.currentPage;
  }

  getTotalPages() {
    return this.totalPages;
  }

  getPaginatedOrders(): Order[] {
    return this.paginatedOrders;
  }



}