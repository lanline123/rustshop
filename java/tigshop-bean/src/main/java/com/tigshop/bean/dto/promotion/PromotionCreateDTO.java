// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.promotion;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

import static com.tigshop.common.constant.promotion.PromotionConstants.*;

/**
 * 营销管理创建数据对象
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:13
 */
@Data
@Schema(description = "营销管理参数")
public class PromotionCreateDTO {
    @Schema(description ="营销管理名称")
    @NotBlank(message = PROMOTION_NAME_NOT_NULL)
    @Size(max = 50, message = PROMOTION_NAME_OVER_LENGTH)
    private String promotionName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "类型1秒杀2优惠券3满减4折扣5满赠6限时折扣")
    private Integer type;

    @Schema(description = "活动对应的ID")
    private Integer relationId;

    @Schema(description = "活动范围:0,全部商品;3,指定商品.4，不含以下商品")
    @TableField(value = "`range`")
    private Integer range;

    @Schema(description = "[json]活动范围相关信息")
    private List<Integer> rangeData;

    @Schema(description = "允许的SKU")
    private List<Integer> skuIds;

    @Schema(description = "适用会员范围：0-全部会员，1-指定标记的会员")
    private Integer memberRange;

    @Schema(description = "选择的会员标记列表")
    private List<Integer> memberMarks;

    @Schema(description = "会员人群圈选（历史结构，仅供旧调用方兼容；新调用方请用 sendUserTarget）")
    private com.tigshop.bean.dto.user.MemberAudienceDTO memberAudience;

    @Schema(description = "发送会员/参与人群（指定会员+等级+标签三组并集，优先于 memberAudience）")
    private com.tigshop.bean.dto.user.SendUserTargetDTO sendUserTarget;
}
