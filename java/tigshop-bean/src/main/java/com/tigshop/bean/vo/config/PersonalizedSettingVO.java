package com.tigshop.bean.vo.config;

import com.tigshop.bean.annotations.settings.ConfigItemField;
import com.tigshop.bean.enums.setting.SettingsEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author Tigshop项目组
 * @create 2025年09月23日 14:51
 */
@Schema(description = "个性化设置结果")
@Data
public class PersonalizedSettingVO {
    @ConfigItemField(SettingsEnum.GUEST_LIKE_GOODS_NAME)
    @Schema(description = "猜你喜欢定义名称")
    private String guestLikeGoodsName;

    @ConfigItemField(SettingsEnum.IS_GUEST_LIKE_GOODS)
    @Schema(description = "是否开启猜你喜欢，0-不开启，1-开启")
    private Integer isGuestLikeGoods;

    @ConfigItemField(SettingsEnum.DEFAULT_AVATAR)
    @Schema(description = "会员默认头像")
    private String defaultAvatar;

    @ConfigItemField(SettingsEnum.SEARCH_SELF_STORE)
    @Schema(description = "是否搜本店")
    private Integer searchSelfStore;

    @ConfigItemField(SettingsEnum.INDEX_SEARCH_SHOP)
    @Schema(description = "首页搜索店铺")
    private Integer indexSearchShop;
}