package com.radovan.play.converter;

import com.radovan.play.dto.OrderAddressDto;
import com.radovan.play.dto.OrderDto;
import com.radovan.play.dto.OrderItemDto;
import com.radovan.play.entity.OrderAddressEntity;
import com.radovan.play.entity.OrderEntity;
import com.radovan.play.entity.OrderItemEntity;
import com.radovan.play.repositories.OrderAddressRepository;
import com.radovan.play.repositories.OrderItemRepository;
import com.radovan.play.repositories.OrderRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.modelmapper.ModelMapper;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class TempConverter {

    private ModelMapper mapper;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private OrderAddressRepository addressRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ZoneId zoneId = ZoneId.of("UTC");

    @Inject
    private void initialize(ModelMapper mapper, OrderRepository orderRepository, OrderItemRepository orderItemRepository, OrderAddressRepository addressRepository) {
        this.mapper = mapper;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
    }

    public OrderAddressDto orderAddressEntityToDto(OrderAddressEntity address) {
        OrderAddressDto returnValue = mapper.map(address, OrderAddressDto.class);
        Optional<OrderEntity> orderOptional = Optional.ofNullable(address.getOrder());
        if (orderOptional.isPresent()) {
            returnValue.setOrderId(orderOptional.get().getOrderId());
        }
        return returnValue;
    }

    public OrderAddressEntity orderAddressDtoToEntity(OrderAddressDto address) {
        OrderAddressEntity returnValue = mapper.map(address, OrderAddressEntity.class);
        Optional<Integer> orderIdOptional = Optional.ofNullable(address.getOrderId());
        if (orderIdOptional.isPresent()) {
            Integer orderId = orderIdOptional.get();
            OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);
            if (orderEntity != null) {
                returnValue.setOrder(orderEntity);
            }
        }
        return returnValue;
    }

    public OrderItemDto orderItemEntityToDto(OrderItemEntity orderItem) {
        OrderItemDto returnValue = mapper.map(orderItem, OrderItemDto.class);
        Optional<OrderEntity> orderoOptional = Optional.ofNullable(orderItem.getOrder());
        if (orderoOptional.isPresent()) {
            returnValue.setOrderId(orderoOptional.get().getOrderId());
        }
        return returnValue;
    }

    public OrderItemEntity orderItemDtoToEntity(OrderItemDto orderItem) {
        OrderItemEntity returnValue = mapper.map(orderItem, OrderItemEntity.class);
        Optional<Integer> orderIdOptional = Optional.ofNullable(orderItem.getOrderId());
        if (orderIdOptional.isPresent()) {
            Integer orderId = orderIdOptional.get();
            OrderEntity orderEntity = orderRepository.findById(orderId).orElse(null);
            if (orderEntity != null) {
                returnValue.setOrder(orderEntity);
            }
        }
        return returnValue;
    }

    public OrderDto orderEntityToDto(OrderEntity order) {
        OrderDto returnValue = mapper.map(order, OrderDto.class);
        List<Integer> orderItemsIds = new ArrayList<>();
        Optional<List<OrderItemEntity>> itemsOptional = Optional.ofNullable(order.getOrderedItems());
        if (!itemsOptional.isEmpty()) {
            itemsOptional.get().forEach((item) -> {
                orderItemsIds.add(item.getOrderItemId());
            });
        }

        returnValue.setOrderedItemsIds(orderItemsIds);

        Optional<OrderAddressEntity> addressOptional = Optional.ofNullable(order.getAddress());
        if (addressOptional.isPresent()) {
            returnValue.setAddressId(addressOptional.get().getOrderAddressId());
        }

        Optional<Timestamp> createTimeOptional = Optional.ofNullable(order.getCreateTime());
        if (createTimeOptional.isPresent()) {
            ZonedDateTime createTimeZoned = createTimeOptional.get().toLocalDateTime().atZone(zoneId);
            String createTimeStr = createTimeZoned.format(formatter);
            returnValue.setCreateTime(createTimeStr);
        }

        return returnValue;
    }

    public OrderEntity orderDtoToEntity(OrderDto order) {
        OrderEntity returnValue = mapper.map(order, OrderEntity.class);
        Optional<List<Integer>> itemsIdsOptional = Optional.ofNullable(order.getOrderedItemsIds());
        List<OrderItemEntity> orderedItems = new ArrayList<>();
        if (!itemsIdsOptional.isEmpty()) {
            itemsIdsOptional.get().forEach((itemId) -> {
                OrderItemEntity itemEntity = orderItemRepository.findById(itemId).orElse(null);
                if (itemEntity != null) {
                    orderedItems.add(itemEntity);
                }
            });
        }
        returnValue.setOrderedItems(orderedItems);

        Optional<Integer> addressIdOptional = Optional.ofNullable(order.getAddressId());
        addressIdOptional.ifPresent((addressId) -> {
            OrderAddressEntity addressEntity = addressRepository.findById(addressId).orElse(null);
            if (addressEntity != null) {
                returnValue.setAddress(addressEntity);
            }
        });

        return returnValue;
    }
}
