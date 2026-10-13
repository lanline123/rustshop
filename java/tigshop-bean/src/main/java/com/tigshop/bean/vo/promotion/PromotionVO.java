// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.promotion;

import com.tigshop.common.annotation.JsonTranslate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 营销管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "营销管理参数")
public class PromotionVO {
    @Schema(description = "活动ID")
    private Integer promotionId;

    @Schema(description = "活动名称")
    @JsonTranslate(dataType = 10)
    private String promotionName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "类型1秒杀2优惠券3满减4折扣5满赠6限时折扣")
    private Integer type;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "活动对应的ID")
    private Integer relationId;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private List<Integer> rangeData;

    @Schema(description = "允许的SKU")
    private List<Integer> skuIds;

    @Schema(description = "是否启用")
    private Integer isAvailable;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "类型名称")
    private String typeText;

    @Schema(description = "活动内容")
    private Map<String, Object> data;

    @Schema(description = "是否领取")
    private Integer isReceive;

    @Schema(description = "优惠价")
    private BigDecimal amount;

    @Schema(description = "时间格式")
    private String timeText;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，无法无损表达 sendUserTarget 时为空）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;

    @Schema(description = "当前用户是否可参与（前台；未登录或后台返回 true）")
    private Boolean canJoin;

    @Schema(description = "不可参与时的受限提示文案，可空")
    private String restrictTip;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PromotionVO that = (PromotionVO) o;
        return Objects.equals(promotionId, that.promotionId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(promotionId);
    }
}
