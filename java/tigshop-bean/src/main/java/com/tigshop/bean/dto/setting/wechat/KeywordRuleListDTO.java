// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.setting.wechat;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 公众号关键字回复规则列表查询参数
 *
 * <p>page / size / sortField / sortOrder 继承自 {@link BasePage}；
 * 本类仅扩展规则名称与启用状态两个筛选字段。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "关键字回复规则列表查询参数")
public class KeywordRuleListDTO extends BasePage {

    @Schema(description = "规则名称（模糊匹配，可空）")
    private String ruleName;

    @Schema(description = "启用状态筛选：0=停用 1=启用（可空）")
    private Integer enabled;
}
