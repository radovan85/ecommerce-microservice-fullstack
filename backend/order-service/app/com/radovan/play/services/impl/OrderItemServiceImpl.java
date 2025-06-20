package com.radovan.play.services.impl;

import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.OrderItemDto;
import com.radovan.play.entity.OrderItemEntity;
import com.radovan.play.repositories.OrderItemRepository;
import com.radovan.play.services.OrderItemService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class OrderItemServiceImpl implements OrderItemService {

    private OrderItemRepository itemRepository;
    private TempConverter tempConverter;

    @Inject
    private void initialize(OrderItemRepository itemRepository, TempConverter tempConverter) {
        this.itemRepository = itemRepository;
        this.tempConverter = tempConverter;
    }

    @Override
    public List<OrderItemDto> listAllByOrderId(Integer orderId) {
        List<OrderItemEntity> allItems = itemRepository.findAllByOrderId(orderId);
        return allItems.stream().map(tempConverter::orderItemEntityToDto).collect(Collectors.toList());
    }
}
