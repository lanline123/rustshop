package com.tigshop.bean.feign.wechat;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Tigshop项目组
 * @create 2025年09月22日 09:59
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkResult extends WechatApiResult {
    private String link;
}