package com.tigshop.bean.dto.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "WorkBuddy商品草稿")
public class ProductDraftDTO {

    @NotBlank
    private String productName;

    @NotBlank
    private String productSn;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal productPrice;

    /** 与 categoryName 二选一；两者都给时以本字段为准。 */
    private Integer categoryId;

    @Schema(description = "分类名称路径，用 / 或 | 分隔，如 女鞋/运动系列；分类不存在时校验失败，不会自动创建")
    private String categoryName;

    private Integer productStock;

    @Schema(description = "上架状态：1 上架，0 下架；缺省为上架")
    private Integer productStatus;

    private String productBrief;

    private String productDesc;

    @Schema(description = "商品图片URL，按顺序排列，首张为主图")
    private List<String> images;

    @Schema(description = "商品详情图URL，按顺序拼进商品详情；与 productDesc 同时给出时本字段被忽略")
    private List<String> detailImages;

    /** 结构化媒体素材；存在时按 role/order 映射，兼容 legacy images/detailImages。 */
    private List<ProductMediaDraftDTO> media;

    @Schema(description = "市场价（划线价），缺省时按售价乘以站点配置的换算倍率自动计算")
    @DecimalMin("0.00")
    private BigDecimal marketPrice;

    @DecimalMin("0.00")
    private BigDecimal costPrice;

    @Schema(description = "商品条码")
    private String productTsn;

    @Schema(description = "搜索关键词，多个词用空格分隔")
    private String keywords;

    @Schema(description = "多规格组合；留空即单规格商品，此时价格与库存取 productPrice/productStock")
    private List<ProductSkuDraftDTO> skus;

    /** 字段来源：user_provided / ai_recommended / site_default。用于预览时向用户讲清哪些值是建议。 */
    private Map<String, String> fieldSources;
}
