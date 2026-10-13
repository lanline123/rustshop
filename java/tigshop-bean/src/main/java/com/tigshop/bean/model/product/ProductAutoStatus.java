// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.product;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品自动上下架表
 *
 * @author Tigshop团队
 * @create 2025年12月2日
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "商品自动上下架表")
@TableName("product_auto_status")
public class ProductAutoStatus implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "门店ID：0-平台商品，>0-门店商品")
    private Integer shopId;

    @Schema(description = "操作类型：0-下架，1-上架")
    private Integer operationType;

    @Schema(description = "计划执行时间（时间戳）")
    private Long scheduledTime;

    @Schema(description = "实际执行时间（时间戳）")
    private Long executeTime;

    @Schema(description = "执行状态：0-待执行，1-执行成功，2-执行失败")
    private Integer status;

    @Schema(description = "失败原因")
    private String errorMessage;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;

    @Schema(description = "是否删除：0-未删除，1-已删除")
    private Integer isDeleted;
}

