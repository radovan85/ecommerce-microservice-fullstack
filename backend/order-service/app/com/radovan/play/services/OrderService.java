package com.radovan.play.services;

import com.radovan.play.dto.OrderDto;
import play.mvc.Http;

import java.util.List;

public interface OrderService {

    OrderDto addOrder(Http.Request request);

    OrderDto getOrderById(Integer orderId);

    List<OrderDto> listAll();

    List<OrderDto> listAllByCartId(Integer cartId);

    void deleteOrder(Integer orderId);

    void deleteAllByCartId(Integer cartId);
}
