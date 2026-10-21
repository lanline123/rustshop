// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.deposit;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 平台端店铺保证金账户总览列表查询参数
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "店铺保证金账户总览查询参数")
public class DepositAccountListDTO extends BasePage {

    @Schema(description = "持有者类型 DepositOwnerType：null=全部 1店铺 2供应商")
    private Integer ownerType;

    @Schema(description = "主体类型（商户性质）：null=全部 1个人 2企业；店铺按商户性质、供应商按供应商性质筛选")
    private Integer subjectType;

    @Schema(description = "持有者名称（模糊，统一搜索：店铺行匹配店铺名，供应商行匹配供应商名）")
    private String ownerName;

    @Schema(description = "店铺名称（模糊，兼容字段；为空时回退使用 ownerName 作为统一名称搜索）")
    private String shopTitle;

    @Schema(description = "商户名称（模糊，匹配企业名称/申请主体；仅作用于店铺行，供应商行无商户概念）")
    private String merchantName;

    @Schema(description = "经营类目id（按在售商品类目过滤）")
    private Integer categoryId;

    @Schema(description = "保证金状态 1正常 2不足 3冻结 4未缴纳")
    private Integer depositStatus;

    @Schema(description = "入驻时间起（秒级时间戳）")
    private Long joinTimeStart;

    @Schema(description = "入驻时间止（秒级时间戳）")
    private Long joinTimeEnd;
}
