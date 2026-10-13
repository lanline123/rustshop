package com.tigshop.bean.vo.workbuddy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 商品推广链接。
 *
 * <p>不含海报：海报由后台前端用 Canvas 现场合成，后端不产出图片。
 * 该事实必须让调用方知道，否则用户会反复索要一个根本不存在的东西。
 */
@Data
@Schema(description = "WorkBuddy商品推广链接")
public class PromotionLinksVO {

    private Integer productId;

    private String productName;

    private String h5ShortLink;

    private String h5OriginalLink;

    private String pcShortLink;

    private String pcOriginalLink;

    /** 小程序码，形如 {@code data:image/png;base64,...} 的内联图片，不是可访问的 URL。 */
    private String miniCode;

    private String wechatShortLink;

    /**
     * 微信短链生成失败的原因。
     *
     * <p>非空即代表 {@link #wechatShortLink} 不可用。必须如实转述——
     * 微信短链依赖公众号配置，失败是常态，假装成功比直接说失败更糟。
     */
    private String wechatShortLinkError;

    /** 海报是否可由后端提供。恒为 false，显式给出以免调用方揣测。 */
    private boolean posterAvailable;
}
