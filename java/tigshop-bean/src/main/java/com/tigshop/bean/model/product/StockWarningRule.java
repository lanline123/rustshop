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
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 库存预警规则（商品/SKU 维度上下限）
 *
 * @author Tigshop团队
 */
@Data
@TableName("stock_warning_rule")
@Schema(description = "库存预警规则")
public class StockWarningRule implements Serializable {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "归属主体：0=平台自营，>0=店铺或门店")
    private Integer shopId;

    @Schema(description = "商品ID")
    private Integer productId;

    @Schema(description = "SKU ID；0=单规格商品")
    private Integer skuId;

    @Schema(description = "预警开关：0=关闭，1=开启")
    private Integer warnEnabled;

    @Schema(description = "库存下限")
    private Integer lowerLimit;

    @Schema(description = "库存上限")
    private Integer upperLimit;

    /** 保存最近一次已发送的预警状态，使同一状态不重复发送 */
    @Schema(description = "最近一次预警状态：0=正常，1=低库存，2=高库存，3=无货")
    private Integer lastWarnStatus;

    @Schema(description = "最近一次预警发送时间")
    private Long lastWarnTime;

    @TableLogic
    @Schema(description = "逻辑删除：0=有效，1=删除")
    private Integer isDelete;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
