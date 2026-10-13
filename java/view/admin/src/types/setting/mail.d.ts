// 获取详情返回参数类型
export interface MailFormState {
    mailService?: number;
    smtpSsl?: number;
    smtpHost?: string;
    smtpPort?: string;
    smtpUser?: string;
    smtpPass?: string;
    smtpMail?: string;
    mailCharset?: string;
    testMailAddress?: string;
    serviceEMail?: string;
}

export interface DingFormState {
    dingTalkRobotToken?: string;
    dingAppSecret?: string;
}

export interface WXWorkFormState {
    wxworkWebhookUrl?: string;
}