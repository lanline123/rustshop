// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.decorate;

import com.tigshop.bean.dto.setting.GalleryPicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 首页分类栏VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "首页分类栏参数")
public class MobileCatNavVO {
    @Schema(description = "首页分类栏ID")
    private Integer mobileCatNavId;

    @Schema(description ="选择分类")
    private Integer categoryId;

    @Schema(description ="选择分类")
    private String categoryName;

    @Schema(description ="分类颜色（颜色选择器）")
    private String catColor;

    @Schema(description ="推荐子分类（多选，JSON格式）")
    private Integer[] childCatIds;

    @Schema(description ="推荐品牌")
    private Integer[] brandIds;

    @Schema(description ="是否显示：1显示，0不显示")
    private Integer isShow;

    @Schema(description ="分类banner字符串，使用英文符号'|'分割")
    private GalleryPicDTO[] imgUrl;

    @Schema(description ="在页面显示的顺序，数字越大顺序越靠后")
    private Short sortOrder;

    @Schema(description ="自定义分类名")
    private String catNameAlias;

    @Schema(description ="子分类详情")
    private List<ChildCatInfo> childCatInfo;

    @Schema(description ="品牌详情")
    private List<BrandInfo> brandInfo;

    @Data
    public static class ChildCatInfo {
        @Schema(description = "分类ID")
        private Integer categoryId;
        @Schema(description = "分类名称")
        private String categoryName;
        @Schema(description = "分类图片")
        private String categoryPic;
    }

    @Data
    public static class BrandInfo {
        @Schema(description = "状态文本")
        private String statusText;
        @Schema(description = "品牌ID")
        private Integer brandId;
        @Schema(description = "品牌名称")
        private String brandName;
        @Schema(description = "品牌Logo")
        private String brandLogo;
    }
}