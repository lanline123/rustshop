// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.decorate;

import com.tigshop.bean.dto.setting.GalleryPicDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static com.tigshop.common.constant.decorate.MobileCatNavConstants.*;

/**
 * 首页分类栏创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "首页分类栏参数")
public class MobileCatNavCreateDTO {
    @NotBlank(message = CATEGORY_ID_NOT_NULL)
    @Schema(description ="选择分类")
    private Integer categoryId;

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
}
