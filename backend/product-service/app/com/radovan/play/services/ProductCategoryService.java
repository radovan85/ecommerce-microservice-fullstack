package com.radovan.play.services;


import com.radovan.play.dto.ProductCategoryDto;

import java.util.List;

public interface ProductCategoryService {

    ProductCategoryDto addCategory(ProductCategoryDto category);

    ProductCategoryDto getCategoryById(Integer categoryId);

    ProductCategoryDto updateCategory(ProductCategoryDto category, Integer categoryId);

    void deleteCategory(Integer categoryId,String jwtToken);

    List<ProductCategoryDto> listAll();
}