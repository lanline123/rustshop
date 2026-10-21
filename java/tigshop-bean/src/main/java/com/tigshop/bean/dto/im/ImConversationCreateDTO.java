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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服会话创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "客服会话参数")
public class ImConversationCreateDTO {
    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "最后客服ID")
    private Integer lastServantId;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;
}
