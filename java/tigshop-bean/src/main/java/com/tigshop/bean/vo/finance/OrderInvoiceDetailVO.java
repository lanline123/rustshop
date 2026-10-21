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

import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单发票VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "订单发票参数")
public class OrderInvoiceDetailVO {
    @Schema(description = "订单发票ID")
    private String addTime;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "申请回复")
    private String applyReply;

    @Schema(description = "公司账户")
    private String companyAccount;

    @Schema(description = "公司地址")
    private String companyAddress;

    @Schema(description = "公司银行")
    private String companyBank;

    @Schema(description = "公司代码")
    private String companyCode;

    @Schema(description = "公司名称")
    private String companyName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "公司电话")
    private String companyPhone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "发票内容")
    private String invoiceContent;

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "发票类型")
    private Integer invoiceType;

    @Schema(description = "发票类型名称")
    private String invoiceTypeName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "订单ID")
    private Integer orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "地区名称列表")
    private List<String> regionNames;

    @Schema(description = "状态")
    private Integer status;

    @JsonTranslate
    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "抬头类型")
    private Integer titleType;

    @Schema(description = "抬头类型名称")
    private String titleTypeName;

    @Schema(description = "总金额")
    private BigDecimal totalAmount;

    @Schema(description = "用户地址")
    private String userAddress;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "发票附件")
    private List<Object> invoiceAttachment;
}
