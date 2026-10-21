/**
 * 搜索筛选公共工具 / composables
 * 国内抽屉、海外弹层、品牌/属性面板共用品牌切换与属性多选逻辑。
 */
import { ref, type Ref } from "vue";
import type { Brand, attrsData } from "@/types/search/search";

/** 属性值可能是字符串或 { value, count } 对象 */
export function getAttrOptionText(value: { value?: string } | string): string {
    return typeof value === "string" ? value : value?.value || "";
}

/** 切换品牌 ID 选中态（原地修改数组） */
export function toggleBrandId(brandIds: number[], brandId: number): void {
    const index = brandIds.indexOf(brandId);
    if (index !== -1) {
        brandIds.splice(index, 1);
    } else {
        brandIds.push(brandId);
    }
}

export function isAttrSelected(attrs: attrsData[], attrName: string, value: string): boolean {
    const attr = attrs.find((item) => item.attrName === attrName);
    return attr ? attr.attrValue.includes(value) : false;
}

/** 切换属性值选中态；取消后若该属性无值则移除整项 */
export function toggleAttrValue(attrs: attrsData[], attrName: string, value: string): void {
    const attrIndex = attrs.findIndex((item) => item.attrName === attrName);
    if (attrIndex > -1) {
        const valueIndex = attrs[attrIndex].attrValue.indexOf(value);
        if (valueIndex > -1) {
            attrs[attrIndex].attrValue.splice(valueIndex, 1);
            if (attrs[attrIndex].attrValue.length === 0) {
                attrs.splice(attrIndex, 1);
            }
        } else {
            attrs[attrIndex].attrValue.push(value);
        }
    } else {
        attrs.push({
            attrName,
            attrValue: [value]
        });
    }
}

/** 基于 brandIds ref 的品牌点击切换 */
export function useBrandSelection(brandIds: Ref<number[]>) {
    const onChangeBrand = (item: Brand) => {
        toggleBrandId(brandIds.value, item.brandId);
    };
    return { onChangeBrand };
}

/** 基于 attrs ref 的属性多选 */
export function useAttrsSelection(attrs: Ref<attrsData[]>) {
    const checkSelected = (attrName: string, value: string) => isAttrSelected(attrs.value, attrName, value);
    const onAttrClick = (attrName: string, value: string) => toggleAttrValue(attrs.value, attrName, value);
    return {
        isAttrSelected: checkSelected,
        onAttrClick
    };
}

/**
 * 区块展开/收起（品牌、属性分组等）
 * @param defaultExpanded true=未写入时视为展开；false=默认收起
 */
export function useSectionExpand(defaultExpanded = false) {
    const expandMap = ref<Record<string, boolean>>({});

    const isExpanded = (key: string): boolean => {
        if (expandMap.value[key] === undefined) {
            return defaultExpanded;
        }
        return expandMap.value[key];
    };

    const toggleExpand = (key: string) => {
        expandMap.value[key] = !isExpanded(key);
    };

    const resetExpand = (keys: string[] = [], expanded = defaultExpanded) => {
        const map: Record<string, boolean> = {};
        keys.forEach((key) => {
            map[key] = expanded;
        });
        expandMap.value = map;
    };

    return {
        expandMap,
        isExpanded,
        toggleExpand,
        resetExpand
    };
}
