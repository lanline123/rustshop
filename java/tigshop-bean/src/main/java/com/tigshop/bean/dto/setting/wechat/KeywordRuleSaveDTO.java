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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 公众号关键字回复规则保存（新增/编辑）参数
 *
 * <p>业务规则要点：
 * <ul>
 *   <li>id 为空视为新增，否则为编辑；</li>
 *   <li>ruleName 全局唯一（不区分大小写），唯一性校验由 Service 层负责；</li>
 *   <li>keywords 长度 1..10，同 matchType 下关键字不可重复（Service 层校验）；</li>
 *   <li>replies 长度 1..5，单条载荷条件必填规则见 {@link ReplyPayloadDTO}，
 *       其中 content 长度上限（关键字回复为 300）由 Service 层额外校验，
 *       不复用 ReplyPayloadDTO 内部的通用上限。</li>
 * </ul>
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@Schema(description = "关键字回复规则保存参数")
public class KeywordRuleSaveDTO {

    @Schema(description = "规则ID（编辑时传入，新增留空）")
    private Long id;

    @Schema(description = "规则名称，≤60，全局唯一（不区分大小写）")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 60, message = "规则名称长度不能超过60")
    private String ruleName;

    @Schema(description = "回复模式：1=随机回复 2=全部回复")
    @NotNull(message = "回复模式不能为空")
    private Integer replyMode;

    @Schema(description = "启用状态：0=停用 1=启用")
    @NotNull(message = "启用状态不能为空")
    private Integer enabled;

    @Schema(description = "关键字列表，长度 1..10")
    @NotEmpty(message = "关键字列表不能为空")
    @Size(min = 1, max = 10, message = "关键字数量需在 1..10 之间")
    @Valid
    private List<KeywordRuleKeywordDTO> keywords;

    @Schema(description = "回复内容列表，长度 1..5")
    @NotEmpty(message = "回复内容列表不能为空")
    @Size(min = 1, max = 5, message = "回复内容数量需在 1..5 之间")
    @Valid
    private List<ReplyPayloadDTO> replies;
}
