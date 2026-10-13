// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tigshop.bean.model.authority.Suppliers;
import com.tigshop.bean.model.product.ECardGroup;
import com.tigshop.bean.model.product.Services;
import com.tigshop.bean.model.setting.ShippingTpl;
import com.tigshop.bean.model.user.UserRank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 商品设置数据对象
 *
 * @author Tigshop团队
 * @create 2024年11月28日 17:12
 */
@Getter
@Setter
@Schema(description = "商品设置数据对象")
public class ProductConfigVO {
    @Schema(description = "运费模板")
    private List<ShippingTpl> shippingTplList;

    @Schema(description = "供应商")
    private List<Suppliers> suppliersList;

    @Schema(description = "服务")
    private List<Services> serviceList;

    // 不加JSON 序列化的注解，eCardList会变成ecardList
    @JsonProperty("eCardList")
    @Schema(description = "电子卡券分组")
    private List<ECardGroup> eCardList;

    @Schema(description = "会员等级等参数")
    private ProductItem item;

    @Schema(description = "商品编辑库存是否可修改：1-可修改（默认）；0-不可修改（库存仅可在库存调整功能修改）")
    private Integer productEditStockModify;

    @Data
    @Schema(description = "会员等级等参数")
    static class ProductItem{
        private List<UserRank> UserRankList;

        private List<Integer> productServiceIds;

        public ProductItem(List<UserRank> userRankList, List<Integer> productServiceIds){
            this.UserRankList = userRankList;
            this.productServiceIds = productServiceIds;
        }
    }

    public ProductConfigVO(List<ShippingTpl> shippingTplList,
                           List<Suppliers> suppliersList,
                           List<Services> serviceList,
                           List<UserRank> userRankList,
                           List<Integer> productServiceIds,
                           List<ECardGroup> eCardGroups) {
        this.shippingTplList = shippingTplList;
        this.suppliersList = suppliersList;
        this.serviceList = serviceList;
        this.item = new ProductItem(userRankList, productServiceIds);
        this.eCardList = eCardGroups;

    }
}
