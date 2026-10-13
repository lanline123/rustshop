package com.tigshop.bean.vo.im;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前台搜索店铺相关VO
 *
 * @author Tigshop团队
 * @create 2024年12月09日
 */
@Data
@Schema(description = "前台搜索店铺相关参数")
public class ImSearchShopVO {
    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "店铺名称")
    private String shopTitle;

    @Schema(description = "店铺logo")
    private String shopLogo;
}

