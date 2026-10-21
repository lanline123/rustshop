// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.msg;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员消息列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "管理员消息列表参数")
public class AdminMsgListDTO extends BasePage {
    @Schema(description = "消息类型：11/12/13/31/54交易消息、21-26商品消息、32售后消息、33-37/44财务消息、41-43店铺服务、51-57其它消息；15/16为历史兼容类型")
    private Integer msgType;
}
