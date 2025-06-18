package com.radovan.play.repositories;

import com.radovan.play.entity.ProductCategoryEntity;

import java.util.List;
import java.util.Optional;

public interface ProductCategoryRepository {

    List<ProductCategoryEntity> listAll();

    void deleteById(Integer categoryId);

    Optional<ProductCategoryEntity> findById(Integer categoryId);

    ProductCategoryEntity save(ProductCategoryEntity categoryEntity);

    ProductCategoryEntity saveAndFlush(ProductCategoryEntity categoryEntity);

    Optional<ProductCategoryEntity> findByName(String name);
}