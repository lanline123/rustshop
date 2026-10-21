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

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 分销员列表查询条件
 *
 * @author kidd
 * @create 2025/6/21
 */
@Setter
@Getter
@Schema(description = "分销员列表查询条件")
public class SalesmanPageQuery extends BasePage {

    @Schema(description = "手机号或昵称")
    private String mobile;

    @Schema(description = "添加时间开始")
    private String addTimeStart;

    @Schema(description = "添加时间结束")
    private String addTimeEnd;

    @Schema(description = "上级手机号")
    private String pidMobile;

    @Schema(description = "分组ID")
    private Integer groupId;

    @Schema(description = "等级")
    private Integer level;

    // *** Unknown ***

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "分销员id")
    private Integer salesmanId;
}
