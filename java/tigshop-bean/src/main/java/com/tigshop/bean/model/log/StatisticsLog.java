package com.tigshop.bean.model.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统计明细表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("statistics_log")
@Schema(description = "统计明细日志")
public class StatisticsLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "统计明细表ID")
    private Integer id;

    @Schema(description = "访问时间")
    private Long accessTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "用户")
    private String user;
}
