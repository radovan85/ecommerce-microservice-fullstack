package com.radovan.play.controllers;

import com.radovan.play.brokers.ProductNatsSender;
import com.radovan.play.dto.ProductDto;
import com.radovan.play.dto.ProductImageDto;
import com.radovan.play.exceptions.DataNotValidatedException;
import com.radovan.play.exceptions.FileUploadException;
import com.radovan.play.security.JwtAuthAction;
import com.radovan.play.security.RoleSecured;
import com.radovan.play.services.ProductImageService;
import com.radovan.play.services.ProductService;
import jakarta.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

import java.util.List;

@With(JwtAuthAction.class)
public class ProductController extends Controller {

    private ProductService productService;
    private ProductImageService imageService;

    private FormFactory formFactory;
    private ProductNatsSender productNatsSender;

    @Inject
    private void initialize(ProductService productService, ProductImageService imageService, FormFactory formFactory, ProductNatsSender productNatsSender) {
        this.productService = productService;
        this.imageService = imageService;
        this.formFactory = formFactory;
        this.productNatsSender = productNatsSender;
    }

    public Result listAllProducts(){
        List<ProductDto> allProducts = productService.listAll();
        return ok(Json.toJson(allProducts));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result createProduct(Http.Request request) {
        Form<ProductDto> form = formFactory.form(ProductDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            throw new DataNotValidatedException("Product data is not valid!");
        }
        ProductDto productDto = form.get();
        ProductDto createdProduct = productService.addProduct(productDto);
        return ok("Product with id " + createdProduct.getProductId() + " has been created");
    }

    public Result getProductById(Integer id) {
        ProductDto product = productService.getProductById(id);
        return ok(Json.toJson(product));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result updateProduct(Http.Request request, Integer id) {
        Form<ProductDto> form = formFactory.form(ProductDto.class).bindFromRequest(request);
        if (form.hasErrors()) {
            throw new DataNotValidatedException("Product data is not valid!");
        }
        ProductDto productDto = form.get();
        ProductDto updatedProduct = productService.updateProduct(productDto, id);
        String jwtToken = request.headers().get("Authorization").orElse(null);
        System.out.println("JWT Token: " + jwtToken);
        productNatsSender.sendCartUpdateRequest(id,jwtToken);
        return ok("Product with id " + updatedProduct.getProductId() + " has been updated!");
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result deleteProduct(Integer id) {
        productService.deleteProduct(id);
        return ok("Product deleted successfully");
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result storeImage(Http.Request request, Integer productId) {
        Http.MultipartFormData<play.libs.Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<play.libs.Files.TemporaryFile> filePart = body.getFile("file");

        if (filePart != null) {
            try {
                ProductImageDto imageDto = imageService.addImage(filePart, productId);
                return ok("Image for product with id " + productId + " has been uploaded");
            } catch (FileUploadException e) {
                return badRequest("Failed to upload image: " + e.getMessage());
            }
        } else {
            return badRequest("Missing file");
        }
    }


    public Result getAllImages(){
        System.out.println("Get all images pozvan");
        List<ProductImageDto> allImages = imageService.listAll();
        return ok(Json.toJson(allImages));
    }
}