package com.tigshop.bean.vo.salesman;

import com.tigshop.bean.model.salesman.Salesman;
import com.tigshop.bean.model.salesman.SalesmanGroup;
import com.tigshop.bean.model.user.User;
import com.tigshop.bean.vo.user.UserBaseVO;
import com.tigshop.common.utils.TigUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 分销员列表
 *
 * @author kidd
 * @since 2025/6/21 10:14
 */
@Data
public class SalesmanPageVO {

    // *** Salesman ***

    @Schema(description = "分销员ID")
    private Integer salesmanId;

    @Schema(description = "销售金额")
    private String saleAmount;

    // *** Other ***

    @Schema(description = "级别文本")
    private String levelText;

    @Schema(description = "基础用户信息")
    private UserBaseVO baseUserInfo;

    @Schema(description = "组信息")
    private SalesmanGroup groupInfo;

    @Schema(description = "上级用户信息")
    private PidUserInfo pidUserInfo;

    @Schema(description = "总客户数")
    private Integer totalCustomer;

    @Schema(description = "添加时间")
    private String addTime;

    @Data
    public static class PidUserInfo {

        @Schema(description = "销售员ID")
        private Integer salesmanId;

        @Schema(description = "用户ID")
        private Integer userId;

        @Schema(description = "基础用户信息")
        private UserBaseVO baseUserInfo;
    }

    public SalesmanPageVO(Salesman salesman, User user, SalesmanGroup groupInfo, User pUser) {
        this.salesmanId = salesman.getSalesmanId();
        this.saleAmount = salesman.getSaleAmount().toString();

        this.addTime = TigUtils.handelTime(salesman.getAddTime());
        this.levelText = salesman.getLevel() == 1 ? "一级分销员" : "二级分销员";

        UserBaseVO userBaseVO = new UserBaseVO();
        if (user != null) {
            userBaseVO = new UserBaseVO(user);
        }
        this.baseUserInfo = userBaseVO;

        this.groupInfo = groupInfo;

        PidUserInfo pidUserInfo = new PidUserInfo();
        if (pUser != null) {
            pidUserInfo.setSalesmanId(salesman.getPid());
            pidUserInfo.setUserId(pUser.getUserId());

            UserBaseVO pUserBase = new UserBaseVO(pUser);
            pidUserInfo.setBaseUserInfo(pUserBase);
        }
        this.pidUserInfo = pidUserInfo;

    }
}
