import { Component, ElementRef, inject, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import axios from 'axios';
import { Product } from '../../../classes/product';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-image-form',
  standalone: true,
  imports: [],
  templateUrl: './image-form.component.html',
  styleUrl: './image-form.component.css'
})
export class ImageFormComponent implements OnInit {

  @ViewChild('imageInput') imageInput: ElementRef | undefined;
  private currentProduct = new Product;
  private route = inject(ActivatedRoute);
  private productService = inject(ProductService);
  private isFileSelected: boolean = false;

  ngOnInit(): void {

    this.getProductDetails(this.route.snapshot.params[`productId`]);
    const form = document.querySelector('form');
    form?.addEventListener(`submit`, this.uploadImage.bind(this));

  }

  getProductDetails(productId: any): Promise<any> {
    return new Promise(() => {
      this.productService.getProductDetails(productId)
        .then((response) => {
            this.currentProduct = response.data;
          })
    })
  }

  getCurrentProduct(): Product {
    return this.currentProduct;
  }

  uploadImage(event: Event): void {
    event.preventDefault(); // Prevent the default form submission

    if (!this.imageInput || !this.imageInput.nativeElement.files[0]) {
      console.error('No file selected.');
      return;
    }

    const file: File = this.imageInput.nativeElement.files[0];
    const formData = new FormData();
    formData.append(`file`, file, file.name);

    axios.post(`${this.productService.getTargetUrl()}/storeImage/${this.currentProduct.productId}`, formData)
      .then((response) => {
        this.productService.redirectAllProducts();
        // Handle success response
      })
      .catch((error) => {
        alert(`Error uploading image:`);
        console.log(error);
        // Handle error response
      });
  }

  onFileSelected(): void {
    if (this.imageInput && this.imageInput.nativeElement.files[0]) {
      this.isFileSelected = true;
    } else {
      this.isFileSelected = false;
    }
  }

  getisFileSelected(): boolean {
    return this.isFileSelected;
  }


}
