package com.radovan.play.services.impl;

import com.radovan.play.brokers.ProductNatsSender;
import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.ProductDto;
import com.radovan.play.entity.ProductEntity;
import com.radovan.play.exceptions.InstanceUndefinedException;
import com.radovan.play.repositories.ProductRepository;
import com.radovan.play.services.ProductCategoryService;
import com.radovan.play.services.ProductService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class ProductServiceImpl implements ProductService {

    private ProductRepository productRepository;
    private TempConverter tempConverter;
    private ProductCategoryService categoryService;
    private ProductNatsSender productNatsSender;

    @Inject
    private void initialize(ProductRepository productRepository, TempConverter tempConverter, ProductCategoryService categoryService, ProductNatsSender productNatsSender) {
        this.productRepository = productRepository;
        this.tempConverter = tempConverter;
        this.categoryService = categoryService;
        this.productNatsSender = productNatsSender;
    }

    @Override
    public ProductDto addProduct(ProductDto product) {
        categoryService.getCategoryById(product.getProductCategoryId());
        ProductEntity storedProduct = productRepository.save(tempConverter.productDtoToEntity(product));
        return tempConverter.productEntityToDto(storedProduct);
    }

    @Override
    public ProductDto getProductById(Integer productId) {
        ProductEntity productEntity = productRepository.findById(productId)
                .orElseThrow(() -> new InstanceUndefinedException("The product has not been found!"));
        return tempConverter.productEntityToDto(productEntity);
    }

    @Override
    public ProductDto updateProduct(ProductDto product, Integer productId,String jwtToken) {
        categoryService.getCategoryById(product.getProductCategoryId());
        ProductDto currentProduct = getProductById(productId);
        product.setProductId(currentProduct.getProductId());

        if (currentProduct.getImageId() != null) {
            product.setImageId(currentProduct.getImageId());
        }

        ProductEntity updatedProduct = productRepository.save(tempConverter.productDtoToEntity(product));

        // ✅ Šaljemo NATS poruku ka `cart-service` da ažurira sve CartItem-e povezane sa ovim proizvodom
        //natsUtils.getConnection().publish("cart.updateAllByProductId." + productId, new byte[0]);

        return tempConverter.productEntityToDto(updatedProduct);
    }


    @Override
    public void deleteProduct(Integer productId,String jwtToken) {
        getProductById(productId);

        // ✅ Šaljemo NATS poruku ka `cart-service` da ukloni sve stavke koje koriste taj proizvod
        //natsUtils.getConnection().publish("cart.removeAllByProductId." + productId, new byte[0]);
        productNatsSender.sendCartDeleteRequest(productId,jwtToken);
        productRepository.deleteById(productId);
    }




    @Override
    public List<ProductDto> listAll() {
        List<ProductEntity> allProducts = productRepository.listAll();
        return allProducts.stream().map(tempConverter::productEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<ProductDto> listAllByCategoryId(Integer categoryId) {
        List<ProductEntity> allProducts = productRepository.listAllByCategoryId(categoryId);
        return allProducts.stream().map(tempConverter::productEntityToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteProductsByCategoryId(Integer categoryId,String jwtToken) {
        List<ProductDto> allProducts = listAllByCategoryId(categoryId);
        allProducts.forEach((product) -> deleteProduct(product.getProductId(),jwtToken));
    }
}
