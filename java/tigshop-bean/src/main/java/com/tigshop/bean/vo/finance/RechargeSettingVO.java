// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.finance;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户充值金额快捷表VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "用户充值金额快捷表参数")
public class RechargeSettingVO {
    @Schema(description = "用户充值金额快捷表ID")
    private Integer rechargeSettingId;

    @Schema(description ="用户充值金额快捷表名称")
    private String rechargeSettingName;

    @Schema(description ="用户充值金额快捷表图片")
    private String rechargeSettingPic;

    @Schema(description ="用户充值金额快捷表排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="用户充值金额快捷表描述")
    private String rechargeSettingDesc;
}