/* 分类树结构 */
export interface DataResponse {
    item: Item;
    descArr: DescArr[];
    skuList: any[];
    picList: PicList[];
    attrList: DataResponseAttrList;
    rankDetail: RankDetail;
    code: number;
    message: string;
}

export interface filterSeleted {
    categoryId: number;
    categoryName: stringnumber;
    keywords?: stringnumber;
    categoryDesc?: stringnumber;
    nparentId?: number;
    sortOrder?: number;
    measureUnit?: stringnumber;
    isShow?: number;
    seoTitle?: stringnumber;
    shortName?: stringnumber;
    categoryPic?: stringnumber;
    categoryIco?: number;
    isHot?: number;
    searchKeywords?: string;
    children?: filterSeleted[];
}

export interface SearchFilterResult {
    data: filterSeleted[];
    code: number;
    message: string;
}
