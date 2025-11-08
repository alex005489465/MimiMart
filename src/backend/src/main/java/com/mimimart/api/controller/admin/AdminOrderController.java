package com.mimimart.api.controller.admin;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.order.*;
import com.mimimart.api.dto.shipment.RecordShippingRequest;
import com.mimimart.api.dto.shipment.UpdateDeliveryStatusRequest;
import com.mimimart.application.service.OrderService;
import com.mimimart.application.service.ShipmentService;
import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.shipment.model.Shipment;
import com.mimimart.domain.shipment.model.ShippingInfo;
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
    private final ShipmentService shipmentService;

    public AdminOrderController(OrderService orderService, ShipmentService shipmentService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
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
        // 驗證日期範圍
        if (queryRequest.getStartDate() != null && queryRequest.getEndDate() != null) {
            if (queryRequest.getStartDate().isAfter(queryRequest.getEndDate())) {
                return ApiResponse.error("INVALID_DATE_RANGE", "開始日期不可晚於結束日期");
            }
        }

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
        Shipment shipment = shipmentService.getShipmentByOrderId(order.getId());
        return ApiResponse.success("查詢成功", OrderDetailResponse.from(order, shipment));
    }

    /**
     * 記錄出貨資訊（填寫物流商、追蹤號碼等）
     */
    @Operation(summary = "記錄出貨資訊", description = "填寫物流商、追蹤號碼等出貨資訊，並將配送狀態改為已出貨")
    @PostMapping("/ship")
    public ApiResponse<Void> shipOrder(@Valid @RequestBody RecordShippingRequest request) {
        // 1. 查詢訂單取得 Order ID
        Order order = orderService.getOrderDetailAdmin(request.getOrderNumber());

        // 2. 建立 ShippingInfo
        ShippingInfo shippingInfo = ShippingInfo.builder()
                .carrier(request.getCarrier())
                .trackingNumber(request.getTrackingNumber())
                .shippedAt(java.time.LocalDateTime.now())
                .estimatedDeliveryDate(request.getEstimatedDeliveryDate())
                .build();

        // 3. 記錄出貨資訊（會同步更新訂單狀態為 SHIPPED）
        shipmentService.recordShipping(order.getId(), shippingInfo);

        // 4. 更新訂單狀態為已出貨
        orderService.shipOrder(request.getOrderNumber());

        return ApiResponse.success("出貨資訊已記錄");
    }

    /**
     * 更新配送狀態
     */
    @Operation(summary = "更新配送狀態", description = "更新訂單的配送狀態（運送中、配送中、已送達等）")
    @PostMapping("/update-delivery-status")
    public ApiResponse<Void> updateDeliveryStatus(@Valid @RequestBody UpdateDeliveryStatusRequest request) {
        Order order = orderService.getOrderDetailAdmin(request.getOrderNumber());
        shipmentService.updateDeliveryStatus(order.getId(), request.getStatus(), request.getNotes());
        return ApiResponse.success("配送狀態已更新");
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

    /**
     * 完成訂單
     */
    @Operation(summary = "完成訂單", description = "將訂單狀態從已出貨改為已完成")
    @PostMapping("/complete")
    public ApiResponse<Void> completeOrder(@Valid @RequestBody OrderNumberRequest request) {
        orderService.completeOrder(request.getOrderNumber());
        return ApiResponse.success("訂單已完成");
    }

    /**
     * 訂單統計
     */
    @Operation(summary = "訂單統計", description = "查詢訂單統計資料（總訂單數、總金額、各狀態訂單數）")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = orderService.getOrderStatistics();
        return ApiResponse.success("查詢成功", statistics);
    }
}
