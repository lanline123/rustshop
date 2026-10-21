package com.tigshop.bean.query.user;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ObjectUtil;
import com.tigshop.bean.model.user.UserRank;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.common.utils.JsonUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 会员等级更新DTO
 *
 * @author Tigshop团队
 * @create 2024年12月03日 17:17
 */
@Data
@Schema(description = "会员等级更新DTO")
public class UserRankEditParam {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "等级类型")
    private Integer rankType;

    @Schema(description = "等级配置")
    private RankConfig userRankConfig;

    @Schema(description = "成长设置")
    private GrowUpSetting growUpSetting;

    @Size(min = 1, max = 10, message = "用户等级；不能少于1条，不能大于10条")
    @Schema(description = "用户等级数据列表")
    private List<UserRankData> data;

    @Data
    @Schema(description = "等级配置")
    public static class RankConfig {
        @Schema(description = "类型")
        private Integer type;

        @Schema(description = "等级有效期（月）")
        private Integer rankAfterMonth;

        @Schema(description = "使用有效期（月）")
        private Integer useMonth;
    }

    @Data
    @Schema(description = "成长设置")
    public static class GrowUpSetting {
        @Schema(description = "购买订单")
        private Integer buyOrder;

        @Schema(description = "购买订单数量")
        private Integer buyOrderNumber;

        @Schema(description = "购买订单成长值")
        private Integer buyOrderGrowth;

        @Schema(description = "EVPI")
        private Integer evpi;

        @Schema(description = "EVPI成长值")
        private Integer evpiGrowth;

        @Schema(description = "绑定手机")
        private Integer bindPhone;

        @Schema(description = "绑定手机成长值")
        private Integer bindPhoneGrowth;
    }

    @Data
    @Schema(description = "用户等级数据")
    public static class UserRankData {
        @Schema(description = "等级ID")
        private Integer rankId;

        @Schema(description = "等级名称")
        private String rankName;

        @Schema(description = "最小成长值")
        private BigDecimal minGrowthPoints;

        @Schema(description = "最大成长值")
        private Integer maxGrowthPoints;

        @Schema(description = "折扣")
        private BigDecimal discount;

        @Schema(description = "是否显示价格")
        private Integer showPrice;

        @Schema(description = "等级类型")
        private Integer rankType;

        @Schema(description = "等级图标URL")
        private String rankLogo;

        @Schema(description = "等级图标")
        private String rankIco;

        @Schema(description = "等级背景")
        private String rankBg;

        @Schema(description = "等级积分")
        private String rankPoint;

        @Schema(description = "是否免运费")
        private Integer freeShipping;

        @Schema(description = "等级卡类型")
        private Integer rankCardType;

        @Schema(description = "权限列表")
        private List<Right> rights;

        @Schema(description = "等级级别")
        private Integer rankLevel;

        public UserRank buildUserRank(int isPro, int rankType) {
            UserRank userRank = UserRank.builder()
                    .rankId(this.rankId)
                    .rankName(this.rankName)
                    .rankCardType(1)
                    .rankLevel(this.rankLevel)
                    .rankLogo(this.rankLogo)
                    .build();

            if (isPro > 0) {
                userRank.setMinGrowthPoints(this.minGrowthPoints);
                userRank.setMaxGrowthPoints(this.maxGrowthPoints);
                userRank.setDiscount(this.discount);
                userRank.setRankType(rankType);
                userRank.setRankIco(this.rankIco);
                userRank.setRankBg(this.rankBg);
                userRank.setRankPoint(ObjectUtil.isEmpty(this.rankPoint) ? "0" : this.rankPoint);
                userRank.setFreeShipping(this.freeShipping);
                userRank.setRankCardType(this.rankCardType);
                List<Right> rights = this.rights != null ? this.rights : new ArrayList<>();
                userRank.setRights(JsonUtil.toJson(rights));
            }

            return userRank;
        }
    }

    @Data
    @Schema(description = "权限对象")
    public static class Right {
        @Schema(description = "权限名称")
        private String name;

        @Schema(description = "显示名称")
        private String showName;

        @Schema(description = "图标URL")
        private String icon;

        @Schema(description = "权限值")
        private BigDecimal value; // 使用 Object 类型以处理不同类型的值（如 String 和 Integer）

        @Schema(description = "描述")
        private String describe;

        @Schema(description = "是否选中")
        private Integer isChecked;
    }

    public void validParam() {
        Assert.isFalse(this.rankType == 0, () -> new GlobalException("请选择会员等级类型"));
        Assert.isFalse(this.rankType == 1 && this.growUpSetting == null, () -> new GlobalException("请填写成长值设置"));
    }

    public List<UserRank> listSaveUserRanks(int isPro, int rankType) {
        return this.data.stream()
                .filter(data -> data.getRankId() == null)
                .map(data -> data.buildUserRank(isPro, rankType))
                .toList();
    }

    public List<UserRank> listEditUserRanks(int isPro, int rankType) {
        return this.data.stream()
                .filter(data -> data.getRankId() != null)
                .map(data -> data.buildUserRank(isPro, rankType))
                .toList();
    }
}
