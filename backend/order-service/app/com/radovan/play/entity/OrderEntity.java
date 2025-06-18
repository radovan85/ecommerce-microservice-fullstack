package com.radovan.play.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class OrderEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer orderId;

    @Column(name = "order_price", nullable = false)
    private Float orderPrice;

    @Column(name = "cart_id", nullable = false)
    private Integer cartId;

    @Column(name = "created_time", nullable = false)
    private Timestamp createTime;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItemEntity> orderedItems;

    @OneToOne(orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "address_id", nullable = false)
    private OrderAddressEntity address;

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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public List<OrderItemEntity> getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(List<OrderItemEntity> orderedItems) {
        this.orderedItems = orderedItems;
    }

    public OrderAddressEntity getAddress() {
        return address;
    }

    public void setAddress(OrderAddressEntity address) {
        this.address = address;
    }

}