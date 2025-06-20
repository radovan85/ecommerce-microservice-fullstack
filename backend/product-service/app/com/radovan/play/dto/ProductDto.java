package com.radovan.play.dto;

import java.io.Serializable;


import play.data.validation.Constraints;

public class ProductDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer productId;

    @Constraints.Required
    @Constraints.MaxLength(100)
    @Constraints.MinLength(5)
    private String productDescription;

    @Constraints.Required
    @Constraints.MaxLength(40)
    @Constraints.MinLength(2)
    private String productBrand;

    @Constraints.Required
    @Constraints.MaxLength(40)
    @Constraints.MinLength(2)
    private String productModel;

    @Constraints.Required
    @Constraints.MaxLength(40)
    @Constraints.MinLength(2)
    private String productName;

    @Constraints.Required
    @Constraints.Min(1)
    private Float productPrice;

    @Constraints.Required
    @Constraints.Min(value = 0)
    private Integer unitStock;

    @Constraints.Required
    @Constraints.Min(value = 0)
    private Float discount;

    private Integer imageId;

    @Constraints.Required
    private Integer productCategoryId;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public String getProductModel() {
        return productModel;
    }

    public void setProductModel(String productModel) {
        this.productModel = productModel;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Float getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Float productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getUnitStock() {
        return unitStock;
    }

    public void setUnitStock(Integer unitStock) {
        this.unitStock = unitStock;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(Float discount) {
        this.discount = discount;
    }

    public Integer getImageId() {
        return imageId;
    }

    public void setImageId(Integer imageId) {
        this.imageId = imageId;
    }

    public Integer getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(Integer productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

}
