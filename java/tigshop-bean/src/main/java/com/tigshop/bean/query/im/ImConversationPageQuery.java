// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.im;

import cn.hutool.core.util.StrUtil;
import com.tigshop.common.core.entity.BasePage;
import com.tigshop.common.utils.HeaderUtils;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 客服会话列表
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Setter
@Getter
@Schema(description = "客服会话列表参数")
public class ImConversationPageQuery extends BasePage {

    @Schema(description = "时间类型; 会话开始时间-1，排队开始时间-2（暂未实现），最后会话时间-3")
    private Integer timeType;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "最后客服Id")
    private Integer lastServantId;

    @Schema(description = "用户来源")
    private String userFrom;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "会话备注")
    private String remark;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户Id")
    private Integer userId;

    @Schema(description = "是否删除")
    private Integer isDelete;

    // *** Other ***

    @Schema(description = "搜索开始时间")
    private Long searchStartTime;

    @Schema(description = "搜索结束时间")
    private Long searchEndTime;

    public void handleParam() {
        this.shopId = HeaderUtils.getShopId();

        if (StrUtil.isNotBlank(this.startTime)) {
            this.searchStartTime = TigUtils.toTimestampYmd(this.startTime);
        }

        if (StrUtil.isNotBlank(this.endTime)) {
            this.searchEndTime = TigUtils.toTimestampYmd(this.endTime);
        }
    }

}
