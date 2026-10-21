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
 * 会话消息创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "会话消息参数")
public class ImMessageCreateDTO {
    @Schema(description ="会话消息名称")
    private String imMessageName;

    @Schema(description ="会话消息图片")
    private String imMessagePic;

    @Schema(description ="会话消息排序")
    private Integer sortOrder;

    @Schema(description ="是否展示")
    private Integer isShow;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="会话消息描述")
    private String imMessageDesc;
}
