// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.vo.user.UserBaseVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 分销员列表
 *
 * @author kidd
 * @create 2025/6/21
 */
@Schema(description = "分销员列表")
@Data
public class SalesmanVO {

    // *** Salesman ***

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "级别")
    private Integer level;

    @Schema(description = "组ID")
    private Integer groupId;

    @Schema(description = "上级ID")
    private Integer pid;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "销售金额")
    private BigDecimal saleAmount;

    // *** Other ***

    @Schema(description = "级别文本")
    private String levelText;

    @Schema(description = "添加时间")
    private String addTime;

    @Schema(description = "基础用户信息")
    private UserBaseVO baseUserInfo;

    @Schema(description = "组信息")
    private SalesmanGroup groupInfo;

    @Schema(description = "上级用户信息")
    private PidUserInfo pidUserInfo;

    @Schema(description = "总客户数")
    private Integer totalCustomer;

    // *** Unknown ***

    @Schema(description = "销售员名称")
    private String salesmanName;

    @Schema(description = "销售员图片")
    private String salesmanPic;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否展示")
    private Integer isShow;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "总佣金")
    private Double totalCommission;

    @Schema(description = "总邀请数")
    private Integer totalInvite;



    @Data
    public static class PidUserInfo {

        @Schema(description = "销售员ID")
        private Integer salesmanId;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "基础用户信息")
        private UserBaseVO baseUserInfo;
    }
}