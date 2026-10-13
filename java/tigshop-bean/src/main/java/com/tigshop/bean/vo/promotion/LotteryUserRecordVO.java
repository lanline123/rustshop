package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖用户中奖详情VO
 *
 * @author Tigshop团队
 * @create 2025年01月04日
 */
@Data
@Schema(description = "抽奖用户中奖详情VO")
public class LotteryUserRecordVO {

    /**
     * 记录ID
     */
    @Schema(description = "记录ID")
    private Integer recordId;

    /**
     * 用户名称
     */
    @Schema(description = "用户名称")
    private String userName;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 来源标签名称
     */
    @Schema(description = "来源标签名称")
    private String fromTagName;

    /**
     * 参与时间
     */
    @Schema(description = "参与时间")
    private String createTime;

    /**
     * 获得奖品（积分显示"积分 x ?"，商品和优惠券显示奖品名称，未中奖显示"未中奖"）
     */
    @Schema(description = "获得奖品")
    @JsonTranslate(dataType = 12)
    private String prizeName;

    /**
     * 奖品状态文本（商品：已兑换/未兑换，积分/优惠券：已发放/未发放，未中奖：-）
     */
    @Schema(description = "奖品状态文本")
    private String statusText;
}
