// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品收藏model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("collect_product")
@Schema(description = "商品收藏")
public class CollectProduct implements Serializable {
    @TableId(value = "collect_id", type = IdType.AUTO)
    @Schema(description = "商品收藏ID")
    private Integer collectId;

    @Schema(description ="用户id")
    private Integer userId;

    @Schema(description ="商品id")
    private Integer productId;

    @Schema(description ="添加时间")
    private Long addTime;

    @Schema(description = "店铺ID")
    private Integer shopId;
}