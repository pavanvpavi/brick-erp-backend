package com.brickerp.dispatch.service;

import com.brickerp.dispatch.dto.*;
import java.util.List;

public interface DispatchService {
    DeliveryOrderResponse createDeliveryOrder(DeliveryOrderRequest request);

    DeliveryOrderResponse getById(Long id);

    List<DeliveryOrderResponse> getAll();

    List<DeliveryOrderResponse> getByStatus(String status);

    DeliveryOrderResponse dispatch(Long id);

    DeliveryOrderResponse markDelivered(Long id, MarkDeliveredRequest request);

    DeliveryOrderResponse markFailed(Long id, String reason);
}