package com.radovan.play.services.impl;

import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.ProductCategoryDto;
import com.radovan.play.entity.ProductCategoryEntity;
import com.radovan.play.exceptions.ExistingInstanceException;
import com.radovan.play.exceptions.InstanceUndefinedException;
import com.radovan.play.repositories.ProductCategoryRepository;
import com.radovan.play.services.ProductCategoryService;
import com.radovan.play.services.ProductService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private Provider<TempConverter> tempConverterProvider;
    private Provider<ProductCategoryRepository> categoryRepositoryProvider;
    private Provider<ProductService> productServiceProvider;

    @Inject
    private void initialize(Provider<TempConverter> tempConverterProvider, Provider<ProductCategoryRepository> categoryRepositoryProvider,
                            Provider<ProductService> productServiceProvider ) {
        this.tempConverterProvider = tempConverterProvider;
        this.categoryRepositoryProvider = categoryRepositoryProvider;
        this.productServiceProvider = productServiceProvider;
    }

    @Override
    public ProductCategoryDto addCategory(ProductCategoryDto category) {
        Optional<ProductCategoryEntity> categoryOptional = categoryRepositoryProvider.get().findByName(category.getName());
        if(categoryOptional.isPresent()) {
            throw new ExistingInstanceException("This category already exists!");
        }
        ProductCategoryEntity categoryEntity = tempConverterProvider.get().categoryDtoToEntity(category);
        ProductCategoryEntity storedCategory = categoryRepositoryProvider.get().save(categoryEntity);
        return tempConverterProvider.get().categoryEntityToDto(storedCategory);
    }

    @Override
    public ProductCategoryDto getCategoryById(Integer categoryId) {
        ProductCategoryEntity categoryEntity = categoryRepositoryProvider.get().findById(categoryId)
                .orElseThrow(() -> new InstanceUndefinedException("The category has not been found"));
        return tempConverterProvider.get().categoryEntityToDto(categoryEntity);
    }

    @Override
    public ProductCategoryDto updateCategory(ProductCategoryDto category, Integer categoryId) {
        ProductCategoryDto currentCategory = getCategoryById(categoryId);
        Optional<ProductCategoryEntity> categoryOptional = categoryRepositoryProvider.get().findByName(category.getName());
        if(categoryOptional.isPresent()) {
            if(categoryOptional.get().getProductCategoryId() != categoryId) {
                throw new ExistingInstanceException("This category already exists!");
            }
        }
        category.setProductCategoryId(currentCategory.getProductCategoryId());
        ProductCategoryEntity updatedCategory = categoryRepositoryProvider.get()
                .saveAndFlush(tempConverterProvider.get().categoryDtoToEntity(category));
        return tempConverterProvider.get().categoryEntityToDto(updatedCategory);
    }

    @Override
    public void deleteCategory(Integer categoryId,String jwtToken) {
        getCategoryById(categoryId);
        productServiceProvider.get().deleteProductsByCategoryId(categoryId,jwtToken);
        categoryRepositoryProvider.get().deleteById(categoryId);
    }

    @Override
    public List<ProductCategoryDto> listAll() {
        List<ProductCategoryEntity> allCategories = categoryRepositoryProvider.get().listAll();
        return allCategories.stream().map(tempConverterProvider.get()::categoryEntityToDto).collect(Collectors.toList());
    }
}