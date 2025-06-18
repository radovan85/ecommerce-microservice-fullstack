package com.radovan.play.dto;

import play.data.validation.Constraints;

import java.io.Serializable;

public class OrderAddressDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer orderAddressId;

    @Constraints.Required
    @Constraints.MaxLength(value = 75)
    private String address;

    @Constraints.Required
    @Constraints.MaxLength(value = 40)
    private String city;

    @Constraints.Required
    @Constraints.MaxLength(value = 40)
    private String state;

    @Constraints.Required
    @Constraints.MaxLength(value = 40)
    private String country;

    @Constraints.Required
    @Constraints.MaxLength(value = 10)
    private String postcode;

    @Constraints.Required
    private Integer orderId;

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

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
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
