package com.tigshop.bean.model.decorate;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tigshop.bean.enums.decorate.DecorateType;
import com.tigshop.bean.enums.decorate.DecoratePageType;
import com.tigshop.common.constant.decorate.InitDecorateDataConstants;
import com.tigshop.common.utils.StringUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 装修页面管理model
 *
 * @author Tigshop团队
 * @create 2024年12月03日 16:56
 */
@Data
@TableName("decorate")
@Schema(description = "页面管理")
public class Decorate {

    @TableId(value = "decorate_id", type = IdType.AUTO)
    @Schema(description = "页面管理ID")
    private Integer decorateId;

    @Schema(description ="模板名称")
    private String decorateTitle;

    @Schema(description ="已发布的页面模块数据")
    private String data;

    @Schema(description ="草稿页面模块数据")
    private String draftData;

    @Schema(description ="页面类型 1:h5 2:PC")
    private Integer decorateType;

    @Schema(description ="页面用途类型，详见 DecoratePageType")
    private Integer pageType;

    @Schema(description ="是否是首页")
    private Integer isHome;

    @Schema(description ="所属店铺")
    private Integer shopId;

    @Schema(description ="状态 1:发布 0:未发布")
    private Integer status;

    @Schema(description ="更新时间")
    private Long updateTime;

    @Schema(description = "父级id")
    private Integer parentId;

    @Schema(description = "语言ID")
    private Integer localeId;

    /**
     * O2O门店首页的页面类型：shop_id=0 时平台级装修（商城首页、个人中心等）也落在同一批查询条件上，
     * 用独立的 9 与商品详情页隔离；shop_id>0 的门店不存在该碰撞，沿用 1
     */
    public static Integer resolveO2oHomePageType(Integer shopId) {
        return shopId == null || shopId == 0 ? DecoratePageType.O2O_PLATFORM_HOME.getCode() : DecoratePageType.HOME.getCode();
    }

    public static Decorate createDefaultO2OH5(Integer shopId){
        Decorate decorate = new Decorate();
        decorate.setDecorateTitle("o2o门店首页装修");
        decorate.setDecorateType(DecorateType.H5.getCode());
        decorate.setData(InitDecorateDataConstants.O2O_H5_DECORATE_DATA);
        decorate.setPageType(resolveO2oHomePageType(shopId));
        decorate.setIsHome(1);
        decorate.setStatus(1);
        decorate.setParentId(0);
        decorate.setUpdateTime(StringUtils.getCurrentTime());
        decorate.setLocaleId(0);
        decorate.setShopId(shopId);
        return decorate;
    }

    @Schema(description = "分享信息（JSON：shareTitle/shareDesc/shareImage）")
    private String shareInfo;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "绑定的H5页面ID")
    private Integer bindedH5Id;
}
