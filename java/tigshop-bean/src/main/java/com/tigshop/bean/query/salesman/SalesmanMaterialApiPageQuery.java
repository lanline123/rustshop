// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.salesman;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销员分组列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "分销员素材列表参数")
public class SalesmanMaterialApiPageQuery extends BasePage {

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "来源")
    private String from;

    @Schema(description = "店铺id")
    private Integer shopId;

    public void initDefaultParam() {
        if (StrUtil.isNotBlank(this.getSortField())) {
            this.setSortField("is_top");
        }

        if (StrUtil.isNotBlank(this.getSortOrder())) {
            this.setSortOrder("desc");
        }

        this.from = "user";
    }
}
