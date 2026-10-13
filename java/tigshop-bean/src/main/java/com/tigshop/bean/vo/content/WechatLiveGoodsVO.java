// **-----------------------------------------------------------------+
// ** 文件 --
// **-----------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **-----------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **-----------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **-----------------------------------------------------------------+

package com.tigshop.bean.vo.content;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信直播商品VO
 *
 * @author Tigshop团队
 * @create 2024年12月27日 17:13
 */
@Data
@Schema(description = "微信直播商品VO")
public class WechatLiveGoodsVO {

    @Schema(description = "直播商品主表ID")
    private Long id;

    @Schema(description = "直播间商品关联ID")
    private Long roomGoodsId;

    @Schema(description = "直播间ID")
    private Integer roomId;

    @Schema(description = "微信商品ID")
    private Integer wechatGoodsId;

    @Schema(description = "系统商品ID")
    private Long productId;

    @Schema(description = "系统SKU ID，0表示无规格")
    private Long productSkuId;

    @Schema(description = "商品规格数据（JSON）")
    private String skuData;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品封面图")
    private String coverImg;

    @Schema(description = "商品价格（分）")
    private Long price;

    @Schema(description = "实时库存")
    private Integer stock;

    @Schema(description = "价格类型：1-一口价，2-价格区间，3-显示折扣价")
    private Integer priceType;

    @Schema(description = "商品小程序路径")
    private String url;

    @Schema(description = "商品状态：0-未审核，1-审核中，2-审核通过，3-审核驳回")
    private Integer auditStatus;

    @Schema(description = "直播间商品排序，仅直播间列表返回")
    private Integer sort;

    @Schema(description = "是否显示：0-显示，1-隐藏，仅直播间列表返回")
    private Integer isShow;

    @Schema(description = "是否已加入当前直播间：0-未加入，1-已加入，仅商品查询列表返回")
    private Integer isJoinedRoom;
}
