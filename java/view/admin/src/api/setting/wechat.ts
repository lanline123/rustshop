import type {
    WxMenuVO,
    SaveAndPublishDTO,
    SaveAndPublishVO,
    KeywordRuleListDTO,
    KeywordRuleVO,
    KeywordRuleDetailVO,
    KeywordRuleSaveDTO,
    KeywordRuleSaveVO,
    ReplyPayload,
    PageResult
} from "@/types/setting/wechat.d";
import request from "@/utils/request";

// ---- M1 菜单 ----

export const getWechatMenu = () => {
    return request<WxMenuVO>({
        url: "wechat/menu",
        method: "get"
    });
};

export const saveAndPublishWechatMenu = (data: SaveAndPublishDTO) => {
    return request<SaveAndPublishVO>({
        url: "wechat/menu/saveAndPublish",
        method: "post",
        data
    });
};

// ---- M2 关键字规则 ----

export const listKeywordRule = (params: KeywordRuleListDTO) => {
    return request<PageResult<KeywordRuleVO>>({
        url: "wechat/keywordRule/list",
        method: "get",
        params
    });
};

export const getKeywordRule = (id: number) => {
    return request<KeywordRuleDetailVO>({
        url: "wechat/keywordRule/detail",
        method: "get",
        params: { id }
    });
};

export const saveKeywordRule = (data: KeywordRuleSaveDTO) => {
    return request<KeywordRuleSaveVO>({
        url: "wechat/keywordRule/save",
        method: "post",
        data
    });
};

export const delKeywordRule = (ids: number[]) => {
    return request({
        url: "wechat/keywordRule/del",
        method: "post",
        data: { ids }
    });
};

export const setKeywordRuleEnabled = (ids: number[], enabled: 0 | 1) => {
    return request({
        url: "wechat/keywordRule/setEnabled",
        method: "post",
        data: { ids, enabled }
    });
};

export const getKefuCapability = () => {
    return request<{ kefuEnabled: boolean }>({
        url: "wechat/keywordRule/capability",
        method: "get"
    });
};

// ---- M3 自动回复 ----

export const getSubscribeReply = () => {
    return request<ReplyPayload>({
        url: "wechat/autoReply/subscribe",
        method: "get"
    });
};

export const saveSubscribeReply = (data: ReplyPayload) => {
    return request({
        url: "wechat/autoReply/subscribe/save",
        method: "post",
        data
    });
};

export const getDefaultReply = () => {
    return request<ReplyPayload>({
        url: "wechat/autoReply/default",
        method: "get"
    });
};

export const saveDefaultReply = (data: ReplyPayload) => {
    return request({
        url: "wechat/autoReply/default/save",
        method: "post",
        data
    });
};

export const getLoginSuccessReply = () => {
    return request<ReplyPayload>({
        url: "wechat/autoReply/loginSuccess",
        method: "get"
    });
};

export const saveLoginSuccessReply = (data: ReplyPayload) => {
    return request({
        url: "wechat/autoReply/loginSuccess/save",
        method: "post",
        data
    });
};
