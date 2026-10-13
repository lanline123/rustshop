// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.im;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 客服会话model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("im_conversation")
@Schema(description = "客服会话")
public class ImConversation implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "客服会话ID")
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "最后客服ID")
    private Integer lastServantId;

    @Schema(description = "添加时间（时间戳）")
    private Long addTime;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "会话状态：0待接入；1会话中；2已结束")
    private Integer status;

    @Schema(description = "最后更新时间（时间戳）")
    private Long lastUpdateTime;

    @Schema(description = "是否删除：0未删除；1已删除")
    private Integer isDelete;

    @Schema(description = "会话备注")
    private String remark;

    @Schema(description = "会话总结")
    private String summary;
}