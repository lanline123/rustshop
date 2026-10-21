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

import cn.hutool.core.lang.tree.Tree;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 商品分类
 *
 * @author Jayce
 * @create 2024年10月31日 16:17
 */
@Data
@Schema(description = "商品分类")
public class PcCatFloorListCategoryListDTO {
    @Schema(description = "id")
    private Integer id;

    @Schema(description = "分类名称")
    private String name;
    @Schema(description = "图片")
    private String pic;

    @Schema(description = "id")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "子分类")
    private List<Tree<Integer>> children;

    public Integer getCategoryId() {
        return this.id;
    }

    public String getCategoryName() {
        return this.name;
    }
}
