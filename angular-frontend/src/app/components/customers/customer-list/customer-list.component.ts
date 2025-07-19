import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Customer } from '../../../classes/customer';
import { User } from '../../../classes/user';
import { CustomerService } from '../../../services/customer.service';
import { UserService } from '../../../services/user.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.css',
})
export class CustomerListComponent implements OnInit {
  private allCustomers: Customer[] = [];
  private allUsers: User[] = [];
  private userService = inject(UserService);
  private customerService = inject(CustomerService);
  private paginatedCustomers: Customer[] = [];
  private pageSize = 10;
  private currentPage = 1;
  private totalPages = 1;

  ngOnInit(): void {
    Promise.all([this.listAllUsers(), this.listAllCustomers()]).catch(
      (error) => {
        console.log(`Error loading functions  ${error}`);
      }
    );
  }

  getAllCustomers(): Customer[] {
    return this.allCustomers;
  }

  getPaginatedCustomers(): Customer[] {
    return this.paginatedCustomers;
  }

  listAllCustomers(): Promise<any> {
    return this.customerService.collectAllCustomers().then((response) => {
      this.allCustomers = response.data;
      this.totalPages = Math.ceil(this.allCustomers.length / this.pageSize);
      this.setPage(1);
    });
  }

  listAllUsers(): Promise<any> {
    return this.userService.collectAllUsers().then((response) => {
      this.allUsers = response.data;
    });
  }

  retrieveUser(userId: any): User {
    var returnValue = new User();
    this.allUsers.forEach((tempUser) => {
      if (tempUser.id === userId) {
        returnValue = tempUser;
      }
    });

    return returnValue;
  }

  setPage(page: number) {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.paginatedCustomers = this.allCustomers.slice(
      (page - 1) * this.pageSize,
      page * this.pageSize
    );
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
}
