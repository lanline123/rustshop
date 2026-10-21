// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.merchant;

import com.alibaba.fastjson.JSONObject;
import com.tigshop.bean.dto.merchant.MerchantAdminDataDTO;
import com.tigshop.bean.model.user.User;
import com.tigshop.common.annotation.SensitiveJson;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商家表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "商家表参数")
public class MerchantVO {
    @Schema(description = "商户认证日期")
    private String addTime;

    @Schema(description ="merchant_user_admin")
    private MerchantAdminDataDTO admin;

    @Schema(description ="基础数据json")
    private JSONObject baseData;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "申请主体信息")
    private String corporateName;

    @Schema(description = "入驻申请ID")
    private Integer merchantApplyId;

    @SensitiveJson(keys = {"contactPhone", "customerServicePhone"})
    @Schema(description = "商户数据JSON")
    private JSONObject merchantData;

    @Schema(description = "商家ID")
    private Integer merchantId;

    @Schema(description = "结算周期单位天")
    private Integer settlementCycle;

    @SensitiveJson(keys = {"contactMobile"})
    @Schema(description ="店铺数据json")
    private JSONObject shopData;

    @Schema(description = "状态，1：正常，2：取消资格")
    private Integer status;

    @Schema(description = "状态文本")
    private String statusText;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @Schema(description = "类型文案，1：个人认证，2：企业认证")
    private String typeText;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "用户数据")
    private User user;

    @Schema(description = "店铺数量")
    private Long shopCount;
}