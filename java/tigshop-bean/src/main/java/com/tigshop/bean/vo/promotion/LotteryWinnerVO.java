package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 抽奖中奖名单VO（公开展示，用户名脱敏）
 *
 * @author Tigshop团队
 * @create 2024年12月23日
 */
@Data
@Schema(description = "抽奖中奖名单VO")
public class LotteryWinnerVO {

    /**
     * 用户昵称（脱敏后）
     */
    @Schema(description = "用户昵称（脱敏后，如：张**、138****8888）")
    private String nickname;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 奖品名称
     */
    @Schema(description = "奖品名称")
    @JsonTranslate(dataType = 12)
    private String prizeName;

    /**
     * 奖品图片
     */
    @Schema(description = "奖品图片")
    @JsonTranslate(dataType = 13)
    private String prizeImage;

    /**
     * 奖品类型:1-积分,2-商品,3-优惠券
     */
    @Schema(description = "奖品类型:1-积分,2-商品,3-优惠券")
    private Integer prizeType;

    /**
     * 中奖时间(秒级时间戳)
     */
    @Schema(description = "中奖时间(秒级时间戳)")
    private Long winTime;
}
