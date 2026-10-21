package com.tigshop.bean.vo.seo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SEO站点地图-文章")
public class SeoArticleVO implements Serializable {

    @Schema(description = "文章ID")
    private String id;

    @Schema(description = "文章标题")
    private String articleTitle;

    @Schema(description = "最后更新时间戳（毫秒）")
    private Long lastUpdate;
}
