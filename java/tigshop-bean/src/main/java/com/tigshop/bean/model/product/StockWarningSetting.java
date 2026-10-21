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
import lombok.Data;

import java.io.Serializable;

/**
 * 库存预警通知总开关（按经营主体隔离）
 *
 * @author Tigshop团队
 */
@Data
@TableName("stock_warning_setting")
@Schema(description = "库存预警通知总开关")
public class StockWarningSetting implements Serializable {

    /** 主体级开关以 shop_id 为唯一标识，不使用自增主键 */
    @TableId(value = "shop_id", type = IdType.INPUT)
    @Schema(description = "归属主体：0=平台自营，>0=店铺或门店")
    private Integer shopId;

    @Schema(description = "库存预警通知总开关：0=关闭，1=开启")
    private Integer notifyEnabled;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long updateTime;
}
