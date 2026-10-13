package com.tigshop.bean.vo.promotion.buyershow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 买家秀视频流分页结果-客户端（游标翻页）
 *
 * @author wzh
 */
@Data
@Schema(description = "买家秀视频流分页结果-客户端")
public class BuyerShowVideoFeedPageClientVO {

    @Schema(description = "当前页视频流项")
    private List<BuyerShowVideoFeedItemClientVO> list;

    @Schema(description = "下一页游标；无更多时为 null")
    private String nextCursor;

    @Schema(description = "是否还有下一页")
    private Boolean hasMore;
}
