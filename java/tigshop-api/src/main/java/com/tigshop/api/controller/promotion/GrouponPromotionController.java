package com.tigshop.api.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.query.promotion.groupon.GrouponItemPageQuery;
import com.tigshop.bean.vo.promotion.GrouponItemInfoVO;
import com.tigshop.bean.vo.promotion.GrouponItemVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionClientDetailVO;
import com.tigshop.bean.vo.promotion.GrouponPromotionImageVO;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.service.promotion.GrouponPromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拼团活动管理
 *
 * @author kidd
 * @since 2025/8/29 13:52
 */
@RequiredArgsConstructor
@Tag(name = "拼团活动管理")
@RestController
@RequestMapping("/api/promotion/groupon")
public class GrouponPromotionController {

    private final GrouponPromotionService grouponPromotionService;

    @Operation(summary = "客户端详情")
    @GetMapping("/detail")
    public GrouponPromotionClientDetailVO clientDetail(@RequestParam("grouponPromotionId") Long grouponPromotionId) {
        return grouponPromotionService.clientDetail(grouponPromotionId);
    }

    @Operation(summary = "拼团活动商品列表")
    @GetMapping("/itemList")
    public Page<GrouponItemVO> itemList(GrouponItemPageQuery pageQuery) {
        return grouponPromotionService.itemList(pageQuery);
    }

    @Operation(summary = "拼团图片列表")
    @GetMapping("/imageList")
    public Page<GrouponPromotionImageVO> grouponPromotionImages(BasePage pageQuery) {
        return grouponPromotionService.grouponPromotionImages(pageQuery);
    }

    @Operation(summary = "商品id获取拼团活动信息")
    @GetMapping("/getGrouponInfo")
    public GrouponItemInfoVO getGrouponInfo(@RequestParam("productId") int productId) {
        return grouponPromotionService.getGrouponInfo(productId);
    }
}
