// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.bean.param.salesman;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.tigshop.common.exception.GlobalException;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 分销模式配置
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@Schema(description = "分销模式配置")
public class SalesmanConfigSaveParam {

    // *** 分销模式 ***

    @Schema(description = "分销模式：1-推广员等级模式（默认） 2-微商模式")
    private Integer distributionMode;

    @Schema(description = "下级招募方式（微商模式专用）：1-消费自动开通 2-名片邀请开通")
    private List<Integer> recruitMethods;

    @Schema(description = "销售类型")
    private Integer saleType;

    @Schema(description = "分销员级别")
    private List<LevelDTO> level;

    @Schema(description = "是否注册成为分销员")
    private Integer registerToSalesman;

    @Schema(description = "是否允许分销员查看客户信息：0-否 1-是")
    private Integer viewCustomerInfo;

    @Data
    @Schema(description = "分销员级别DTO")
    public static class LevelDTO {

        @Schema(description = "分销员级别ID")
        private Integer id;

        @Schema(description = "分销员级别名称")
        private String name;

        @Schema(description = "分销员级别比例")
        private String rate;

        @Schema(description = "下级分销员比例")
        private String downSalesmanRate;

        @Schema(description = "条件设置")
        private ConditionDTO condition;

        @Data
        @Schema(description = "条件DTO")
        public static class ConditionDTO {

            @Schema(description = "自购金额条件")
            private ConditionDetail selfBuyAmount;

            @Schema(description = "推广金额条件")
            private ConditionDetail salesAmount;

            @Schema(description = "发展客户数条件")
            private ConditionDetail salesInviteUsers;

            @Data
            @Schema(description = "条件详情DTO")
            public static class ConditionDetail {
                @Schema(description = "是否选中")
                private Boolean checked;

                @Schema(description = "条件值")
                private String value;

                @Schema(description = "条件标题")
                private String title;

                @Schema(description = "单位")
                private String unit;

                @Schema(description = "是否禁用")
                private Boolean disabled; // 此项根据 JSON 数据可选
            }
        }
    }

    // *** 结算方案 ***

    @Schema(description = "结算类型；1-自动结算，2-人工结算")
    private Integer settlementType;

    @Schema(description = "自动结算日期类型")
    private Integer dateType;

    @Schema(description = "人工结算说明")
    private String desc;

    @Schema(description = "权限检查子权限名称")
    private String authorityCheckSubPermissionName;

    public void adjustLevels() {
        int count = this.level.size();

        // i 从 1 开始遍历到 count - 2 (因为最后一项没有下一级)
        for (int i = 1; i < count - 1; i++) {
            LevelDTO current = this.level.get(i);     // 当前级别
            LevelDTO next = this.level.get(i + 1);    // 下一级别

            // 校验自购金额条件
            checkCondition(current.getCondition().getSelfBuyAmount(), next.getCondition().getSelfBuyAmount());
            // 校验推广金额条件
            checkCondition(current.getCondition().getSalesAmount(), next.getCondition().getSalesAmount());
            // 校验发展客户数条件
            checkCondition(current.getCondition().getSalesInviteUsers(), next.getCondition().getSalesInviteUsers());
        }
    }

    /**
     * 辅助校验单个 ConditionDetail 对象
     */
    private void checkCondition(LevelDTO.ConditionDTO.ConditionDetail current, LevelDTO.ConditionDTO.ConditionDetail next) {
        if (current == null || next == null) {
            return; // 没有该项则跳过
        }

        if (Boolean.TRUE.equals(current.getChecked())) {
            Assert.isTrue(next.getChecked(), () -> new GlobalException("下一级的选项必须参考上一级的选项选中"));
            Assert.isTrue(StrUtil.isNotBlank(next.getValue()), () -> new GlobalException("请设置需求条件的金额"));

            // 当前和下一级比较大小
            try {
                // 使用 BigDecimal 比较
                BigDecimal curVal = new BigDecimal(current.getValue());
                BigDecimal nextVal = new BigDecimal(next.getValue());
                Assert.isTrue(curVal.compareTo(nextVal) <= 0, () -> new GlobalException("选项【" + current.getTitle() + "】需小于等于下一等级金额(" + next.getValue() + ")"));
            } catch (NumberFormatException e) {
                throw new GlobalException("条件值必须是有效数字: " + e.getMessage());
            }
        }
    }

}
