package com.tigshop.bean.model.promotion.lottery;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 抽奖活动装修配置
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "装修配置")
public class DecorateConfig implements Serializable {

    /**
     * 背景图片URL
     */
    @Schema(description = "背景图片URL")
    @JsonTranslate(dataType = 13)
    private String bgImage;

    /**
     * 背景颜色(默认:#FF6B6B)
     */
    @Schema(description = "背景颜色(默认:#FF6B6B)")
    private String bgColor;

    /**
     * 是否显示中奖名单:0-否,1-是(默认:1)
     */
    @Schema(description = "是否显示中奖名单:0-否,1-是(默认:1)")
    private Integer showWinnerList;

    /**
     * 玩法规则(富文本)
     */
    @Schema(description = "玩法规则(富文本)")
    @JsonTranslate(dataType = 12)
    private String activityRules;

    /**
     * 奖品入口图URL
     */
    @Schema(description = "奖品入口图URL")
    @JsonTranslate(dataType = 13)
    private String prizeEntryImage;

    /**
     * 获取默认配置
     */
    public static DecorateConfig defaultConfig() {
        DecorateConfig config = new DecorateConfig();
        config.setBgColor("#FF6B6B");
        config.setShowWinnerList(1);
        return config;
    }
}
