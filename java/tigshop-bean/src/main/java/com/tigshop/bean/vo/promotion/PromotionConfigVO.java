/**
 * ---------------------------------------------------------------------+
 * 文件 -- CouponConfigVO
 * ---------------------------------------------------------------------+
 * 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
 * ---------------------------------------------------------------------+
 * 作者：Tigshop团队，yq@tigshop.com
 * ---------------------------------------------------------------------+
 * 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
 * ---------------------------------------------------------------------+
 */
package com.tigshop.bean.vo.promotion;

import com.tigshop.bean.model.user.UserRank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Tigshop团队
 */
@Getter
@Setter
@Schema(description = "营销活动配置项")
public class PromotionConfigVO {

    private List<UserRank> rankList;

    public PromotionConfigVO(List<UserRank> rankList) {
        this.rankList = rankList;
    }

    // @Schema(description = "会员等级等参数")
    // private PromotionItem item;
    //
    // @Data
    // @Schema(description = "会员等级等参数")
    // static class PromotionItem{
    //     //     private List<UserRank> rankList;
    //
    //     public PromotionItem(List<UserRank> rankList){
    //         this.rankList = rankList;
    //     }
    // }
    //
    // public PromotionConfigVO(List<UserRank> rankList) {
    //     this.item = new PromotionConfigVO.PromotionItem(rankList);
    // }
}
