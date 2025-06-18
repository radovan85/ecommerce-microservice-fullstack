package com.radovan.play.services;

import com.radovan.play.dto.OrderItemDto;

import java.util.List;

public interface OrderItemService {

    List<OrderItemDto> listAllByOrderId(Integer orderId);
}
