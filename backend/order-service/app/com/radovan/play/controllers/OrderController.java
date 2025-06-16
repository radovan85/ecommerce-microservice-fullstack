package com.radovan.play.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.play.brokers.OrderNatsSender;
import com.radovan.play.exceptions.DataNotValidatedException;
import com.radovan.play.security.JwtAuthAction;
import com.radovan.play.security.RoleSecured;
import com.radovan.play.services.*;
import jakarta.inject.Inject;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;

@With(JwtAuthAction.class)
public class OrderController extends Controller {


    private OrderService orderService;
    private OrderAddressService orderAddressService;
    private OrderItemService orderItemService;
    private OrderNatsSender orderNatsSender;
    private FormFactory formFactory;

    @Inject
    private void initialize(OrderService orderService, OrderAddressService orderAddressService, OrderItemService orderItemService, OrderNatsSender orderNatsSender, FormFactory formFactory) {
        this.orderService = orderService;
        this.orderAddressService = orderAddressService;
        this.orderItemService = orderItemService;
        this.orderNatsSender = orderNatsSender;
        this.formFactory = formFactory;
    }

    @RoleSecured({"ROLE_USER"})
    public Result provideMyAddress(Http.Request request) {
        JsonNode customer = orderNatsSender.retrieveCustomer(extractJwtToken(request));
        JsonNode customerData = customer.get("customer");
        Integer addressId = customerData.get("shippingAddressId").asInt();
        JsonNode address = orderNatsSender.retrieveShippingAddress(addressId, extractJwtToken(request));
        return ok(Json.toJson(address));
    }

    @RoleSecured({"ROLE_USER"})
    public Result confirmShipping(Http.Request request) {
        String jwtToken = extractJwtToken(request);
        /*
        Form<JsonNode> form = formFactory.form(JsonNode.class).bindFromRequest(request);
        if (form.hasErrors()) {
            throw new DataNotValidatedException("Product data is not valid!");
        }

        JsonNode address = form.get();

         */

        JsonNode addressData = request.body().asJson();
        if (addressData == null || !addressData.has("address") || !addressData.has("city")) {
            throw new DataNotValidatedException("Invalid shipping address data!");
        }

        // Dohvati korisnika i adresu
        JsonNode customer = orderNatsSender.retrieveCustomer(jwtToken);
        JsonNode customerNode = customer.get("customer"); // Prvo dohvatamo customer objekat
        int addressId = customerNode.get("shippingAddressId").asInt(); // Sada uzimamo shippingAddressId

        // Ažuriraj adresu preko NATS-a
        JsonNode updatedAddress = orderNatsSender.updateShippingAddress(addressId, addressData, jwtToken);

        return ok(Json.toJson(updatedAddress));
    }



    @RoleSecured({"ROLE_USER"})
    public Result placeOrder(Http.Request request) {
        orderService.addOrder(request);
        return ok("Your order has been submitted without any problems!");
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result getAllOrders() {
        return ok(Json.toJson(orderService.listAll()));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result getOrderDetails(Integer orderId) {
        return ok(Json.toJson(orderService.getOrderById(orderId)));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result getAllAddresses() {
        return ok(Json.toJson(orderAddressService.listAll()));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result getAllItems(Integer orderId) {
        return ok(Json.toJson(orderItemService.listAllByOrderId(orderId)));
    }

    @RoleSecured({"ROLE_ADMIN"})
    public Result deleteOrder(Integer orderId) {
        orderService.deleteOrder(orderId);
        return ok("The order with id " + orderId + " has been permanently deleted!");
    }

    private String extractJwtToken(Http.Request request) {
        return request.header("Authorization")
                .map(header -> header.replace("Bearer ", "").trim())
                .orElseThrow(() -> new RuntimeException("Missing authorization token"));
    }

}
