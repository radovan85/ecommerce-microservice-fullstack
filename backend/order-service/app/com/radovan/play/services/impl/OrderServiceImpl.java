package com.radovan.play.services.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.radovan.play.brokers.OrderNatsSender;
import com.radovan.play.converter.TempConverter;
import com.radovan.play.dto.*;
import com.radovan.play.entity.OrderAddressEntity;
import com.radovan.play.entity.OrderEntity;
import com.radovan.play.entity.OrderItemEntity;
import com.radovan.play.exceptions.InstanceUndefinedException;
import com.radovan.play.exceptions.OutOfStockException;
import com.radovan.play.repositories.OrderAddressRepository;
import com.radovan.play.repositories.OrderItemRepository;
import com.radovan.play.repositories.OrderRepository;
import com.radovan.play.services.*;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class OrderServiceImpl implements OrderService {

    private OrderRepository orderRepository;
    private OrderAddressRepository orderAddressRepository;
    private OrderItemRepository orderItemRepository;
    private TempConverter tempConverter;
    private final ZoneId zoneId = ZoneId.of("UTC");
    private OrderNatsSender orderNatsSender;

    @Inject
    private void initialize(OrderRepository orderRepository, OrderAddressRepository orderAddressRepository, OrderItemRepository orderItemRepository, TempConverter tempConverter, OrderNatsSender orderNatsSender) {
        this.orderRepository = orderRepository;
        this.orderAddressRepository = orderAddressRepository;
        this.orderItemRepository = orderItemRepository;
        this.tempConverter = tempConverter;
        this.orderNatsSender = orderNatsSender;
    }

    @Override
    public OrderDto getOrderById(Integer orderId) {
        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new InstanceUndefinedException("The order has not been found!"));
        return tempConverter.orderEntityToDto(orderEntity);
    }

    @Override
    public List<OrderDto> listAll() {
        List<OrderEntity> allOrders = orderRepository.findAll();
        return allOrders.stream().map(tempConverter::orderEntityToDto).collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> listAllByCartId(Integer cartId) {
        List<OrderEntity> allOrders = orderRepository.findAllByCartId(cartId);
        return allOrders.stream().map(tempConverter::orderEntityToDto).collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Integer orderId) {
        getOrderById(orderId);
        orderRepository.deleteById(orderId);
    }


    @Override
    public void deleteAllByCartId(Integer cartId){
        List<OrderDto> allOrders = listAllByCartId(cartId);
        allOrders.forEach(order -> deleteOrder(order.getOrderId()));
    }

    @Override
    public OrderDto addOrder(String jwtToken) {

        JsonNode customerData = orderNatsSender.retrieveCurrentCustomer(jwtToken);

        if (customerData == null || !customerData.has("cartId") || !customerData.has("shippingAddressId")) {
            throw new RuntimeException("Customer data is missing required fields!");
        }

        int cartId = customerData.get("cartId").asInt();
        int addressId = customerData.get("shippingAddressId").asInt();

        // 3. Validate cart
        JsonNode cart = orderNatsSender.validateCart(cartId, jwtToken);

        Float cartPrice = cart.get("cartPrice").floatValue();

        // 4. Create basic order
        OrderDto orderDto = new OrderDto();
        orderDto.setCartId(cartId);
        orderDto.setOrderPrice(cartPrice);

        // 5. Get shipping address
        JsonNode shippingAddress = orderNatsSender.retrieveAddress(addressId, jwtToken);
        if (shippingAddress == null || !shippingAddress.has("address") || !shippingAddress.has("city")) {
            throw new RuntimeException("Shipping address data is missing required fields!");
        }

        OrderAddressDto orderAddressDto = new OrderAddressDto();
        orderAddressDto.setAddress(shippingAddress.get("address").asText());
        orderAddressDto.setCity(shippingAddress.get("city").asText());
        orderAddressDto.setState(shippingAddress.get("state").asText());
        orderAddressDto.setCountry(shippingAddress.get("country").asText());
        orderAddressDto.setPostcode(shippingAddress.get("postcode").asText());

        OrderAddressEntity storedAddress = orderAddressRepository.save(tempConverter.orderAddressDtoToEntity(orderAddressDto));

        // 6. Create order entity
        ZonedDateTime currentTime = Instant.now().atZone(zoneId);
        Timestamp currentTimeStamp = Timestamp.valueOf(currentTime.toLocalDateTime());

        OrderEntity orderEntity = tempConverter.orderDtoToEntity(orderDto);
        orderEntity.setAddress(storedAddress);
        orderEntity.setCreateTime(currentTimeStamp);
        OrderEntity storedOrder = orderRepository.save(orderEntity);

        // 7. Process cart items
        List<OrderItemEntity> orderedItems = new ArrayList<>();
        List<JsonNode> cartItems = List.of(orderNatsSender.retrieveCartItems(cartId, jwtToken));

        for (JsonNode cartItem : cartItems) {
            if (!cartItem.has("productId") || !cartItem.has("quantity") || !cartItem.has("price")) {
                throw new RuntimeException("Cart item is missing required fields!");
            }

            Integer productId = cartItem.get("productId").asInt();
            Integer quantity = cartItem.get("quantity").asInt();

            JsonNode productResponse = orderNatsSender.retrieveProductFromBroker(productId,jwtToken);
            if (productResponse == null || !productResponse.has("product")) {
                throw new RuntimeException("Invalid product response structure");
            }

            JsonNode product = productResponse.get("product");
            if (!product.has("unitStock") || !product.has("productName") || !product.has("productPrice")) {
                throw new RuntimeException("Product data is missing required fields!");
            }

            Integer unitStock = product.get("unitStock").asInt();
            String productName = product.get("productName").asText();
            Float productPrice = product.get("productPrice").floatValue();
            Float productDiscount = product.has("discount") ? product.get("discount").floatValue() : 0.0f;

            if (quantity > unitStock) {
                throw new OutOfStockException("There is a shortage of " + productName + " in stock");
            }

            ObjectNode updatedProduct = (ObjectNode) product.deepCopy();
            updatedProduct.put("unitStock", unitStock - quantity);
            JsonNode updateResponse = orderNatsSender.updateProductViaBroker(updatedProduct, productId,jwtToken);

            if (updateResponse != null && updateResponse.has("error")) {
                throw new RuntimeException("Failed to update product stock");
            }

            OrderItemDto orderItemDto = new OrderItemDto();
            orderItemDto.setQuantity(quantity);
            orderItemDto.setPrice(cartItem.get("price").floatValue());
            orderItemDto.setProductName(productName);
            orderItemDto.setProductDiscount(productDiscount);
            orderItemDto.setProductPrice(productPrice);
            orderItemDto.setOrderId(storedOrder.getOrderId());

            OrderItemEntity orderItemEntity = tempConverter.orderItemDtoToEntity(orderItemDto);
            orderItemEntity.setOrder(storedOrder);
            orderedItems.add(orderItemRepository.save(orderItemEntity));
        }

        // 11. Finalize order
        storedOrder.getOrderedItems().addAll(orderedItems);
        storedOrder = orderRepository.save(storedOrder);

        // 12. Clean cart
        orderNatsSender.removeAllByCartId(cartId,jwtToken);
        orderNatsSender.refreshCartState(cartId,jwtToken);

        return tempConverter.orderEntityToDto(storedOrder);
    }


}