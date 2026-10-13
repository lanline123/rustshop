package com.tigshop.bean.vo.product;

import com.tigshop.common.annotation.JsonTranslate;
import com.tigshop.common.annotation.Sensitive;
import com.tigshop.common.enums.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 询价列表信息
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "电子卡券组参数")
public class PriceInquiryVO {

    @Schema(description = "主键id")
    private Integer id;

    @Sensitive(type = SensitiveType.PHONE)
    @Schema(description = "联系电话")
    private String mobile;

    @Sensitive(type = SensitiveType.EMAIL)
    @Schema(description = "邮箱地址")
    private String email;

    @Schema(description = "需求信息")
    private String content;

    @Schema(description = "商品id")
    private Integer productId;

    @Schema(description = "回复备注")
    private String remark;

    @Schema(description = "回复状态：0 未回复 1 已回复")
    private Integer status;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "产品信息")
    private ProductVO product;

    @Schema(description = "回复记录列表")
    private List<PriceInquiryReplyRecordVO> replyRecords;

    @Data
    @Schema(description = "产品信息")
    public static class ProductVO{
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
    }
}
