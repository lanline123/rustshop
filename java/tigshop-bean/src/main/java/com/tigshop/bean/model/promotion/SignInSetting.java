// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.model.promotion;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 积分签到model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("sign_in_setting")
@Schema(description = "积分签到")
public class SignInSetting {
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "积分签到ID")
    private Integer id;

    @Schema(description ="积分签到名称")
    private String name;

    @Schema(description ="赠送积分")
    private Integer points;

    @Schema(description ="天数")
    private Integer dayNum;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "添加时间")
    private Long createTime;
}
