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

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 关键字回复规则中的单个关键字子项
 *
 * <p>同一规则下同 matchType 的 keyword 不可重复，去重校验由 Service 层负责。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@Schema(description = "关键字回复规则关键字子项")
public class KeywordRuleKeywordDTO {

    @Schema(description = "关键字，≤30")
    @NotBlank(message = "关键字不能为空")
    @Size(max = 30, message = "关键字长度不能超过30")
    private String keyword;

    @Schema(description = "匹配方式：1=全匹配 2=半匹配")
    @NotNull(message = "匹配方式不能为空")
    private Integer matchType;
}
