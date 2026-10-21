// **---------------------------------------------------------------------+
// ** 文件 -- ${DESCRIPTION}
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.dto.cart;

import cn.hutool.json.JSONArray;
import com.tigshop.bean.vo.cart.CartVO;
import com.tigshop.bean.vo.promotion.PromotionVO;
import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 购物车促销价格返回结构
 *
 * @author Tigshop团队
 * @create 2024年10月22日 13:50
 */
@Data
@Schema(description = "购物车促销价格返回结构")
public class CartPromotionParsePriceDTO {

    @Schema(description = "购物车列表")
    private List<CartVO> cartList;

    @Schema(description = "优惠信息")
    private PromotionVO promotion;

    @Schema(description = "满赠商品")
    private Gift gift;

    @Data
    @Schema(description = "满赠商品信息")
    public static class Gift {
        @Schema(description = "giftID")
        private Integer giftId;

        @Schema(description = "商品ID")
        private Integer productId;

        @Schema(description = "SKU ID")
        private Integer skuId;

        @Schema(description = "商品名称")
        @JsonTranslate(dataType = 2)
        private String productName;

        @Schema(description = "商品编号")
        private String productSn;

        @Schema(description = "商品缩略图")
        private String picThumb;

        @Schema(description = "类型（例如，满赠类型为 4）")
        private Integer type;

        @Schema(description = "商品类型")
        private Integer productType;

        @Schema(description = "店铺ID")
        private Integer shopId;

        @Schema(description = "SKU 数据")
        private JSONArray skuData;

        @Schema(description = "优惠规则类型")
        private Integer rulesType;

        @Schema(description = "数量")
        private Integer num;
    }

}
