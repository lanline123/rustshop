export interface CommonCleanupFormState {
    errcode: number;
    message: string;
}

export interface UserInfoObject {
    adminId: number;
    username: string;
    mobile: string;
    avatar: string;
    email: string;
    addTime: string;
    authList: string[];
    suppliersId: number;
    roleId: number;
    shopId: number;
    parentId: number;
    menuTag?: any;
    orderExport: string[];
    extra?: any;
}

export interface ConfigObject {
    icoDefinedCss: string;
    dollarSign: string;
    storageType: number;
    storageUrl: string;
    pcDomain: string;
    h5Domain: string;
}

export interface Children {
    authorityId: number;
    isShow: number;
    authoritySn: string;
    authorityName: string;
    parentId: number;
    authorityIco: string;
    routeLink: string;
    childAuth: any[];
    children: any[];
    current?: boolean;
    blank?: string;
}

export interface MainMenu {
    authorityId: number;
    active?: boolean;
    isShow: number;
    authoritySn: string;
    authorityName: string;
    parentId: number;
    authorityIco: string;
    routeLink: string;
    childAuth: any[];
    children: Children[];
    current?: boolean;
    blank?: string;
}

