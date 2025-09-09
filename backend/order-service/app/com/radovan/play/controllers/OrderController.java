package com.radovan.play.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.radovan.play.brokers.OrderNatsSender;
import com.radovan.play.exceptions.DataNotValidatedException;
import com.radovan.play.security.JwtAuthAction;
import com.radovan.play.security.RoleSecured;
import com.radovan.play.services.*;
import com.radovan.play.utils.TokenUtils;
import jakarta.inject.Inject;
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


    @Inject
    private void initialize(OrderService orderService, OrderAddressService orderAddressService, OrderItemService orderItemService, OrderNatsSender orderNatsSender) {
        this.orderService = orderService;
        this.orderAddressService = orderAddressService;
        this.orderItemService = orderItemService;
        this.orderNatsSender = orderNatsSender;
    }

    @RoleSecured({"ROLE_USER"})
    public Result provideMyAddress(Http.Request request) {
        String jwtToken = TokenUtils.provideToken(request);
        JsonNode customerData = orderNatsSender.retrieveCurrentCustomer(jwtToken);
        Integer addressId = customerData.get("shippingAddressId").asInt();
        JsonNode address = orderNatsSender.retrieveAddress(addressId,jwtToken);
        return ok(Json.toJson(address));
    }

    @RoleSecured({"ROLE_USER"})
    public Result confirmShipping(Http.Request request) {
        String jwtToken = TokenUtils.provideToken(request);

        JsonNode addressData = request.body().asJson();
        if (addressData == null || !addressData.has("address") || !addressData.has("city")) {
            throw new DataNotValidatedException("Invalid shipping address data!");
        }

        // Dohvati korisnika i adresu
        JsonNode customerData = orderNatsSender.retrieveCurrentCustomer(jwtToken);
        int addressId = customerData.get("shippingAddressId").asInt(); // Sada uzimamo shippingAddressId

        // Ažuriraj adresu preko NATS-a
        JsonNode updatedAddress = orderNatsSender.updateShippingAddress(addressData,addressId,jwtToken);

        return ok(Json.toJson(updatedAddress));
    }



    @RoleSecured({"ROLE_USER"})
    public Result placeOrder(Http.Request request) {
        orderService.addOrder(TokenUtils.provideToken(request));
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



}
