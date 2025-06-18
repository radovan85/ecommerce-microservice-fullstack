package com.radovan.play.services;

import com.radovan.play.dto.ProductDto;
import play.mvc.Http;

import java.util.List;

public interface ProductService {

    ProductDto addProduct(ProductDto product);

    ProductDto getProductById(Integer productId);

    ProductDto updateProduct(ProductDto product, Integer productId);

    void deleteProduct(Integer productId);

    List<ProductDto> listAll();

    List<ProductDto> listAllByCategoryId(Integer categoryId);

    void deleteProductsByCategoryId(Integer categoryId);
}