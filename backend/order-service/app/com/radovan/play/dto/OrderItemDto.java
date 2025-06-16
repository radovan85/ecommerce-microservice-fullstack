package com.radovan.play.dto;

import play.data.validation.Constraints;

import java.io.Serializable;

public class OrderItemDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer orderItemId;

    @Constraints.Required
    private Integer quantity;

    @Constraints.Required
    private Float price;

    @Constraints.Required
    private String productName;

    @Constraints.Required
    @Constraints.Min(value = 0)
    private Float productDiscount;

    @Constraints.Required
    @Constraints.Min(value=1)
    private Float productPrice;

    @Constraints.Required
    private Integer orderId;

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Float getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(Float productDiscount) {
        this.productDiscount = productDiscount;
    }

    public Float getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Float productPrice) {
        this.productPrice = productPrice;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

}