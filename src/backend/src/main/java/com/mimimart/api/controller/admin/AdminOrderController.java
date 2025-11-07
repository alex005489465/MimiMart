package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.order.*;
import com.mimimart.application.service.OrderService;
import com.mimimart.domain.order.model.Order;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 後台訂單 Controller
 */
@Tag(name = "後台訂單管理", description = "管理員訂單管理 API")
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 查詢所有訂單(分頁+篩選)
     */
    @Operation(summary = "查詢訂單列表", description = "查詢所有訂單,支援分頁與多條件篩選")
    @GetMapping("/list")
    public ApiResponse<List<OrderListItemResponse>> getOrderList(
            @ModelAttribute AdminOrderQueryRequest queryRequest,
            @Parameter(description = "頁碼 (從 1 開始)") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每頁筆數") @RequestParam(defaultValue = "20") int size
    ) {
        // 將前端的 1-based 頁碼轉換為 Spring Data JPA 的 0-based
        int zeroBasedPage = page - 1;
        Pageable pageable = PageRequest.of(zeroBasedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage = orderService.getAllOrdersAdmin(
                queryRequest.getStatus(),
                queryRequest.getOrderNumber(),
                queryRequest.getStartDate(),
                queryRequest.getEndDate(),
                pageable
        );

        // 轉換為 List 回應
        List<OrderListItemResponse> responseList = orderPage.getContent().stream()
                .map(OrderListItemResponse::from)
                .collect(Collectors.toList());

        // 建立分頁資訊
        Map<String, Object> meta = new HashMap<>();
        // 將 0-based 頁碼轉換為 1-based 返回給前端
        meta.put("currentPage", orderPage.getNumber() + 1);
        meta.put("totalPages", orderPage.getTotalPages());
        meta.put("totalItems", orderPage.getTotalElements());
        meta.put("pageSize", orderPage.getSize());

        return ApiResponse.success("查詢成功", responseList, meta);
    }

    /**
     * 查詢訂單詳情
     */
    @Operation(summary = "查詢訂單詳情", description = "根據訂單編號查詢訂單詳細資訊")
    @PostMapping("/detail")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@Valid @RequestBody OrderNumberRequest request) {
        Order order = orderService.getOrderDetailAdmin(request.getOrderNumber());
        return ApiResponse.success("查詢成功", OrderDetailResponse.from(order));
    }

    /**
     * 標記訂單為已出貨
     */
    @Operation(summary = "標記訂單為已出貨", description = "將訂單狀態從已付款改為已出貨")
    @PostMapping("/ship")
    public ApiResponse<Void> shipOrder(@Valid @RequestBody OrderNumberRequest request) {
        orderService.shipOrder(request.getOrderNumber());
        return ApiResponse.success("訂單已標記為已出貨");
    }

    /**
     * 取消訂單(含原因)
     */
    @Operation(summary = "取消訂單", description = "管理員取消訂單並填寫取消原因")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelOrder(@Valid @RequestBody CancelOrderRequest request) {
        orderService.cancelOrderAdmin(request.getOrderNumber(), request.getCancellationReason());
        return ApiResponse.success("訂單已取消");
    }
}
