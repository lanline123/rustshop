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
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 公众号关键字回复规则批量删除参数
 *
 * <p>对应接口 POST /adminapi/wechat/keywordRule/del。
 * 项目内 {@code com.tigshop.bean.dto.im.DelDTO} 字段为 conversationId/shopId，语义不符；
 * {@code BatchDTO} 使用 {@code List<Integer>} 且含多余字段，与本功能主键 {@code Long} 类型不匹配，
 * 因此单独建立本 DTO 承载 {@code List<Long> ids}。
 *
 * @author Tigshop团队
 * @create 2026年04月23日
 */
@Data
@Schema(description = "关键字回复规则批量删除参数")
public class KeywordRuleDelDTO {

    @Schema(description = "规则 id 集合")
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
