// 首页
export interface HomeDataType {
    decorateId?: number;
    moduleList?: ModuleListType[];
}
export interface ModuleListType {
    module?: {};
    moduleIndex?: number;
    isShow?: boolean;
    active?: boolean;
    type?: string;
    style?: number;
}

export interface BaseResponseListWrap<T> {
    current: number;
    pages: number;
    records: T;
    size: number;
    total: number;
}
