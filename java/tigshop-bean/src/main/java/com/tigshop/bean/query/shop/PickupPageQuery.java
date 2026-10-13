// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.query.shop;


import com.tigshop.common.core.entity.BasePage;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author Tigshop团队
 * @create 2025/8/28 15:27
 */
@Data
public class PickupPageQuery extends BasePage {

    private String regionId;

    private Integer shopId;

    private BigDecimal longitude = BigDecimal.ZERO;

    private BigDecimal latitude = BigDecimal.ZERO;
}
