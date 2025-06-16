import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { ProductCategory } from '../../../classes/product-category';
import { AuthService } from '../../../services/auth.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { NavbarComponent } from '../../navbar/navbar.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css'],
  standalone: true,
  imports: [CommonModule, RouterLink]
})
export class CategoryListComponent implements OnInit {

  private allCategories: ProductCategory[] = [];
  private paginatedCategories: ProductCategory[] = [];
  private categoryService = inject(ProductCategoryService);
  private pageSize = 5;
  private currentPage = 1;
  private totalPages = 1;
  private hasAuthorityAdmin: boolean = false;
  private authService = inject(AuthService);

  ngOnInit(): void {
    Promise.all([
      this.listAllCategories(),
      this.hasAuthorityAdmin = this.authService.isAdmin()
    ])
  }

  listAllCategories() {
    this.categoryService.collectAllCategories()
      .then((response) => {
        this.allCategories = response.data;
        this.totalPages = Math.ceil(this.allCategories.length / this.pageSize);
        this.setPage(1);
      })
  }

  setPage(page: number) {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
    this.paginatedCategories = this.allCategories.slice((page - 1) * this.pageSize, page * this.pageSize);
  }

  nextPage() {
    this.setPage(this.currentPage + 1);
  }

  prevPage() {
    this.setPage(this.currentPage - 1);
  }

  deleteCategory(categoryId: any) {
    if (confirm(`Remove this category?\nIt will affect all related data!`)) {
      this.categoryService.deleteCategory(categoryId)
        .then(() => {
          this.allCategories = this.allCategories.filter(
            (tempCategory) => tempCategory.productCategoryId !== categoryId
          );

          this.totalPages = Math.max(
            1,
            Math.ceil(this.allCategories.length / this.pageSize)
          );

          if (
            (this.currentPage - 1) * this.pageSize >=
            this.allCategories.length &&
            this.currentPage > 1
          ) {
            this.currentPage--;
          }

          this.setPage(this.currentPage);
          this.paginatedCategories = [
            ...this.allCategories.slice(
              (this.currentPage - 1) * this.pageSize,
              this.currentPage * this.pageSize
            ),
          ];
        });
    }
  }

  public getHasAuthorityAdmin(): boolean {
    return this.hasAuthorityAdmin;
  }

  public getPaginatedCategories(): ProductCategory[] {
    return this.paginatedCategories;
  }

  public getCurrentPage(): number {
    return this.currentPage;
  }

  public getTotalPages(): number {
    return this.totalPages;
  }

  public getAllCategories(): ProductCategory[] {
    return this.allCategories;
  }

}

