package com.tigshop.bean.param.promotion;

import com.tigshop.bean.model.promotion.lottery.DecorateConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 抽奖活动参数
 * 
 * 保存流程：
 * 1. 保存活动基本信息，获得 activityId
 * 2. 删除原有奖品（编辑时），保存新的奖品列表，建立 tempIndex → prizeId 映射
 * 3. 处理九宫格配置，将 tempIndex 转换为 prizeId
 * 4. 更新活动的 gridConfig
 *
 * @author Tigshop团队
 * @create 2024年12月12日
 */
@Data
@Schema(description = "抽奖活动参数")
public class LotteryActivityParam {

    /**
     * 活动ID(更新时必填)
     */
    @Schema(description = "活动ID(更新时必填)")
    private Integer activityId;

    /**
     * 活动名称
     */
    @NotBlank(message = "活动名称不能为空")
    @Schema(description = "活动名称")
    private String activityName;

    /**
     * 活动开始时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank(message = "开始时间不能为空")
    @Schema(description = "活动开始时间(格式:yyyy-MM-dd HH:mm:ss)")
    private String startTime;

    /**
     * 活动结束时间(格式:yyyy-MM-dd HH:mm:ss)
     */
    @NotBlank(message = "结束时间不能为空")
    @Schema(description = "活动结束时间(格式:yyyy-MM-dd HH:mm:ss)")
    private String endTime;

    /**
     * 抽奖参与条件:0-无限制(免费),1-消耗积分
     */
    @Schema(description = "抽奖参与条件:0-无限制(免费),1-消耗积分")
    private Integer costType;

    /**
     * 每次抽奖消耗积分(消耗积分模式必填)
     */
    @Schema(description = "每次抽奖消耗积分(消耗积分模式必填)")
    private Integer costPoints;

    /**
     * 每日最大抽奖次数(0表示不限制)
     */
    @Schema(description = "每日最大抽奖次数(0表示不限制)")
    private Integer dailyLimit;

    /**
     * 最大抽奖次数(0表示不限制)
     */
    @Schema(description = "最大抽奖次数(0表示不限制)")
    private Integer totalLimit;

    /**
     * 每人最多中奖次数(0表示不限制)
     */
    @Schema(description = "每人最多中奖次数(0表示不限制)")
    private Integer winLimit;

    /**
     * 装修配置(包含背景图片、背景颜色、是否显示中奖名单、玩法规则)
     */
    @Schema(description = "装修配置")
    private DecorateConfig decorateConfig;

    /**
     * 奖品列表（整体保存，支持新增和编辑）
     * - 新增奖品：设置 tempIndex，用于与九宫格关联
     * - 编辑奖品：设置 prizeId
     */
    @Valid
    @Schema(description = "奖品列表（整体保存）")
    private List<LotteryPrizeItemParam> prizeList;

    /**
     * 九宫格配置(8个位置)
     * - 关联已有奖品：使用 prizeId
     * - 关联新增奖品：使用 tempIndex（与奖品列表中的 tempIndex 对应）
     */
    @NotEmpty(message = "九宫格配置不能为空")
    @Size(min = 8, max = 8, message = "九宫格必须配置8个位置")
    @Valid
    @Schema(description = "九宫格配置(8个位置)")
    private List<GridPositionParam> gridConfig;

    /**
     * 未中奖名称(九宫格未选择奖品的位置显示)
     */
    @NotBlank(message = "未中奖名称不能为空")
    @Schema(description = "未中奖名称")
    private String noWinName;

    /**
     * 未中奖图片URL(九宫格未选择奖品的位置显示)
     */
    @NotBlank(message = "未中奖图片不能为空")
    @Schema(description = "未中奖图片URL")
    private String noWinImage;

    /**
     * 未中奖提示语(弹窗显示)
     */
    @Schema(description = "未中奖提示语")
    private String noWinTips;

    /**
     * 奖品入口图URL
     */
    @Schema(description = "奖品入口图URL")
    private String prizeEntryImage;
}


