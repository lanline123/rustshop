// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.salesman.Salesman;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
public class SalesmanJsonVO extends Salesman{
    @Schema(description = "订单分销等级（1级取 rate,2级取 downSalesmanRate）")
    private Integer orderSaleType;
}