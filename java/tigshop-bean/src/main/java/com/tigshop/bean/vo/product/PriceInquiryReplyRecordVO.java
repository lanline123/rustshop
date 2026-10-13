package com.tigshop.bean.vo.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 询价回复记录
 *
 * @author Tigshop团队
 */
@Data
@Schema(description = "询价回复记录")
public class PriceInquiryReplyRecordVO {

    @Schema(description = "回复人名称")
    private String replyName;

    @Schema(description = "回复时间")
    private String createTime;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "回复类型：1 商家回复 2 用户回复")
    private Integer replyType;
}
