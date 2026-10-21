package com.tigshop.bean.dto.content;

import com.tigshop.common.core.entity.BasePage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 微信直播商品主表列表查询参数
 */
@Getter
@Setter
@Schema(description = "微信直播商品主表列表查询参数")
public class WechatLiveGoodsListDTO extends BasePage {
}
