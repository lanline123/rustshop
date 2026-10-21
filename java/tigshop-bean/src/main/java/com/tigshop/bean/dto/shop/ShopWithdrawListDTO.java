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
 * 商家账户表列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "商家账户表列表参数")
public class ShopWithdrawListDTO extends BasePage {
    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "申请开始时间")
    private String addTimeStart;

    @Schema(description = "申请结束时间")
    private String addTimeEnd;

    @Schema(description = "门店ID")
    private Integer shopId;

    /** 线上提现渠道过滤：WECHAT/HUIFU/ALIPAY；传入时仅返回该渠道线上提现记录（三渠道独立体系 CR-20260721-144356） */
    @Schema(description = "线上提现渠道过滤：WECHAT/HUIFU/ALIPAY")
    private String payChannel;

    @Schema(description = "提现类型：1线下、2线上，不传为全部")
    private Integer withdrawType;

    @Schema(description = "店铺名称关键字")
    private String keyword;

    @Schema(description = "店铺标题")
    private String shopTitle;

    @Schema(description = "店铺类型")
    private Integer shopType;

    @Schema(description = "提现开始时间")
    private String withdrawTimeStart;

    @Schema(description = "提现结束时间")
    private String withdrawTimeEnd;
}
