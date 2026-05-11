package com.brickerp.dispatch.service.impl;

import com.brickerp.common.exception.BusinessException;
import com.brickerp.common.exception.ResourceNotFoundException;
import com.brickerp.dispatch.dto.*;
import com.brickerp.dispatch.entity.DeliveryOrder;
import com.brickerp.dispatch.entity.DeliveryOrder.DeliveryStatus;
import com.brickerp.dispatch.repository.DeliveryOrderRepository;
import com.brickerp.dispatch.service.DispatchService;
import com.brickerp.order.entity.SalesOrder;
import com.brickerp.order.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DispatchServiceImpl implements DispatchService {

    private final DeliveryOrderRepository deliveryRepository;
    private final SalesOrderRepository salesOrderRepository;

    @Override
    public DeliveryOrderResponse createDeliveryOrder(DeliveryOrderRequest request) {
        SalesOrder order = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("SalesOrder", request.getSalesOrderId()));

        if (order.getStatus() == SalesOrder.OrderStatus.DRAFT ||
                order.getStatus() == SalesOrder.OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot create delivery for order with status: " + order.getStatus());
        }

        DeliveryOrder delivery = DeliveryOrder.builder()
                .deliveryNumber(generateDeliveryNumber())
                .salesOrder(order)
                .status(DeliveryStatus.PENDING)
                .deliveryDate(request.getDeliveryDate())
                .vehicleNumber(request.getVehicleNumber())
                .driverName(request.getDriverName())
                .driverPhone(request.getDriverPhone())
                .deliveryAddress(request.getDeliveryAddress())
                .notes(request.getNotes())
                .build();

        return toResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryOrderResponse dispatch(Long id) {
        DeliveryOrder delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));

        if (delivery.getStatus() != DeliveryStatus.PENDING) {
            throw new BusinessException("Only PENDING deliveries can be dispatched");
        }

        delivery.setStatus(DeliveryStatus.DISPATCHED);
        if (delivery.getDeliveryDate() == null)
            delivery.setDeliveryDate(LocalDate.now());
        return toResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryOrderResponse markDelivered(Long id, MarkDeliveredRequest request) {
        DeliveryOrder delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));

        if (delivery.getStatus() != DeliveryStatus.DISPATCHED) {
            throw new BusinessException("Only DISPATCHED deliveries can be marked as delivered");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setReceivedBy(request.getReceivedBy());
        delivery.setReceivedAt(request.getReceivedAt() != null ? request.getReceivedAt() : LocalDate.now());
        if (request.getNotes() != null)
            delivery.setNotes(request.getNotes());

        // Update sales order status to DELIVERED
        SalesOrder order = delivery.getSalesOrder();
        order.setStatus(SalesOrder.OrderStatus.DELIVERED);
        order.setDeliveryDate(LocalDate.now());
        salesOrderRepository.save(order);

        return toResponse(deliveryRepository.save(delivery));
    }

    @Override
    public DeliveryOrderResponse markFailed(Long id, String reason) {
        DeliveryOrder delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));

        delivery.setStatus(DeliveryStatus.FAILED);
        delivery.setNotes(reason);
        return toResponse(deliveryRepository.save(delivery));
    }

    @Override
    @Transactional(readOnly = true)
    public DeliveryOrderResponse getById(Long id) {
        return deliveryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryOrder", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> getAll() {
        return deliveryRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeliveryOrderResponse> getByStatus(String status) {
        try {
            DeliveryStatus ds = DeliveryStatus.valueOf(status.toUpperCase());
            return deliveryRepository.findByStatusOrderByCreatedAtDesc(ds)
                    .stream().map(this::toResponse).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + status);
        }
    }

    private String generateDeliveryNumber() {
        return deliveryRepository.findLastDeliveryNumber()
                .map(last -> {
                    int num = Integer.parseInt(last.replace("DEL-", ""));
                    return String.format("DEL-%05d", num + 1);
                })
                .orElse("DEL-00001");
    }

    private DeliveryOrderResponse toResponse(DeliveryOrder d) {
        DeliveryOrderResponse r = new DeliveryOrderResponse();
        r.setId(d.getId());
        r.setDeliveryNumber(d.getDeliveryNumber());
        r.setSalesOrderId(d.getSalesOrder().getId());
        r.setSalesOrderNumber(d.getSalesOrder().getOrderNumber());
        r.setCustomerName(d.getSalesOrder().getCustomer().getName());
        r.setStatus(d.getStatus().name());
        r.setDeliveryDate(d.getDeliveryDate());
        r.setVehicleNumber(d.getVehicleNumber());
        r.setDriverName(d.getDriverName());
        r.setDriverPhone(d.getDriverPhone());
        r.setDeliveryAddress(d.getDeliveryAddress());
        r.setNotes(d.getNotes());
        r.setReceivedBy(d.getReceivedBy());
        r.setReceivedAt(d.getReceivedAt());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}