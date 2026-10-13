// 获取详情返回参数类型

export interface MailTemplateFilterResult {
    records: MailTemplateFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface MailTemplateFilterState {
    templateId: number|string;
    templateName: string;

    templateCode?: string;
    isHtml?: number;
    templateSubject?: string;
    templateContent?: string;
    lastModify?: number;
    lastSend?: number;
    type?: string;
}

