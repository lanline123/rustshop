// 列表查询时筛选参数类型
export interface DecorateDiscreteFormState {
    backgroundColor?: string;
    backgroundPic?: {
        picUrl?: string;
        picThumb?: string;
    };
    navList?: {
        picUrl?: string;
        picThumb?: string;
        picActiveUrl?: string;
        picActiveThumb?: string;
        picLink?: {
            link?: string;
            title?: string;
        };
        picTitle?: string;
        picDesc?: string;
    }[];
}

// 获取详情返回参数类型
export interface FormResult {
    item: {
        data?: DecorateDiscreteFormState[];
    };
}

type menuType =  1|2
export interface DecorateUserMenu {
    menuType: menuType;
    menuList: menuList[];
}
export interface Pic_link {
	path: string;
	label: string;
	name: string;
	link: string;
}
export interface menuList {
	type?: string;
	picTitle?: string;
	picThumb?: string;
	picUrl?: string;
	picLink?: Pic_link;
}

export interface DecorateUserBanner {
    bannerList:{
        picId?: number;
        picThumb?: string;
        picUrl: string;
        picName?: string;
    }
}
type moduleType = DecorateUserMenu | DecorateUserBanner;
export interface DecorateUserFormState {
    type:string;
    label:string;
    module: moduleType;
}

