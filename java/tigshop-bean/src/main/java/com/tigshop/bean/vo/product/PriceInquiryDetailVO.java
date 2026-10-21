package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 询价详情
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "询价详情")
public class PriceInquiryDetailVO {

    @Schema(description = "主键id")
    private Integer id;

    @Schema(description = "产品信息")
    private ProductVO product;

    @Schema(description = "需求信息")
    private String content;

    @Schema(description = "询价时间")
    private String createTime;

    @Schema(description = "联系人")
    private String contactName;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系方式")
    private String mobile;

    @Sensitive(type = SensitiveType.EMAIL)
    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "回复备注")
    private String remark;

    @Schema(description = "回复状态：0 未回复 1 已回复")
    private Integer status;

    @Schema(description = "回复记录列表")
    private List<PriceInquiryReplyRecordVO> replyRecords;

    @Data
    @Schema(description = "产品信息")
    public static class ProductVO {
        @Schema(description = "商品id")
        private Integer productId;

        @JsonTranslate(dataType = 2)
        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品编号")
        private String productSn;

        @JsonTranslate(dataType = 7)
        @Schema(description = "商品图片")
        private String picThumb;

        @Schema(description = "商品价格")
        private BigDecimal productPrice;

        @Schema(description = "市场价")
        private BigDecimal marketPrice;
    }
}
