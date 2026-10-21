package com.tigshop.service.promotion;

import com.tigshop.bean.vo.decorate.DecoratePromoteVO;
import com.tigshop.common.exception.GlobalException;
import com.tigshop.service.common.BaseService;
import com.tigshop.bean.model.promotion.ShortUrl;

/**
 * 短链接服务接口
 *
 * @author Tigshop团队
 */
public interface ShortUrlService extends BaseService<ShortUrl> {
    /**
     * 根据短链接编码获取长链接
     *
     * @param code 短链接编码
     * @return 长链接
     */
    String getLongUrlByCode(String code);

    ShortUrl getByCode(String code);

    String getWechatShortLink(String shareLink, Integer id);

    /** 获取指定小程序页面的微信短链；id 为 null 时 shareLink 为完整页面路径。 */
    String getWechatShortLink(String shareLink, Integer id, String pageTitle);

    // 获取网页短链
    String getWebShortLink(String shareLink, Integer id, String shareType);

    /** 获取网页短链；id 为 null 时 shareLink 为完整页面路径。 */
    String getWebShortLinkByAutoId(String shareLink, Integer id, String shareType);


    // 获取网页短链
    String getPcWebShortLink(String shareLink, String id, String shareType);

    /**
     * 根据完整长链接生成装修推广短链（H5 + 微信）。
     * 微信短链生成失败时回退为 H5 短链。
     *
     * @param longUrl 完整长链接，必须包含 scheme 和 host
     * @return 装修推广短链 VO
     */
    DecoratePromoteVO generateDecoratePromoteShortLink(String longUrl);
}
