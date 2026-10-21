// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.query.order;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 软件许可服务实现
 *
 * @author Tigshop团队
 * @create 2024年11月19日 10:19
 */
@Setter
@Getter
@Schema(description = "售后管理列表参数")
public class AftersalesListPageQuery extends BasePage {
    @Schema(description = "搜索关键字")
    private String keyword;

    @Schema(description = "售后类型")
    private Integer aftersaleType;

    @Schema(description = "售后编号")
    private String aftersalesSn;

    @Schema(description = "交易单号")
    private String outTradeNo;

    @Schema(description = "售后状态")
    private Integer status;

    @Schema(description = "店铺id")
    private Integer shopId;

    @Schema(description = "用户id")
    private Integer userId;

    @Schema(description = "微信直播主键ID")
    private Integer wechatLiveId;

    @Schema(description = "微信直播房间ID")
    private Integer wechatLiveRoomId;

    @Schema(description = "微信直播商品ID")
    private Integer wechatGoodsId;

    @Schema(description = "直播分享人OpenId")
    private String liveShareOpenId;
}
