package com.tigshop.bean.vo.promotion.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

/**
 * @author Tigshop项目组
 * @create 2025年10月31日 11:07
 */
@lombok.Data
@Schema(description = "礼品卡导入VO")
@Builder
public class GiftCardsImportVO {
    @Schema(description = "成功数量")
    private Integer successCount;

    @Schema(description = "失败数量")
    private Integer failCount;

    @Schema(description = "失败列表")
    private List<String> failList;
}