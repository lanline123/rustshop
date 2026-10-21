// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.shop;

import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 店铺表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "店铺表参数")
public class ShopBaseVO {

    @Schema(description = "简介")
    private String description;

    @Schema(description = "客服入口信息页面： 1 商品详情页 2 订单页")
    private String kefuInlet;

    @Schema(description = "客服链接")
    private String kefuLink;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "客服电话")
    private String kefuPhone;

    @Schema(description = "客服微信")
    private String kefuWeixin;

    @Schema(description = "商户id")
    private Integer merchantId;

    @Schema(description = "店铺表ID")
    private Integer shopId;

    @Schema(description = "店铺logo")
    private String shopLogo;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺状态1开业  4暂停运营 10关店")
    private Integer status;

}