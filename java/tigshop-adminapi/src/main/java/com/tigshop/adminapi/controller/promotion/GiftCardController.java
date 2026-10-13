package com.tigshop.adminapi.controller.promotion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.plugin.excel.annotation.ResponseExcel;
import com.tigshop.bean.dto.promotion.GiftCardUpdateStatusDTO;
import com.tigshop.bean.model.promotion.GiftCards;
import com.tigshop.bean.param.promotion.giftcard.GiftCardCreateParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardIdParam;
import com.tigshop.bean.param.promotion.giftcard.GiftCardUpdateParam;
import com.tigshop.bean.query.promotion.card.GiftCardsPageQuery;
import com.tigshop.bean.vo.promotion.card.*;
import com.tigshop.service.promotion.GiftCardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月28日 11:14
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "礼品卡")
@RequestMapping("/adminapi/promotion/giftCards")
public class GiftCardController {
    private final GiftCardsService giftCardsService;

    @GetMapping("/list")
    @Operation(summary = "礼品卡列表")
    public Page<GiftCardsVO> list(GiftCardsPageQuery query) {
        return giftCardsService.list(query);
    }

    @GetMapping("/detail")
    @Operation(summary = "礼品卡详情")
    public GiftCardsVO detail(@RequestParam Integer giftCardsId) {
        GiftCards byId = giftCardsService.getById(giftCardsId);
        return giftCardsService.giftCardsDetail(byId.getGiftCardSn(),true);
    }

    // 卡导入模板

    @GetMapping("/importTemplate")
    @Operation(summary = "礼品卡导入模板")
    public void importTemplate(HttpServletResponse response) {
         giftCardsService.importTemplate(response);
    }

    @ResponseExcel
    @GetMapping("/exportList")
    @Operation(summary = "礼品卡列表导出")
    public List<GiftCardsExportVO> exportList(@RequestParam Integer cardGroupsId){
        return giftCardsService.exportList(cardGroupsId);
    }

    // 卡列表导入
    @PostMapping("/importList")
    @Operation(summary = "礼品卡列表导入")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public GiftCardsImportVO importList(@RequestParam Integer cardGroupsId, @RequestParam("file") MultipartFile file){
        return giftCardsService.importList(cardGroupsId, file);
    }

    // 卡删除
    @PostMapping("/delete")
    @Operation(summary = "礼品卡删除")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    public void deleteGiftCards(@RequestBody GiftCardIdParam param){
        giftCardsService.deleteGiftCards(param.getCardGroupsId().intValue());
    }

    // 卡添加
    @PostMapping("/create")
    @Operation(summary = "礼品卡添加")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    void createGiftCards(@RequestBody GiftCardCreateParam param){
        giftCardsService.createGiftCards(param);
    }

    // 卡批量生成
    @PostMapping("/createBatch")
    @Operation(summary = "礼品卡批量生成")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    void createGiftCardsBatch(@RequestBody GiftCardIdParam param){
        giftCardsService.createGiftCardsBatch(param.getCardGroupsId(), param.getCount());
    }

    // 卡编辑
    @PostMapping("/update")
    @Operation(summary = "礼品卡编辑")
    @PreAuthorize("@pms.hasPermission('giftCardUpdateManage')")
    void updateGiftCards(@RequestBody GiftCardUpdateParam param){
        giftCardsService.updateGiftCards(param);
    }

    // 礼品卡详情（新接口）
    @Operation(summary = "礼品卡兑换详情")
    @GetMapping("/giftCardExchangeDetail")
    public GiftCardDetailVO giftCardExchangeDetail(@RequestParam Integer giftCardsId) {
        return giftCardsService.giftCardExchangeDetail(giftCardsId);
    }

    @Operation(summary = "礼品卡修改可用状态")
    @PostMapping("/updateCardStatus")
    public void updateGiftCardsAvailableStatus(@RequestBody GiftCardUpdateStatusDTO dto) {
        giftCardsService.updateGiftCardsAvailableStatus(dto);
    }
}