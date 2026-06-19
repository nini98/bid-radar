package com.bidradar.bid.controller;

import com.bidradar.bid.dto.request.BidNoticeSearchRequest;
import com.bidradar.bid.dto.response.BidListResponse;
import com.bidradar.bid.dto.response.BidNoticeDetailResponse;
import com.bidradar.bid.service.BidNoticeService;
import com.bidradar.common.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidNoticeController {

    private final BidNoticeService bidNoticeService;

    @GetMapping
    public Response<BidListResponse> getList(@ModelAttribute BidNoticeSearchRequest request) {
        return Response.success(bidNoticeService.getList(request));
    }

    @GetMapping("/{bidId}")
    public Response<BidNoticeDetailResponse> getDetail(@PathVariable Long bidId) {
        return Response.success(bidNoticeService.getDetail(bidId));
    }
}
