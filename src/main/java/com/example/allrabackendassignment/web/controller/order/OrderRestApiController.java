package com.example.allrabackendassignment.web.controller.order;

import com.example.allrabackendassignment.service.OrderService;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderItemRequest;
import com.example.allrabackendassignment.web.dto.internal.order.request.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderRestApiController {

    private final OrderService orderService;

    @PostMapping()
    public void createOrder(@RequestBody OrderItemRequest orderItemRequest) {
        orderService.createOrder(orderItemRequest);
    }
}
