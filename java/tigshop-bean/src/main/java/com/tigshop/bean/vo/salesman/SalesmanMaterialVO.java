// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.product.Product;
import com.tigshop.bean.model.salesman.SalesmanMaterialCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分销员分组VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分销员分组参数")
public class SalesmanMaterialVO {

    // *** SalesmanMaterial ***

    @Schema(description = "分销员素材ID")
    private Integer id;

    @Schema(description = "添加时间")
    private Long addTime;

    @Schema(description = "是否置顶：1 是 0 否")
    private Integer isTop;

    @Schema(description = "正文内容")
    private String content;

    @Schema(description = "是否有效：1 是 0 否")
    private Integer isAvailable;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "类型")
    private Integer categoryId;

    @Schema(description = "分享次数")
    private Integer shareNum;

    @Schema(description = "产品ID")
    private Integer productId;

    @Schema(description = "多图json")
    private String picsJson;

    // *** Other ***

    @Schema(description = "多图")
    private List<Pic> pics;

    @Schema(description = "分销员分组")
    private SalesmanMaterialCategory category;

    @Schema(description = "商品")
    private Product product;

    @Data
    @Schema(description = "图片信息")
    public static class Pic {
        @Schema(description = "图片ID")
        private Integer picId;

        @Schema(description = "图片缩略图")
        private String picThumb;

        @Schema(description = "图片URL")
        private String picUrl;

        @Schema(description = "图片名称")
        private String picName;
    }

}