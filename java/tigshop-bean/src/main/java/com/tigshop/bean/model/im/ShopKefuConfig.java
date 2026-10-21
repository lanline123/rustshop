package com.tigshop.bean.model.im;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 店铺客服配置
 *
 * @author Tigshop团队
 */
@Data
@TableName("shop_kefu_config")
@Schema(description = "店铺客服配置")
public class ShopKefuConfig implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "客服选择；0-无客服，1-腾讯云客服，2-企业微信客服，3-自定义客服，4-TigChart客服")
    private Integer kefuType;

    @Schema(description = "腾讯云客服打开方式；0-打开链接地址，1-小弹窗打开")
    private Integer kefuYzfType;

    @Schema(description = "腾讯云客服sign")
    private String kefuYzfSign;

    @Schema(description = "企业微信客服ID")
    private String kefuWorkwxId;

    @Schema(description = "企业微信企业ID")
    private String corpId;

    @Schema(description = "客服链接")
    private String kefuCode;

    @Schema(description = "客服链接打开方式；0-打开链接地址，1-小弹窗打开")
    private Integer kefuCodeBlank;

    @Schema(description = "WhatsApp客服号码")
    private String kefuWhatsappPhone;

    @Schema(description = "客服热线电话")
    private String kefuPhone;

    @Schema(description = "服务时间")
    private String kefuTime;

    @Schema(description = "接入方式：0 全部自动接入 1 全部手动接入")
    private Integer autoAccessMode;

    @Schema(description = "回头客接入：0 关闭 1 优先接入上次客服")
    private Integer returnCustomerAccess;

    @Schema(description = "创建时间")
    private Long createTime;

    @Schema(description = "更新时间")
    private Long modifyTime;
}
