package com.radovan.play.entity;

import java.io.Serializable;
import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItemEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer orderItemId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Float price;

    @Column(nullable = false, name = "product_name")
    private String productName;

    @Column(nullable = false, name = "product_discount")
    private Float productDiscount;

    @Column(name = "product_price", nullable = false)
    private Float productPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

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

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

}
