package com.mimimart.api.controller.shop;

import com.mimimart.api.dto.ApiResponse;
import com.mimimart.api.dto.order.*;
import com.mimimart.application.service.OrderService;
import com.mimimart.application.service.ShipmentService;
import com.mimimart.domain.order.model.Order;
import com.mimimart.domain.shipment.model.Shipment;
import com.mimimart.infrastructure.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台訂單 Controller
 */
@Tag(name = "前台訂單管理", description = "會員訂單相關 API")
@RestController
@RequestMapping("/api/shop/order")
public class ShopOrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    public ShopOrderController(OrderService orderService, ShipmentService shipmentService) {
        this.orderService = orderService;
        this.shipmentService = shipmentService;
    }

    /**
     * 建立訂單(從前端傳入的項目列表)
     */
    @Operation(summary = "建立訂單", description = "從前端傳入的購買項目建立訂單,並觸發付款流程")
    @PostMapping("/create")
    public ApiResponse<OrderDetailResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        Order order = orderService.createOrder(
                userDetails.getUserId(),
                request.getItems(),
                request.toDeliveryInfo(),
                request.getShippingFee()
        );

        // 查詢物流資訊
        Shipment shipment = shipmentService.getShipmentByOrderId(order.getId());

        return ApiResponse.success("訂單建立成功", OrderDetailResponse.from(order, shipment));
    }

    /**
     * 查詢會員訂單列表
     */
    @Operation(summary = "查詢訂單列表", description = "查詢會員的所有訂單(按建立時間降序)")
    @GetMapping("/list")
    public ApiResponse<List<OrderListItemResponse>> getOrderList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Order> orders = orderService.getMemberOrders(userDetails.getUserId());
        List<OrderListItemResponse> response = orders.stream()
                .map(OrderListItemResponse::from)
                .collect(Collectors.toList());
        return ApiResponse.success("查詢成功", response);
    }

    /**
     * 查詢訂單詳情
     */
    @Operation(summary = "查詢訂單詳情", description = "根據訂單編號查詢訂單詳細資訊")
    @PostMapping("/detail")
    public ApiResponse<OrderDetailResponse> getOrderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderNumberRequest request
    ) {
        Order order = orderService.getOrderDetail(userDetails.getUserId(), request.getOrderNumber());
        Shipment shipment = shipmentService.getShipmentByOrderId(order.getId());
        return ApiResponse.success("查詢成功", OrderDetailResponse.from(order, shipment));
    }

    /**
     * 取消訂單
     */
    @Operation(summary = "取消訂單", description = "取消訂單(僅等待付款中的訂單可取消)")
    @PostMapping("/cancel")
    public ApiResponse<Void> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderNumberRequest request
    ) {
        orderService.cancelOrder(userDetails.getUserId(), request.getOrderNumber());
        return ApiResponse.success("訂單已取消");
    }

    /**
     * 確認收貨
     */
    @Operation(summary = "確認收貨", description = "確認收貨並完成訂單(僅已出貨的訂單可確認)")
    @PostMapping("/confirm-receipt")
    public ApiResponse<Void> confirmReceipt(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OrderNumberRequest request
    ) {
        orderService.confirmReceipt(userDetails.getUserId(), request.getOrderNumber());
        return ApiResponse.success("已確認收貨");
    }

    /**
     * 會員訂單狀態統計
     */
    @Operation(summary = "訂單狀態統計", description = "查詢會員各狀態訂單數量")
    @GetMapping("/stats")
    public ApiResponse<java.util.Map<String, Long>> getMyOrderStats(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        java.util.Map<String, Long> stats = orderService.getMemberOrderStats(userDetails.getUserId());
        return ApiResponse.success("查詢成功", stats);
    }
}
