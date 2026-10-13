// 获取详情返回参数类型

export interface AfterSalesServiceFilterResult {
    records: AfterSalesServiceFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface AfterSalesServiceFilterState {
    templateContent?: string;
    showTemplateContent?: boolean; // 是否显示售后服务，1-显示，0-隐藏
}
