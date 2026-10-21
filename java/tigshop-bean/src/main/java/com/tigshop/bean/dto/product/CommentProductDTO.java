// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 评价管理VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "评价管理参数")
public class CommentProductDTO {

    private Integer commentId;

    /**
     * 发表该评论的用户的用户id,取值user的user_id
     */
    private Integer userId;

    /**
     * 评论该文章或商品的人的名称,取值users的user_name
     */
    private String username;

    /**
     * 会员头像
     */
    private String avatar;

    /**
     * 商品id
     */
    private Integer productId;

    /**
     * 订单id
     */
    private Integer orderId;

    /**
     * 订单商品itemId
     */
    private Integer orderItemId;

    /**
     * 星级，5代表5星
     */
    private Integer commentRank;

    /**
     * 评论的内容
     */
    private String content;

    /**
     * 评论的时间
     */
    private Long addTime;

    /**
     * 状态，0：待审核，1：审核通过，2：无效
     */
    private Integer status;

    /**
     * 是否为回复
     */
    private Integer parentId;

    /**
     * 评价有用
     */
    private Integer usefull;

    /**
     * 评价无用
     */
    private Integer useless;

    /**
     * 【JSON】标签[string]
     */
    private String commentTag;

    /**
     * 【JSON】晒单图片[{pic_thumb,pic_url}]
     */
    private String showPics;

    /**
     * 是否推荐
     */
    private Integer isRecommend;

    /**
     * 是否置顶
     */
    private Integer isTop;

    /**
     * 是否已晒单
     */
    private Integer isShowed;

    /**
     * 系统默认评价
     */
    private Integer isDefault;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 所属店铺
     */
    private Integer shopId;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品编号")
    private String productSn;

    @Schema(description = "商品临时编号")
    private String productTsn;

    @Schema(description = "商品库存")
    private Integer productStock;

    @Schema(description = "商品价格")
    private BigDecimal productPrice;

    @Schema(description = "市场价（划线价）")
    private BigDecimal marketPrice;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "运费模板ID")
    private Long shippingTplId;

    @Schema(description = "商品状态")
    private Integer productStatus;

    @Schema(description = "商品类型")
    private Integer productType;

    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "品牌ID")
    private Integer brandId;

    @Schema(description = "关键词")
    private String keywords;

    @Schema(description = "店铺分类ID")
    private Integer shopCategoryId;

    @Schema(description = "审核状态")
    private Integer checkStatus;

    @Schema(description = "审核原因")
    private String checkReason;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "商品重量")
    private BigDecimal productWeight;

    @Schema(description = "是否促销")
    private Integer isPromote;

    @Schema(description = "是否促销活动")
    private Integer isPromoteActivity;

    @Schema(description = "促销价格")
    private BigDecimal promotePrice;

    @Schema(description = "促销开始日期")
    private Long promoteStartDate;

    @Schema(description = "促销结束日期")
    private Long promoteEndDate;

    @Schema(description = "秒杀最大数量")
    private Integer seckillMaxNum;

    @Schema(description = "商品简介")
    private String productBrief;

    @Schema(description = "商品描述")
    private String productDesc;

    @Schema(description = "图片URL")
    private String picUrl;

    @Schema(description = "缩略图URL")
    private String picThumb;

    @Schema(description = "原图URL")
    private String picOriginal;

    @Schema(description = "是否包邮")
    private Integer freeShipping;

    @Schema(description = "积分")
    private Integer integral;

    @Schema(description = "店铺排序")
    private Integer storeSortOrder;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "是否精品")
    private Integer isBest;

    @Schema(description = "是否新品")
    private Integer isNew;

    @Schema(description = "是否热销")
    private Integer isHot;

    @Schema(description = "最后更新时间")
    private Long lastUpdate;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "赠送积分")
    private Integer giveIntegral;

    @Schema(description = "等级积分")
    private Integer rankIntegral;

    @Schema(description = "供应商ID")
    private Integer suppliersId;

    @Schema(description = "虚拟销量")
    private Integer virtualSales;

    @Schema(description = "限购数量")
    private Integer limitNumber;

    @Schema(description = "商品保养")
    private String productCare;

    @Schema(description = "相关商品")
    private String productRelated;

    @Schema(description = "商品服务ID")
    private String productServiceIds;

    @Schema(description = "是否支持退货")
    private Integer isSupportReturn;

    @Schema(description = "是否支持货到付款")
    private Integer isSupportCod;

    @Schema(description = "商品视频")
    private String productVideo;

    @Schema(description = "预付价格")
    private BigDecimal prepayPrice;

    @Schema(description = "卡组ID")
    private Integer cardGroupId;

    @Schema(description = "虚拟样品")
    private String virtualSample;

    @Schema(description = "付费内容")
    private String paidContent;

}