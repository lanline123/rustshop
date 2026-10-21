// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.im;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 客服消息播报记录列表参数（语音播报「播报记录」抽屉-客服消息 tab 专用，需求 #646738）
 * <p>
 * 仅承载分页参数（page/size 继承自 {@link BasePage}）。店铺、客服、消息方向等过滤条件
 * 由服务端固定注入（shopId=当前店铺、servantId=当前登录客服、type=1 用户消息、status=1 正常），
 * 不由前端传入，口径与 SSE customerMsgCount 一致。
 *
 * @author Tigshop团队
 */
@Setter
@Getter
@Schema(description = "客服消息播报记录列表参数")
public class ImMessageReceivedListDTO extends BasePage {
}
