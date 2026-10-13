import exp from "constants";

// 列表查询时筛选参数类型
export interface UserFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    rankName?: string;
}

export interface UserFilterResult {
    userRank: {
        records: UserFilterState[];
    }
    filter: UserFilterParams;
    rankConfig:{
        type: number;
        rankAfterMonth: number;
        useMonth: number;
    },
    total: number;
}
interface GrowUpSetting {
    buyOrder: number;
    buyOrderNumber: number;
    buyOrderGrowth: number;
    evpi: number;
    evpiGrowth: number;
    bindPhone: number;
    bindPhoneGrowth: number;
}
// 获取列表返回参数类型
export interface UserFilterState {
    userRights?: string; //会员权益
    userCount?: number; //会员数
    rankId?: number; //会员等级id 
    rankName?: string; //会员等级名称
    rankLogo?: string; //会员等级名称
    minGrowthPoints?: number; //该等级的最低成长值
    maxGrowthPoints?: number; //该等级的最高成长值
    discount?: number; //该会员等级的商品折扣
    showPrice?: number; //价格
    rankType?: number; //等级类型：1：根据成长值变化，2：固定等级
    rankIco?: string; //会员等级图标
    rankBg?: string; //会员等级背景图
    rankPoint?: number; //权益积分
    freeShipping?: number; //是否包邮
    rights?: any[] //权益
}


export interface UserFormResult {
    item: UserFormState;
}
// 获取详情返回参数类型
export interface UserFormStatePro {
    rankType?: number; //等级类型：1：根据成长值变化，2：固定等级
    data: DataFormParamsPro[];
    userRankConfig:{
        type: number;
        rankAfterMonth: number;
        useMonth: number;
    },
    growUpSetting: GrowUpSetting;
}
export interface DataFormParamsPro {
    rankId?: number; //会员等级id 
    rankLevel?: number; //会员等级
    rankName?: string; //会员等级名称
    rankLogo?: string; //会员等级名称
    minGrowthPoints?: any; //升级条件
    discount?: number | string; //该会员等级的商品折扣
    rankCardType?: number; //会员卡背景类型：1：背景色，2：自定义图片
    rankIco?: string; //会员等级背景色黑金会员
    rankBg?: string; //会员等级背景图
    rankPoint?: number | string; //多倍积分
    freeShipping?: number; //是否包邮
    rights: any[] //权益
}


// 获取详情返回参数类型
export interface UserFormState {
    data: DataFormParams[];
}
export interface DataFormParams {
    rankId?: number; //会员等级id 
    rankLevel?: number; //会员等级
    rankName?: string; //会员等级名称
    rankLogo?: string; //会员等级名称
}
