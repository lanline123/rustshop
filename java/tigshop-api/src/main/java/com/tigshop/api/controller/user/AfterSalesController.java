package com.tigshop.api.controller.user;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tigshop.bean.dto.order.AfterSalesCancelDTO;
import com.tigshop.bean.dto.order.AfterSalesFeedbackDTO;
import com.tigshop.bean.dto.order.ClientAfterSalesUpdateDTO;
import com.tigshop.bean.model.order.Order;
import com.tigshop.bean.param.order.AftersalesApplyParam;
import com.tigshop.bean.query.order.AftersalesListPageQuery;
import com.tigshop.bean.vo.order.*;
import com.tigshop.common.core.TranslatePackage;
import com.tigshop.common.enums.DataType;
import com.tigshop.common.i18n.MessageHelper;
import com.tigshop.service.order.AftersalesLogService;
import com.tigshop.service.order.AftersalesService;
import com.tigshop.service.order.OrderService;
import com.tigshop.service.setting.AfterSalesReasonConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.tigshop.common.utils.SecurityUtils.getCurrentUserId;

/**
 * 用户售后控制器
 *
 * @author Tigshop团队
 * @create 2025年01月20日 16:42
 */
@RestController
@RequestMapping(("/api/user/aftersales"))
@Tag(name = "前台用户售后")
public class AfterSalesController {
    @Resource
    private OrderService orderService;
    @Resource
    private AftersalesService aftersalesService;
    @Resource
    private AftersalesLogService aftersalesLogService;
    @Resource
    private AfterSalesReasonConfigService afterSalesReasonConfigService;
    @Resource
    private TranslatePackage translatePackage;
    @Resource
    MessageHelper messageHelper;

    @GetMapping("/list")
    @Operation(summary = "售后列表")
    public Page<AfterSalesClientVO> list(AftersalesListPageQuery listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return orderService.afterSalesList(listDTO);
    }

    @GetMapping("/config")
    @Operation(summary = "售后配置")
    public Map<String, Object> config(@RequestParam(value = "orderId", required = false) Integer orderId) {
        Map<Integer, String> type = new HashMap<>();
        type.put(1, messageHelper.get("aftersales.type.return.refund"));
        type.put(2, messageHelper.get("aftersales.type.refund.only"));
        if (orderId != null) {
        }
        AfterSalesReasonConfigService.ReasonConfig reasonConfig = afterSalesReasonConfigService.getConfig();
        return Map.of(
                "aftersaleType", type,
                // Map 中的 List<String> 不会经过 @JsonTranslate，需逐项翻译接口文案。
                "aftersaleReason", reasonConfig.reasons().stream()
                        .map(reason -> translatePackage.translate(reason, DataType.API.getCode()))
                        .toList(),
                "afterSalesReasonEnabled", reasonConfig.enabled());
    }

    @GetMapping("/applyData")
    @Operation(summary = "售后详情")
    public AfterSalesApplyDataVO applyData(@RequestParam(value = "itemId", required = false) Integer itemId,
                                           @RequestParam(value = "orderId", required = false) Integer orderId) {
        Integer userId = getCurrentUserId();
        OrderVO detail = orderService.detail(orderId, userId);
        List<AfterSalesDetailVO> afterSalesDetailVOS = orderService.applyData(itemId, orderId);
        for (AfterSalesDetailVO afterSalesDetailVO : afterSalesDetailVOS) {
            detail.getItems().stream().filter(item -> CollUtil.isNotEmpty(item.getExtraSkuData()) && Objects.equals(item.getItemId(), afterSalesDetailVO.getItemId()))
                    .forEach(item -> {
                        OrderExtraSkuDataDTO dataDTO = item.getExtraSkuData().getFirst();
                        if (dataDTO != null) {
                            afterSalesDetailVO.setAttrPrice(dataDTO.getAttrPrice());
                        }
                    });
        }

        return AfterSalesApplyDataVO.builder()
                .list(afterSalesDetailVOS)
                .order(detail)
                .build();
    }

    @PostMapping("/create")
    @Operation(summary = "售后申请")
    public void create(@RequestBody @Validated AftersalesApplyParam param) {
        aftersalesService.afterSalesApply(param);
    }

    @PostMapping("/update")
    @Operation(summary = "售后申请修改")
    public void update(@RequestBody ClientAfterSalesUpdateDTO updateDTO) {
        Integer userId = getCurrentUserId();
        updateDTO.setUserId(userId);
        aftersalesService.afterSalesApplyUpdate(updateDTO);
    }

    @GetMapping("/getRecord")
    @Operation(summary = "售后申请记录")
    public Page<AftersalesVO> getRecord(AftersalesListPageQuery listDTO) {
        Integer userId = getCurrentUserId();
        listDTO.setUserId(userId);
        return aftersalesService.list(listDTO);
    }

    @GetMapping("/detail")
    @Operation(summary = "查看售后记录")
    public AfterSalesClientDetailVO detail(@RequestParam Integer id) {
        Integer userId = getCurrentUserId();
        AfterSalesClientDetailVO afterSalesClientDetailVO = aftersalesService.clientDetail(id, userId);
        OrderVO orderVO = orderService.detail(afterSalesClientDetailVO.getOrderId(), userId);
        afterSalesClientDetailVO.setOrders(orderVO);
        return afterSalesClientDetailVO;
    }

    @GetMapping("/detailLog")
    @Operation(summary = "查看售后log记录")
    public List<AftersalesLogVO> detailLog(@RequestParam Integer id) {
        Integer userId = getCurrentUserId();
        // 校验用户是否有权限查看此售后记录
        aftersalesService.clientDetail(id, userId);
        return aftersalesLogService.getDetailLog(id);
    }

    @PostMapping("/feedback")
    @Operation(summary = "提交售后反馈记录")
    public void feedback(@RequestBody AfterSalesFeedbackDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setUserId(userId);
        aftersalesService.submitFeedbackRecord(dto);
    }

    @PostMapping("/cancel")
    @Operation(summary = "取消售后申请")
    public void cancel(@RequestBody AfterSalesCancelDTO dto) {
        Integer userId = getCurrentUserId();
        dto.setUserId(userId);
        aftersalesService.cancel(dto);
    }
}
