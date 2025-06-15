package com.radovan.play.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_addresses")
public class OrderAddressEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer orderAddressId;

    @Column(nullable = false, length = 75)
    private String address;

    @Column(nullable = false, length = 40)
    private String city;

    @Column(nullable = false, length = 40)
    private String state;

    @Column(nullable = false, length = 40)
    private String country;

    @Column(name = "post_code", nullable = false, length = 10)
    private String postcode;

    @OneToOne(mappedBy = "address", fetch = FetchType.EAGER, orphanRemoval = true)
    private OrderEntity order;

    public Integer getOrderAddressId() {
        return orderAddressId;
    }

    public void setOrderAddressId(Integer orderAddressId) {
        this.orderAddressId = orderAddressId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
