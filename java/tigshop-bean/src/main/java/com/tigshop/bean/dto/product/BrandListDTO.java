package com.tigshop.bean.dto.product;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 商品品牌
 * @author Jayce
 * @create 2024-09-30 15:16:06
 */
@Getter
@Setter
@Schema(description = "商品品牌")
public class BrandListDTO extends BasePage {

    /**
     * 该品牌是否显示;0否1显示
     */
    @Schema(description = "是否显示")
    private Integer isShow;

    /**
     * 是否热销品牌:0,否;1,是
     */
    @Schema(description = "是否热销品牌")
    private Integer brandIsHot;

    /**
     * 首字母
     */
    @Schema(description = "首字母")
    private String firstWord;


    @Schema(description = "审核状态:0:待审核,1:审核通过,2:已拒绝")
    private Integer status;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "是否为审核列表")
    private Integer isAudit;
}