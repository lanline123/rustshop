// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.log;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统计基础表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("statistics_base")
@Schema(description = "统计基础表")
public class StatisticsBase implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "统计基础表ID")
    private Integer id;

    @Schema(description = "日期")
    private String date;

    @Schema(description = "点击数")
    private Integer clickCount;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "访客数")
    private Integer visitorCount;

}