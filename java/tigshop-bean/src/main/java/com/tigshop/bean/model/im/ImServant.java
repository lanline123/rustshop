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
 * 客服model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("im_servant")
@Schema(description = "客服")
public class ImServant implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "ID")
    private Integer id;

    @Schema(description ="客服id")
    private Integer servantId;

    @Schema(description ="最后更新时间")
    private Long lastUpdateTime;

    @Schema(description ="进入时间")
    private Long addTime;

    @Schema(description ="状态")
    private Integer status;

    @Schema(description ="所属店铺")
    private Integer shopId;

    @Schema(description ="当前进行的会话数量")
    private Long conversationNum;
}