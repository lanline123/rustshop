// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.decorate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 售后申请表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("decorate_share")
@Schema(description = "装修页面分享表")
public class DecorateShare implements Serializable {
    @TableId(value = "share_id", type = IdType.AUTO)
    @Schema(description = "自增ID号")
    private Integer shareId;

    @Schema(description = "分享sn码")
    private String shareSn;

    @Schema(description = "分享验证token")
    private String shareToken;

    @Schema(description = "装修页面id")
    private Integer decorateId;

    @Schema(description = "token有效时间")
    private Long validTime;

    @Schema(description = "添加时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
