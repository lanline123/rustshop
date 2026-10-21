// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.salesman;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销员分组model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("salesman_material")
@Schema(description = "分销员分组")
public class SalesmanMaterial {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "分销员素材ID")
    private Integer id;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "是否置顶：1 是 0 否")
    private Integer isTop;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "是否有效：1 是 0 否")
    private Integer isAvailable;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "多图")
    private String pics;

    @Schema(description = "类型")
    private Integer categoryId;

    @Schema(description = "分享次数")
    private Integer shareNum;

    @Schema(description = "产品ID")
    private Integer productId;
}