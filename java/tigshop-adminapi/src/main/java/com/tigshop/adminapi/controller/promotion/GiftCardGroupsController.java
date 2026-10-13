package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupIdParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardGroupsUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardGroupsPageQuery;
import com.tigshop.bean.vo.promotion.card.GiftCardGroupsVO;
import com.tigshop.service.promotion.GiftCardGroupsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tigshop.common.utils.HeaderUtils.getShopId;

/**
 * @author Tigshop项目组
 * @create 2025年10月28日 11:14
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "礼品卡分组")
@RequestMapping("/adminapi/promotion/giftCardGroups")
public class GiftCardGroupsController {
    private final GiftCardGroupsService giftCardGroupsService;

    @GetMapping("/list")
    @Operation(summary = "礼品卡分组列表")
    public Page<GiftCardGroupsVO> list(GiftCardGroupsPageQuery query) {
        return giftCardGroupsService.list(query);
    }

    @PostMapping("/updateStatus")
    @Operation(summary = "修改礼品卡分组状态")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void updateStatus(@RequestBody GiftCardGroupsUpdateParam param) {
        giftCardGroupsService.updateStatus(param.getCardGroupsId(), param.getStatus());
    }

    @PostMapping("/create")
    @Operation(summary = "创建礼品卡分组")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void createGiftCardGroups(@RequestBody GiftCardGroupsCreateParam param) {
        giftCardGroupsService.createGiftCardGroups(param);
    }

    @PostMapping("/update")
    @Operation(summary = "修改礼品卡分组")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void updateGiftCardGroups(@RequestBody GiftCardGroupsUpdateParam param) {
        giftCardGroupsService.updateGiftCardGroups(param);
    }

    @GetMapping("/detail")
    @Operation(summary = "礼品卡分组详情")
    public GiftCardGroupsVO detail(@RequestParam Integer cardGroupsId) {
        return giftCardGroupsService.detail(cardGroupsId);
    }

    @GetMapping("/getGiftCardGroups")
    @Operation(summary = "获取礼品卡分组")
    public List<GiftCardGroupsVO> getGiftCardGroupsByCardType(@RequestParam Integer cardType
            , @RequestParam(required = false) Integer giftCardGroupsId) {
        return giftCardGroupsService.getGiftCardGroupsByCardType(cardType, giftCardGroupsId, getShopId());
    }

    @PostMapping("/delete")
    @Operation(summary = "删除礼品卡分组")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void deleteGiftCardGroups(@RequestBody GiftCardGroupIdParam param) {
        giftCardGroupsService.deleteGiftCardGroups(param.getGiftCardGroupsId());
    }

    @PostMapping("/giftCardGroupProductsUpdate")
    @Operation(summary = "礼品卡绑定商品管理")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void giftCardGroupProductsUpdate(@RequestBody GiftCardGroupsUpdateParam param) {
        giftCardGroupsService.giftCardGroupProductsUpdate(param);
    }
}