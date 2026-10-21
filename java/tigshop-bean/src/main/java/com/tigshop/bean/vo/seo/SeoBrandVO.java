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
@Schema(description = "SEO站点地图-品牌")
public class SeoBrandVO implements Serializable {

    @Schema(description = "品牌ID")
    private String id;

    @Schema(description = "品牌名称")
    private String brandName;

    @Schema(description = "SEO友好链接，有值时优先使用")
    private String seoLink;

    @Schema(description = "最后更新时间戳（毫秒）")
    private Long lastUpdate;
}
