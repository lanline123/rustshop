// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.shop;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 店铺资金变化列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "店铺资金变化列表参数")
public class AccountListDTO extends BasePage {
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "申请开始时间")
    private String startTime;

    @Schema(description = "申请结束时间")
    private String endTime;

    @Schema(description = "店铺标题")
    private Integer shopTitle;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "资金类型：all/online/offline，默认 all")
    private String balanceType;

    @Schema(description = "线上渠道：WECHAT/HUIFU/ALIPAY；不传则不限渠道")
    private String payChannel;

    @Schema(description = "店铺类型：1 店铺，2 门店")
    private Integer shopType;
}
