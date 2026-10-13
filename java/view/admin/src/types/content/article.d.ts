// 列表查询时筛选参数类型
export interface ArticleFilterParams {
    page: number;
    size: number;
    sortField: string;
    sortOrder: string;
    keyword?: string;
    articleCategoryId?: string;
    articleIds?: string;
    isShow?: number | string;
    isHot?: number | string;
}

// 获取列表返回参数类型
export interface ArticleFilterResult {
    records: ArticleFilterState[];
    filter: {
        page: number;
    };
    total: number;
}
export interface ArticleFilterState {
    articleId: number;
    articleTitle?: string;
    articleSn?: string;
    isHot?: number | string;
    isShow?: number;
    addTime?: string;
}

// 获取详情返回参数类型
export interface ArticleFormState {
    articleId?: number;
    articleTitle?: string;
    articleCategoryId?: number[];
    articleCategoryName?: string;
    articleSn?: string;
    articleThumb?: string;
    articleAuthor?: string;
    keywords?: string;
    articleTag?: string;
    description?: string;
    link?: string;
    articleType?: number;
    isHot?: number;
    isTop?: number;
    isShow?: number;
    content?: string;
    productIds: number[];
}
