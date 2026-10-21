package com.tigshop.bean.dto.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 商品草稿中的结构化媒体素材。 */
@Data
@Schema(description = "商品媒体素材")
public class ProductMediaDraftDTO {
    private Integer assetId;
    private String type;
    private String role;
    private String url;
    private String coverUrl;
    private String format;
}
