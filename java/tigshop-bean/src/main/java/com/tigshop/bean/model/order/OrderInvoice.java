// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.order;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单发票model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("order_invoice")
@Schema(description = "订单发票")
public class OrderInvoice implements Serializable {
    /**
     * 订单发票申请id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 会员id
     */
    private Integer userId;

    /**
     * 订单id/父订单id
     */
    private Integer orderId;

    /**
     * 发票类型：1:普通发票，2:专用发票
     */
    private Integer invoiceType;

    /**
     * 发票申请状态：0:待处理，1:已开，2:失败/作废
     */
    private Integer status;

    /**
     * 申请失败时的回复内容
     */
    private String applyReply;

    /**
     * 抬头：1：个人，2企业
     */
    private Integer titleType;

    /**
     * 纳税人识别号
     */
    private String companyCode;

    /**
     * 个人/单位名称
     */
    private String companyName;

    /**
     * 单位地址
     */
    private String companyAddress;

    /**
     * 单位电话
     */
    private String companyPhone;

    /**
     * 开户银行
     */
    private String companyBank;

    /**
     * 银行账号
     */
    private String companyAccount;

    /**
     * 发票内容，统一为商品明细
     */
    private String invoiceContent;

    /**
     * 发票号码
     */
    private String invoiceNo;

    /**
     * 发票金额
     */
    private BigDecimal amount;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 申请时间
     */
    private Long addTime;

}