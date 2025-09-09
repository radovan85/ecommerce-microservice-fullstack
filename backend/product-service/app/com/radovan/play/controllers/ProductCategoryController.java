package com.radovan.play.controllers;

import com.radovan.play.dto.ProductCategoryDto;
import com.radovan.play.exceptions.DataNotValidatedException;
import com.radovan.play.security.JwtAuthAction;
import com.radovan.play.security.RoleSecured;
import com.radovan.play.services.ProductCategoryService;
import com.radovan.play.utils.TokenUtils;
import jakarta.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

@With(JwtAuthAction.class)
public class ProductCategoryController extends Controller {

    private FormFactory formFactory;
    private ProductCategoryService categoryService;

    @Inject
    private void initialize(FormFactory formFactory,ProductCategoryService categoryService){
        this.formFactory = formFactory;
        this.categoryService = categoryService;
    }


    public Result getAllCategories(){
        return ok(Json.toJson(categoryService.listAll()));
    }


    public Result getCategoryDetails(Integer categoryId){
        return ok(Json.toJson(categoryService.getCategoryById(categoryId)));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result saveCategory(Http.Request request){
        Form<ProductCategoryDto> categoryForm = formFactory.form(ProductCategoryDto.class).bindFromRequest(request);
        if (categoryForm.hasErrors()) {
            throw new DataNotValidatedException("Product category data is not valid!");
        }

        ProductCategoryDto category = categoryForm.get();
        ProductCategoryDto storedCategory = categoryService.addCategory(category);
        return ok("The category with id " + storedCategory.getProductCategoryId() + " has been stored!");
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result updateCategory(Http.Request request,Integer categoryId){
        Form<ProductCategoryDto> categoryForm = formFactory.form(ProductCategoryDto.class).bindFromRequest(request);
        if (categoryForm.hasErrors()) {
            throw new DataNotValidatedException("Product category data is not valid!");
        }

        ProductCategoryDto category = categoryForm.get();
        ProductCategoryDto updatedCategory = categoryService.updateCategory(category,categoryId);
        return ok("The category with id " + updatedCategory.getProductCategoryId() + " has been updated without any issues");
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result deleteCategory(Http.Request request, Integer categoryId){
        categoryService.deleteCategory(categoryId, TokenUtils.provideToken(request));
        return ok("The category with id " + categoryId + " has been permanently deleted!");
    }
}
