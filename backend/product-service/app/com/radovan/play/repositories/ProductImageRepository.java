package com.radovan.play.repositories;

import com.radovan.play.entity.ProductImageEntity;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository {

    ProductImageEntity save(ProductImageEntity imageEntity);

    Optional<ProductImageEntity> findByProductId(Integer productId);

    List<ProductImageEntity> listAll();

    void deleteById(Integer imageId);

    Optional<ProductImageEntity> findById(Integer imageId);
}
