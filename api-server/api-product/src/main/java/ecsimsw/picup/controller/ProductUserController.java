package ecsimsw.picup.controller;

import ecsimsw.picup.page.annotation.AdditionalSortParameter;
import ecsimsw.picup.page.annotation.LimitedSizePagination;
import ecsimsw.picup.dto.rest.*;
import ecsimsw.picup.service.ProductSearchService;
import ecsimsw.picup.service.ProductOrderService;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/products")
@RestController
public class ProductUserController {

    private final ProductSearchService cursorSearchService;
    private final ProductOrderService orderService;

    public ProductUserController(ProductSearchService cursorSearchService, ProductOrderService orderService) {
        this.cursorSearchService = cursorSearchService;
        this.orderService = orderService;
    }

    @AdditionalSortParameter(property = "id", direction = Direction.ASC)
    @LimitedSizePagination(maxSize = 10000)
    @GetMapping("/cursor")
    public ResponseEntity<List<ProductResponse>> currentFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final List<ProductResponse> responses = cursorSearchService.fetchFrame(pageable, frameRequest);
        return ResponseEntity.ok(responses);
    }

    @AdditionalSortParameter(property = "id", direction = Direction.ASC)
    @LimitedSizePagination(maxSize = 10000)
    @GetMapping("/cursor/next")
    public ResponseEntity<List<ProductResponse>> nextFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final List<ProductResponse> responses = cursorSearchService.fetchNextFrame(pageable, frameRequest);
        return ResponseEntity.ok(responses);
    }

    @AdditionalSortParameter(property = "id", direction = Direction.ASC)
    @LimitedSizePagination(maxSize = 10000)
    @GetMapping("/cursor/prev")
    public ResponseEntity<List<ProductResponse>> prevFrame(Pageable pageable, ProductFrameRequest frameRequest) {
        final List<ProductResponse> responses = cursorSearchService.fetchPrevFrame(pageable, frameRequest);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/order")
    public ResponseEntity<OrderResponse> order(OrderRequest orderRequest) {
        final OrderResponse response = orderService.order(orderRequest);
        return ResponseEntity.ok(response);
    }
}
