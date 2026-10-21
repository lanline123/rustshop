// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+
package com.tigshop.bean.vo.user;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Tigshop团队
 * @create 2025/4/3 14:36
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "会员等级详情")
public class UserRankDetailVO<T> {

    @Schema(description = "会员等级ID")
    private Integer rankType;
    @Schema(description = "会员等级列表")
    private List<T> userRankList;

    @Schema(description = "会员等级配置")
    private UserRankConfigVO userRankConfig;
    @Schema(description = "会员成长值配置")
    private Object growUpSetting;
}
