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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分销模式设置VO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "分销模式设置参数")
public class SalesmanConfigVO {

    // *** SalesmanConfig ***

    @Schema(description = "分销模式设置ID")
    private Integer id;

    @Schema(description = "店铺ID")
    private Integer shopId;

    @Schema(description = "分销模式设置代码")
    private String code;

    // *** 分销模式 ***

    @Schema(description = "分销模式：1-推广员等级模式（默认） 2-微商模式")
    private Integer distributionMode;

    @Schema(description = "下级招募方式（微商模式专用）：1-消费自动开通 2-名片邀请开通")
    private List<Integer> recruitMethods;

    @Schema(description = "当前部署是否支持微商模式（按版本裁剪，前端据此显隐微商入口）")
    private Boolean wechatBizEnabled;

    @Schema(description = "分销类型")
    private Integer saleType;

    @Schema(description = "分销等级列表")
    private List<Level> level;

    @Schema(description = "是否注册成为分销员")
    private Integer registerToSalesman;

    @Schema(description = "是否允许分销员查看客户信息：0-否 1-是")
    private Integer viewCustomerInfo;

    @Data
    @Schema(description = "分销等级")
    public static class Level {
        @Schema(description = "等级ID")
        private Integer id;

        @Schema(description = "等级名称")
        private String name;

        @Schema(description = "佣金率")
        private String rate;

        @Schema(description = "下级分销员佣金率")
        private String downSalesmanRate;

        @Schema(description = "条件")
        private Condition condition;

        @Data
        @Schema(description = "条件")
        public static class Condition {
            @Schema(description = "自购金额条件")
            private SelfBuyAmount selfBuyAmount;

            @Schema(description = "推广金额条件")
            private SalesAmount salesAmount;

            @Schema(description = "发展客户数条件")
            private SalesInviteUsers salesInviteUsers;

            @Schema(description = "邀请推广条件")
            private InviteSales inviteSales;

            @Data
            @Schema(description = "自购金额条件")
            public static class SelfBuyAmount {
                @Schema(description = "是否启用")
                private Boolean checked;

                @Schema(description = "值")
                private String value;

                @Schema(description = "标题")
                private String title;

                @Schema(description = "单位")
                private String unit;

                @Schema(description = "是否禁用")
                private Boolean disabled;
            }

            @Data
            @Schema(description = "推广金额条件")
            public static class SalesAmount {
                @Schema(description = "是否启用")
                private Boolean checked;

                @Schema(description = "值")
                private String value;

                @Schema(description = "标题")
                private String title;

                @Schema(description = "单位")
                private String unit;

                @Schema(description = "是否禁用")
                private Boolean disabled;
            }

            @Data
            @Schema(description = "发展客户数条件")
            public static class SalesInviteUsers {
                @Schema(description = "是否启用")
                private Boolean checked;

                @Schema(description = "值")
                private String value;

                @Schema(description = "标题")
                private String title;

                @Schema(description = "单位")
                private String unit;

                @Schema(description = "是否禁用")
                private Boolean disabled;
            }

            @Data
            @Schema(description = "邀请推广条件")
            public static class InviteSales {
                @Schema(description = "是否启用")
                private Boolean checked;

                @Schema(description = "值")
                private String value;
            }
        }
    }

    // *** 结算方案设置 ***

    @Schema(description = "结算类型")
    private Integer settlementType;

    @Schema(description = "自动结算时间")
    private Integer dateType;

    @Schema(description = "权限检查子权限名称")
    private String authorityCheckSubPermissionName;

    @Schema(description = "人工结算说明")
    private String desc;

}

