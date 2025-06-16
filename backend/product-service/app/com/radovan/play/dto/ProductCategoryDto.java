package com.radovan.play.dto;

import play.data.validation.Constraints;

import java.io.Serializable;

public class ProductCategoryDto implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Integer productCategoryId;

    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(40)
    private String name;

    public Integer getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(Integer productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
