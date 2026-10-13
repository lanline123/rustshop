package com.tigshop.api.controller.shop;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.shop.CollectCreateDTO;
import com.tigshop.bean.dto.shop.ShopListDTO;
import com.tigshop.bean.query.product.CommentListPageQuery;
import com.tigshop.bean.query.shop.PickupPageQuery;
import com.tigshop.bean.query.shop.StoreDetailQuery;
import com.tigshop.bean.query.shop.StorePageQuery;
import com.tigshop.bean.vo.decorate.DecorateDiscreteVO;
import com.tigshop.bean.vo.o2o.PickupListVO;
import com.tigshop.bean.vo.o2o.StoreDetailVO;
import com.tigshop.bean.vo.o2o.StoreListVO;
import com.tigshop.bean.vo.product.CommentVO;
import com.tigshop.bean.vo.shop.ClientShopDetailVO;
import com.tigshop.bean.vo.shop.ClientShopVO;
import com.tigshop.common.annotation.IgnoreSeoResponseAdvice;
import com.tigshop.service.decorate.DecorateDiscreteService;
import com.tigshop.service.decorate.DecorateService;
import com.tigshop.service.product.CommentService;
import com.tigshop.service.shop.CollectShopService;
import com.tigshop.service.shop.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 店铺控制器
 *
 * @author Jayce
 * @create 2024/9/30 15:56
 */
@RestController
@RequestMapping("/api/shop/shop")
@Tag(name = "店铺")
@Validated
public class ShopClientController {
    @Resource
    ShopService shopService;

    @Resource
    CollectShopService collectShopService;

    @Resource
    DecorateService decorateService;

    @Resource
    DecorateDiscreteService decorateDiscreteService;

    @Resource
    CommentService commentService;

    @GetMapping("/list")
    @Operation(summary = "店铺列表")
    public Page<ClientShopVO> list(ShopListDTO listDTO) {
        return shopService.clientList(listDTO);
    }

    @PostMapping("/collect")
    @Operation(summary = "店铺收藏")
    public void collect(@RequestBody CollectCreateDTO createDTO) {
        collectShopService.collect(createDTO.getShopId());
    }

    @GetMapping("/head")
    @Operation(summary = "店铺头部装修")
    @IgnoreSeoResponseAdvice
    public DecorateDiscreteVO shopHead(@RequestParam("shopId") Integer shopId) {
        return decorateDiscreteService.getShopHead(shopId);
    }

    @GetMapping("/detail")
    @Operation(summary = "店铺详情")
    public ClientShopDetailVO detail(@RequestParam("shopId") Integer shopId) {
        return shopService.clientDetail(shopId);
    }

    @GetMapping("/decorate")
    @Operation(summary = "店铺装修")
    public JSONObject shopDecorate(@RequestParam("shopId") Integer shopId,
                                   @RequestParam(value = "previewId", required = false) Integer previewId) {
        return decorateService.getShopIndexDecorateData(shopId, previewId);
    }

    @GetMapping("/pickupList")
    @Operation(summary = "店铺关联自提点")
    public Page<PickupListVO> pickupList(PickupPageQuery query) {
        return shopService.pickupList(query);
    }

    @GetMapping("/pickupDetail")
    @Operation(summary = "自提点详情")
    public PickupListVO pickupDetail(@RequestParam("shopId") Integer shopId) {
        return shopService.pickupDetail(shopId);
    }

    @GetMapping("/storeList")
    @Operation(summary = "店铺列表")
    public Page<StoreListVO> storeList(StorePageQuery storePageQuery) {
        return shopService.storeList(storePageQuery);
    }

    @GetMapping("/storeDetail")
    @Operation(summary = "门店详情")
    public StoreDetailVO storeDetail(StoreDetailQuery query) {
        return shopService.storeDetail(query);
    }

    @GetMapping("/getCommentList")
    @Operation(summary = "获取评论列表")
    public Page<CommentVO> getCommentList(@RequestParam("page") Integer page,
                                          @RequestParam("type") Integer type,
                                          @RequestParam("shopId") Integer shopId) {
        CommentListPageQuery pageQuery = new CommentListPageQuery();
        pageQuery.setPage(page);
        pageQuery.setSize(10);
        pageQuery.setShopId(shopId);
        if (type == 5) {
            pageQuery.setIsShowed(1);
        }
        pageQuery.setType(type);
        pageQuery.setSortField("isTop");
        pageQuery.setSortOrder("desc");
        return commentService.listByShopId(pageQuery);
    }
}
