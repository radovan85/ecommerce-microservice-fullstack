import { CommonModule } from '@angular/common';
import {  Component,  OnInit } from '@angular/core';


@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit {

  images = [
    { src: 'assets/images/sony-xperia-xa2-plus-banner.jpg', alt: 'Image 1' },
    { src: 'assets/images/lenovop70-a.jpg', alt: 'Image 2' },
    { src: 'assets/images/lenovo-tab-p11.jpg', alt: 'Image 3' },
    { src: 'assets/images/sony-playstation-5.jpg', alt: 'Image 4' }
    // Add more images as needed
  ];
  currentIndex = 0;
  intervalId: any;

  ngOnInit(): void {
    this.startSlider();
  }

  startSlider(): void {
    this.intervalId = setInterval(() => {
      this.moveSlide('next');
    }, 5000);
  }

  moveSlide(direction: string): void {
    if (direction === 'next') {
      this.currentIndex = (this.currentIndex + 1) % this.images.length;
    } else {
      this.currentIndex = (this.currentIndex - 1 + this.images.length) % this.images.length;
    }
  }

  currentSlide(index: number): void {
    this.currentIndex = index;
  }

  ngOnDestroy(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }
}





