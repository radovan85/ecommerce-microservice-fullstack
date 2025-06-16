package com.radovan.play.repositories;

import com.radovan.play.entity.ProductEntity;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Optional<ProductEntity> findById(Integer productId);

    ProductEntity save(ProductEntity productEntity);

    void deleteById(Integer productId);

    List<ProductEntity> listAllByCategoryId(Integer categoryId);

    List<ProductEntity> listAll();
}
