export interface PrintListParams {
    page?: number,
    size?: number,
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    id?:number;
    printName?: string;
    printSn?: string;
    status?: 1 | 2;
    platform?: number; 
    deviceCode?: string;
    /** Backend contract: binding is submitted as an array; UI currently does not expose multi-device binding. */
    deviceCodes?: string[];
}

export interface PrintListResponse {
    records: PrintListItem;
    total: number;
    size: number;
    current: number;
    pages: number;
}

export interface PrintListItem {
   addTime?: string;
   platformText?: string;
   printId?: number;
   printName?: string;
   printSn?: string;
   status?: number;
   statusText?: string;
   updateTime?: string;
   /** 绑定的收银设备码；deviceName 由后端可选回传，缺省时前端按设备码补名称 */
   deviceCode?: string;
   deviceName?: string;
}

export interface PrintDetailParams {
    id:number;
}

export interface PrintUdateStatusParams {
   field: string;
   id: number;
   val: string;
}

export interface PrintDeleteParams {
    id:number;
    adminNote?: string;
}

export interface PrintConfigParams {
    printId: number;
}

export interface PrintDetailResponse {
    addTime?: string;
    platform?: number;
    platformText?: string;
    printId?: number;
    printKey?: string;
    printName?: string;
    printNumber?: number;
    printSn?: string;
    shopId?: number;
    status?: number;
    statusText?: string;
    thirdAccount?: string;
    thirdKey?: string;
    autoPrint?: number;
    voiceEnabled?: number;
    voiceContentType?: number;
    updateTime?: string;
    deviceCode?: string;
    deviceName?: string;
}

/**
 * 创建/更新打印机。
 * platform：1=飞鹅云 2=商米云；不传时后端默认 1。
 * 商米云与飞鹅云共用 thirdAccount / thirdKey / printSn / printKey：
 * 商米云语义为 AppID / AppSecret（AppKey）/ 打印机 SN / printKey 传空字符串。
 */
export interface PrintCreateParams {
    printId: number | undefined;
    platform?: number;
    printName: string;
    printNumber?: number;
    status: number;
    autoPrint: number;
    voiceEnabled?: number;
    voiceContentType?: number;
    printKey?: string;
    printSn?: string;
    thirdAccount?: string;
    thirdKey?: string;
    /** 绑定的收银设备码，非必填；空字符串表示解绑 */
    deviceCode?: string;
}


export interface PrintConfigResponse {
    addTime?: string;
    id?: number;
    printId?: number;
    template?: PrintTemplateItem[];
    type?: number;
    typeDesc?: string;
    updateTime?: string;
    paperSize?: 58 | 80;
}

export interface PrintTemplateItem {
    options?: PrintTemplateOption[];
    title?: string;
}

export interface PrintTemplateOption {
    choose?: boolean;
    chooseTitle?: string;
    templateValue?: string;
    value?: string;
}
