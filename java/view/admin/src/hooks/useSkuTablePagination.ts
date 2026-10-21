/**
 * @file SKU 明细表格前端分页 Hook
 * @description
 *   统一「商品编辑页规格明细 / 供应商商品明细 / 列表快捷改库存弹窗」三处的前端分页逻辑，
 *   避免「绝对下标 / 越界修正 / 跳错误行」这类易错点在多处各写一遍、改一处漏多处。
 *
 *   职责单一：只负责分页 + 绝对下标，不管筛选（筛选留在各组件，把「已筛选的 viewList」传进来即可）。
 *
 * @example
 *   const { currentPage, pageSize, paginationEnabled, pagedList, rowIndex, goToIndex, resetPage } =
 *       useSkuTablePagination(filteredList, { fullList: () => productList.value, enabled: () => skuType.value === 2 });
 *
 * @module hooks/useSkuTablePagination
 */
import { ref, computed, watch, type Ref, type ComputedRef } from "vue";
import { SKU_PAGINATION_THRESHOLD } from "@/utils/skuPerfMode";

/** SKU 明细表格默认每页条数 */
export const SKU_PAGE_SIZE_DEFAULT = 50;
/** SKU 明细表格可选每页条数（去掉 10/15；50~100 间增加 60、80） */
export const SKU_PAGE_SIZES = [20, 30, 50, 60, 80, 100] as const;

/** 列表数据源：ref 或 getter */
type ListSource<T> = Ref<T[]> | (() => T[]);

const readList = <T>(src: ListSource<T>): T[] => (typeof src === "function" ? src() : src.value) || [];

export interface SkuTablePagination<T> {
    /** 当前页（1 起） */
    currentPage: Ref<number>;
    /** 每页条数 */
    pageSize: Ref<number>;
    /** 是否启用分页（额外 enabled 门控 && 条数超阈值） */
    paginationEnabled: ComputedRef<boolean>;
    /** viewList（已筛选）总条数 */
    total: ComputedRef<number>;
    /** 当前页数据（未启用分页时返回完整 viewList） */
    pagedList: ComputedRef<T[]>;
    /** 行 → 在 fullList 中的绝对下标（供 el-form-item :prop / 校验器定位真实行） */
    rowIndex: (row: T) => number;
    /** 跳到「viewList 中该下标」所在页（调用方需确保该下标在当前 viewList 内，如先清空筛选） */
    goToIndex: (index: number) => void;
    /** 复位到第一页 */
    resetPage: () => void;
}

/**
 * @param viewList 展示用列表（调用方已做好筛选；无筛选则直接传全量）
 * @param options.fullList 用于计算绝对下标的全量列表，缺省时用 viewList
 * @param options.threshold 启用分页的条数阈值，默认 SKU_PAGINATION_THRESHOLD(50)
 * @param options.pageSize 初始每页条数，默认 SKU_PAGE_SIZE_DEFAULT(50)
 * @param options.enabled 额外启用门控（如 skuType === 2），默认 true
 */
export function useSkuTablePagination<T extends object>(
    viewList: ListSource<T>,
    options: {
        fullList?: ListSource<T>;
        threshold?: number;
        pageSize?: number;
        enabled?: Ref<boolean> | (() => boolean);
    } = {}
): SkuTablePagination<T> {
    const threshold = options.threshold ?? SKU_PAGINATION_THRESHOLD;
    const currentPage = ref(1);
    const pageSize = ref(options.pageSize ?? SKU_PAGE_SIZE_DEFAULT);

    const readEnabled = (): boolean => {
        const e = options.enabled;
        if (e == null) return true;
        return typeof e === "function" ? e() : e.value;
    };

    const view = computed(() => readList(viewList));
    const fullGetter = (): T[] => (options.fullList ? readList(options.fullList) : view.value);

    const total = computed(() => view.value.length);
    const paginationEnabled = computed(() => readEnabled() && total.value > threshold);

    const pagedList = computed(() => {
        if (!paginationEnabled.value) return view.value;
        const start = (currentPage.value - 1) * pageSize.value;
        return view.value.slice(start, start + pageSize.value);
    });

    // 行 → 绝对下标（针对 fullList），用 Map 保证 O(1)；fullList 整体替换时重建
    const rowIndexMap = ref<Map<T, number>>(new Map());
    watch(
        fullGetter,
        (list) => {
            const m = new Map<T, number>();
            (list || []).forEach((r, i) => m.set(r, i));
            rowIndexMap.value = m;
        },
        { immediate: true }
    );
    const rowIndex = (row: T): number => {
        const i = rowIndexMap.value.get(row);
        return i == null ? fullGetter().indexOf(row) : i;
    };

    // 条数/页大小变化后修正当前页越界
    watch([total, pageSize], () => {
        const maxPage = Math.max(1, Math.ceil(total.value / pageSize.value));
        if (currentPage.value > maxPage) currentPage.value = maxPage;
    });

    const goToIndex = (index: number) => {
        currentPage.value = Math.floor(index / pageSize.value) + 1;
    };
    const resetPage = () => {
        currentPage.value = 1;
    };

    return { currentPage, pageSize, paginationEnabled, total, pagedList, rowIndex, goToIndex, resetPage };
}
