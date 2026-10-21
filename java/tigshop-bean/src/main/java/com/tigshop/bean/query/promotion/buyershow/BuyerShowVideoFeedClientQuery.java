package com.tigshop.bean.query.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 买家秀视频流查询参数-客户端
 *
 * @author wzh
 */
@Data
@Schema(description = "买家秀视频流查询参数-客户端")
public class BuyerShowVideoFeedClientQuery {

    @Min(value = 1, message = "size最小为1")
    @Max(value = 50, message = "size最大为50")
    @Schema(description = "每页条数（前端默认5，最大50）")
    private Integer size = 5;

    @Schema(description = "游标（上一页返回的 nextCursor，首次不传）")
    private String cursor;

    @Schema(description = "从列表点入的起始买家秀id（保证首屏是用户点击的那条，仅首次生效）")
    private Integer startId;

    @Schema(description = "分组ID（可选）")
    private Long groupId;

    @Schema(description = "关键词（可选）")
    private String keyword;

    public int getSize() {
        if (size == null) {
            return 5;
        }
        return Math.min(size, 50);
    }
}
