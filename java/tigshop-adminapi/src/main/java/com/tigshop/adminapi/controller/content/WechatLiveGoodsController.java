package com.tigshop.adminapi.controller.content;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.content.WechatLiveGoodsImportDTO;
import com.tigshop.bean.dto.content.WechatLiveGoodsItemDTO;
import com.tigshop.bean.dto.content.WechatLiveGoodsListDTO;
import com.tigshop.bean.dto.content.*;
import com.tigshop.bean.vo.content.WechatLiveGoodsVO;
import com.tigshop.service.content.WechatLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信直播商品控制器
 */
@RestController
@RequestMapping("/adminapi/content/wechatLiveGoods")
@Tag(name = "微信直播商品", description = "微信直播商品管理接口")
@PreAuthorize("@pms.hasPermission('wechatLiveProduct')")
@Validated
public class WechatLiveGoodsController {

    @Resource
    private WechatLiveService wechatLiveService;

    /**
     * 导入直播商品主表
     * <p>请求体格式：JSON</p>
     * @param importDTO 导入商品参数
     */
    @PostMapping("/import")
    @Operation(summary = "导入直播商品主表", description = "请求体格式：JSON")
    @PreAuthorize("@pms.hasPermission('wechatLiveProductModifyManage')")
    public void importGoods(@Valid @RequestBody WechatLiveGoodsImportDTO importDTO) {
        wechatLiveService.importGoods(buildGoodsItems(importDTO.getGoodsItems(), importDTO.getProductIds()));
    }

    /**
     * 获取直播商品主表列表
     * <p>请求参数格式：Query（URL参数）</p>
     * @param listDTO 查询参数
     * @return 商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取直播商品主表列表", description = "请求参数格式：Query（URL参数）")
    public Page<WechatLiveGoodsVO> getGoodsLibrary(WechatLiveGoodsListDTO listDTO) {
        return wechatLiveService.getGoodsLibrary(listDTO);
    }

    /**
     * 查询直播商品
     * <p>请求参数格式：Query（URL参数）</p>
     * @param queryDTO 查询参数
     * @return 商品列表
     */
    @GetMapping("/query")
    @Operation(summary = "查询直播商品", description = "请求参数格式：Query（URL参数）")
    public Page<WechatLiveGoodsVO> queryGoods(@Valid WechatLiveGoodsQueryDTO queryDTO) {
        return wechatLiveService.queryGoods(queryDTO);
    }



    private List<WechatLiveGoodsItemDTO> buildGoodsItems(List<WechatLiveGoodsItemDTO> goodsItems,
                                                         List<Long> productIds) {
        List<WechatLiveGoodsItemDTO> result = new ArrayList<>();
        if (goodsItems != null) {
            result.addAll(goodsItems);
        }
        if (productIds != null) {
            result.addAll(productIds.stream().map(productId -> {
                WechatLiveGoodsItemDTO itemDTO = new WechatLiveGoodsItemDTO();
                itemDTO.setProductId(productId);
                itemDTO.setProductSkuId(0L);
                return itemDTO;
            }).toList());
        }
        return result;
    }
}
