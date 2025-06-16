package com.radovan.play.dto;

import play.data.validation.Constraints;

import java.io.Serializable;
import java.util.List;

public class OrderDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer orderId;

    @Constraints.Required
    private Float orderPrice;

    @Constraints.Required
    private Integer cartId;

    private List<Integer> orderedItemsIds;

    @Constraints.Required
    private Integer addressId;

    private String createTime;

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Float getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(Float orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public List<Integer> getOrderedItemsIds() {
        return orderedItemsIds;
    }

    public void setOrderedItemsIds(List<Integer> orderedItemsIds) {
        this.orderedItemsIds = orderedItemsIds;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
