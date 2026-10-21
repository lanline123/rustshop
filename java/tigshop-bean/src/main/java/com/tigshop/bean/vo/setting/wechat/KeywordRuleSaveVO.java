// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.setting.wechat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公众号关键字规则保存响应 VO
 * <p>
 * 对应接口 POST /adminapi/wechat/keywordRule/save，返回保存成功后的规则 id。
 *
 * @author Tigshop团队
 * @create 2026-04-23
 */
@Data
@Schema(description = "公众号关键字规则保存响应 VO")
public class KeywordRuleSaveVO {

    @Schema(description = "保存成功返回的规则 id")
    private Long id;
}
