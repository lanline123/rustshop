package com.tigshop.bean.model.finance;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 商户进件申请 model
 *
 * @author Tigshop团队
 */
@Data
@TableName("merchant_application")
@Schema(description = "商户进件申请")
public class MerchantApplication {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "进件申请ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "申请主体类型:SHOP店铺/门店 VENDOR供应商")
    private String applicantType;

    @Schema(description = "申请主体ID:SHOP=shopId VENDOR=vendorId")
    private Long applicantId;

    @Schema(description = "申请主体名称(店铺名/供应商名)")
    private String applicantName;

    @Schema(description = "支付渠道")
    private String payChannel;

    @Schema(description = "业务申请编号(幂等键)")
    private String businessCode;

    @Schema(description = "微信申请单号")
    private String applymentId;

    @Schema(description = "组织类型")
    private String organizationType;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "联系人姓名")
    private String contactName;

    @Schema(description = "联系人电话")
    private String contactPhone;

    @Schema(description = "身份证号(加密)")
    private String idCardNumberEnc;

    @Schema(description = "银行账号(加密)")
    private String bankAccountEnc;

    @Schema(description = "状态:DRAFT/SUBMITTED/CHECKING/AUDITING/ACCOUNT_NEED_VERIFY/NEED_SIGN/FINISH/REJECTED")
    private String status;

    @Schema(description = "成功后返回的子商户号")
    private String subMchId;

    @Schema(description = "签约链接")
    private String signUrl;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "审核人ID(平台管理员)")
    private Integer auditAdminId;

    @Schema(description = "审核时间")
    private Date auditTime;

    @Schema(description = "提交请求快照(完整JSON,用于展示与审核通过后提交微信)")
    private String requestSnapshot;

    @TableLogic
    @Schema(description = "逻辑删除:0未删 非0已删")
    private Integer isDel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date modifyTime;
}
