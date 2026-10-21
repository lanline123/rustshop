// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.merchant;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商家表model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("merchant")
@Schema(description = "商家表")
public class Merchant {

    @Schema(description = "商家ID")
    @TableId(type = IdType.AUTO)
    private Integer merchantId;

    @Schema(description = "入驻申请ID")
    private Integer merchantApplyId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "商户创建时间")
    private Long addTime;

    @Schema(description = "基础数据JSON")
    private String baseData;

    @Schema(description = "店铺数据JSON")
    private String shopData;

    @Schema(description = "商户数据JSON")
    private String merchantData;

    @Schema(description = "状态，1：正常，2：取消资格")
    private Integer status;

    @Schema(description = "null或者0表示未删除 1 表示删除")
    private Integer isDelete;

    @Schema(description = "类型，1：个人，2：企业")
    private Integer type;

    @Schema(description = "企业名称")
    private String companyName;

    @Schema(description = "申请主体信息")
    private String corporateName;

    @Schema(description = "结算周期单位天")
    private Integer settlementCycle;

}