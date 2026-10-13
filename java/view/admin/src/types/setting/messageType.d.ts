import type { SmsProviderType } from "./config";

// 列表查询时筛选参数类型
export interface MessageTypeFilterParams {
    page: number,
    size: number,
    sortField: string,
    sortOrder: string,
    keyword?: string;
    sendType?: number;
}

// 获取列表返回参数类型
export interface MessageTypeFilterResult {
    records: MessageTypeFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface MessageTypeFilterState {
    messageId: number;
    name?: string;
    describe?: string;
    isWechat?: number;
    isMiniProgram?: number;
    isMessage?: number;
    isMsg?: number;
    isApp?: number;
    isDing?: number;
    addTime?: number;
}


// 获取详情返回参数类型
export interface MessageTypeFormState {
    messageId?: number;
    name?: string;
    describe?: string;
    smsProvider?: SmsProviderType | string | number | null;
    isWechat?: number;
    isMiniProgram?: number;
    isMessage?: number;
    isMsg?: number;
    isApp?: number;
    isDing?: number;
    isMail?: number;
    isWxWork?: number;
    addTime?: number;
    // 阿里云短信通道开关（复选：1=+86国内、2=其他海外、1,2=两者）；用于判定海外短信模板是否可编辑
    smsAliyunChannel?: string;
    templateMessage:TemplateMessageState;
}
export interface TemplateMessageState{
    wechatData: TemplateMessageStateItem;
    appData: TemplateMessageStateItem;
    dingData: TemplateMessageStateItem;
    messageData: TemplateMessageStateItem;
    miniProgramData: TemplateMessageStateItem;
    msgData: TemplateMessageStateItem;
    emailData: TemplateMessageStateItem;
    wxWorkData: TemplateMessageStateItem;

}

export interface TemplateMessageStateItem{
    content?: string;
    id?: number;
    messageId?: number;
    templateId?: string;
    // 海外短信明文文案（国际/港澳台，仅阿里云海外通道 SendMessageToGlobe 使用）
    contentOversea?: string;
    templateName?: string;
    templateNum?: string;
    toUserid?: string;
    type?: number;
    typeName?: string;
}
