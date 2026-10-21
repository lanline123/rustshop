package com.tigshop.api.controller.promotion;

import com.tigshop.bean.model.promotion.GiftCardGroups;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.param.promotion.giftcard.ExchangeCardParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardClaimParam;
import com.tigshop.bean.vo.promotion.card.GiftCardGroupsVO;
import com.tigshop.bean.vo.promotion.card.GiftCardsVO;
import com.tigshop.common.utils.SecurityUtils;
import com.tigshop.service.promotion.GiftCardGroupsService;
import com.tigshop.service.promotion.GiftCardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author Tigshop项目组
 * @create 2025年10月28日 11:14
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "礼品卡")
@RequestMapping("/api/promotion/giftCardGroups")
public class GiftCardGroupsController {
    private final GiftCardsService giftCardsService;
    private final GiftCardGroupsService giftCardGroupsService;

    @GetMapping("/showCardPassword")
    @Operation(summary = "礼品卡密码显示")
    GiftCardsVO showCardPassword(@RequestParam String giftCardSn){
        return giftCardsService.giftCardsDetail(giftCardSn,true);
    }

    @GetMapping("/detail")
    @Operation(summary = "礼品卡详情")
    public GiftCardsVO detail(@RequestParam String giftCardSn) {
        GiftCardsVO giftCardsVO = giftCardsService.giftCardsDetail(giftCardSn,false);
        GiftCardGroupsVO giftCardGroupsDetail = giftCardGroupsService.detail(giftCardsVO.getCardGroupsId());
        giftCardsVO.setGiftCardProducts(giftCardGroupsDetail.getGiftCardProducts());
        giftCardsVO.setCardGroupName(giftCardGroupsDetail.getCardGroupName());
        giftCardsVO.setImageUrl(giftCardGroupsDetail.getImageUrl());
        return giftCardsVO;
    }

    // 领取卡
    @PostMapping("/claim")
    @Operation(summary = "领取礼品卡")
    public void claim(@RequestBody GiftCardClaimParam param) {
        giftCardsService.claimCard(param, SecurityUtils.getCurrentUserId());
    }

    // 转赠卡
    @PostMapping("/transfer")
    @Operation(summary = "转赠礼品卡")
    public String transfer(@RequestBody GiftCardClaimParam param) {
       return giftCardsService.transferCard(param, SecurityUtils.getCurrentUserId());
    }

    // 卡是否过期
    @GetMapping("/isGiftCardExpired")
    @Operation(summary = "礼品卡是否过期")
    public boolean isGiftCardExpired(@RequestParam Integer cardId) {
        GiftCards card = giftCardsService.getById(cardId);
        return giftCardsService.isGiftCardExpired(card);
    }

    // 兑换卡
    @PostMapping("/exchange")
    @Operation(summary = "兑换礼品卡")
    public GiftCardsVO exchange(@RequestBody ExchangeCardParam param) {
        return giftCardsService.exchangeCard(param.getCardNumber(), param.getCardPassword(), SecurityUtils.getCurrentUserId());
    }

    @GetMapping("/getTransferLink")
    @Operation(summary = "获取转赠链接")
    public String getTransferLink(@RequestParam String giftCardSn,@RequestParam Integer isFrom) {
        return giftCardsService.getTransferLink(giftCardSn,isFrom);
    }
}