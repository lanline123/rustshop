export type CategoryFeeRuleTargetType = 1 | 2;

export type CategoryFeeRuleScopeType = 1 | 2 | 3;

export type CategoryFeeRuleStatus = 0 | 1;

export interface CategoryFeeRuleFilterParams {
    page?: number;
    size?: number;
    sortField?: string;
    sortOrder?: string;
    keyword?: string;
    targetType?: CategoryFeeRuleTargetType;
    status?: CategoryFeeRuleStatus | "";
}

export interface CategoryFeeRuleSaveBaseParams {
    ruleName: string;
    targetType: CategoryFeeRuleTargetType;
    scopeType: CategoryFeeRuleScopeType;
    serviceFeeRate: number;
    remark?: string;
    status: CategoryFeeRuleStatus;
    categoryIds: number[];
    targetIds?: number[];
}

export type CategoryFeeRuleCreateParams = CategoryFeeRuleSaveBaseParams;

export interface CategoryFeeRuleUpdateParams extends CategoryFeeRuleSaveBaseParams {
    id: number;
}

export interface CategoryFeeRuleUpdateStatusParams {
    id: number;
    status: CategoryFeeRuleStatus;
}

export interface CategoryFeeRuleDeleteParams {
    ids: number[];
}

export interface CategoryFeeRuleFilterState {
    id: number;
    ruleName: string;
    targetType: CategoryFeeRuleTargetType;
    targetTypeName?: string;
    scopeType: CategoryFeeRuleScopeType;
    scopeTypeName?: string;
    precisionLayer?: number;
    serviceFeeRate: number;
    targetCount?: number;
    categoryCount?: number;
    status: CategoryFeeRuleStatus;
    createTime?: string;
}

export interface CategoryFeeRuleCategoryItem {
    categoryId: number;
    categoryName: string;
    categoryPath?: string;
}

export interface CategoryFeeRuleTargetItem {
    targetId: number;
    targetName: string;
}

export interface CategoryFeeRuleFormState extends CategoryFeeRuleFilterState {
    remark?: string;
    categories: CategoryFeeRuleCategoryItem[];
    targets: CategoryFeeRuleTargetItem[];
    modifyTime?: string;
}

export interface CategoryFeeRuleFilterResult {
    records: CategoryFeeRuleFilterState[];
    total: number;
    size?: number;
    current?: number;
    pages?: number;
    page?: number;
    filter?: CategoryFeeRuleFilterParams;
}

export type CategoryFeeRuleMutationResult = string[];
