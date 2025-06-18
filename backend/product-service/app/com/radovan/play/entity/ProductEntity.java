package com.radovan.play.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class ProductEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productId;

    @Column(name = "description", nullable = false, length = 100)
    private String productDescription;

    @Column(name = "product_brand", nullable = false, length = 40)
    private String productBrand;

    @Column(name = "product_model", nullable = false, length = 40)
    private String productModel;

    @Column(name = "product_name", nullable = false, length = 40)
    private String productName;

    @Column(name = "price", nullable = false)
    private Float productPrice;

    @Column(name = "unit", nullable = false)
    private Integer unitStock;

    @Column(nullable = false)
    private Float discount;

    @OneToOne(fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "product")
    private ProductImageEntity image;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id",nullable = false)
    private ProductCategoryEntity productCategory;

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

    public ProductImageEntity getImage() {
        return image;
    }

    public void setImage(ProductImageEntity image) {
        this.image = image;
    }

    public ProductCategoryEntity getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategoryEntity productCategory) {
        this.productCategory = productCategory;
    }

}
