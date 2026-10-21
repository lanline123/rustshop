// 列表筛选
export interface ArticleCategoryFilterParams {
    page: number;
    size: number;
    sortField?: string,
    sortOrder?: string,
    keyword?: string;
    parentId: number;
}

export interface ArticleCategoryFilterResult {
    records: ArticleCategoryFilterState[];
    filter: {
        page: number;
    };
    parentName?: string;
    total: number;
}

export interface ArticleCategoryFilterState {
    articleCategoryId: number;
    articleCategoryName?: string;
    categorySn?: string;
    categoryType?: number;
    description?: string;
    keywords?: string;
    parentId?: number;
    sortOrder?: number;
    children?: ArticleCategoryFilterState[];
}

export interface ArticleCategoryFormState {
    articleCategoryId?: number;
    articleCategoryName?: string;
    parentId?: number;
    keywords?: string;
    sortOrder?: number;
    description?: string;
}

export interface FormState {
    articleCategoryName: string;
    parentId: any;
    keywords: string;
    description: string;
    sortOrder: number;
}


