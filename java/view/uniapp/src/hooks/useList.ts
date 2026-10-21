import { ref } from "vue";
import { onReachBottom } from "@dcloudio/uni-app";
import getPromotionList from "@/utils/getPromotionList";

/**
 * @description: 列表hook
 * @params api:请求接口
 * @params options:配置项
 * @params options.params:请求参数
 * @params options.path:数据字段
 * @params options.immediate:是否立即请求
 * @params options.needReachBottom:是否需要触底加载
 * @params options.needPromotionList:是否需要获取促销列表
 * @params options.manageData:数据管理函数
 */

interface ListResponse<T> {
    [key: string]: any;
    list?: T[];
    total?: number;
}

interface ListOptions {
    immediate?: boolean;
    needReachBottom?: boolean;
    needPromotionList?: boolean;
    params?: Record<string, any>;
    path?: {
        dataKey?: string;
        totalKey?: string;
        pageKey?: string;
        sizeKey?: string;
    };
    manageData?: (data: any[]) => void;
}

export default function useList<T>(api: (params: Record<string, any>) => Promise<ListResponse<T>>, options: ListOptions) {
    const data = ref<T[]>([]);
    const promotionList = ref<Record<string, any>>({});
    const total = ref(0);
    const isLoading = ref(true);
    const isLoadMore = ref(false);
    const error = ref<Error | null>(null);

    const persistentExtraData = ref<Record<string, any>>({});

    const { immediate = false, needReachBottom = true, needPromotionList = false, params = {}, path = {} } = options;

    const { dataKey = "list", totalKey = "total", pageKey = "page", sizeKey = "size" } = path;

    const omitPaginationKeys = (obj: Record<string, any>) => {
        const cloned = { ...obj };
        // 分页字段不应被“持久化覆盖”，否则会影响触底加载的自增逻辑
        delete cloned[pageKey];
        delete cloned[sizeKey];
        return cloned;
    };

    // 初始化分页参数
    if (!params[pageKey]) {
        params[pageKey] = 1;
    }
    if (!params[sizeKey]) {
        params[sizeKey] = 10;
    }

    const resetState = () => {
        data.value = [];
        total.value = 0;
        error.value = null;
        params[pageKey] = 1;
        persistentExtraData.value = {};
    };

    const getList = async (extraData?: object | (() => object)) => {
        try {
            let transientExtraData: Record<string, any> = {};
            if (extraData !== undefined) {
                const resolvedExtraData = typeof extraData === "function" ? extraData() : extraData;
                transientExtraData = (resolvedExtraData || {}) as Record<string, any>;
                // 如果外部显式传了 page/size，则同步到内部分页状态，保证触底加载从正确页继续
                if (transientExtraData[pageKey] !== undefined) {
                    params[pageKey] = transientExtraData[pageKey];
                }
                if (transientExtraData[sizeKey] !== undefined) {
                    params[sizeKey] = transientExtraData[sizeKey];
                }
                // 仅持久化过滤条件，不持久化 page/size，避免覆盖触底加载分页
                persistentExtraData.value = { ...persistentExtraData.value, ...omitPaginationKeys(transientExtraData) };
            }

            const requestData = {
                ...params,
                ...persistentExtraData.value,
                ...transientExtraData
            };

            if (requestData[pageKey] > 1) {
                isLoadMore.value = true;
            } else {
                isLoading.value = true;
                data.value = []; // 重置数据
            }

            const res = await api(requestData);
            const currentData = dataKey === "" ? res : (res[dataKey] ?? []);

            data.value = requestData[pageKey] === 1 ? currentData : [...data.value, ...currentData];
            total.value = res[totalKey] ?? 0;

            if (needPromotionList && currentData.length) {
                try {
                    const productIds = currentData.map((item: any) => ({
                        productId: item.productId
                    }));
                    const promotionListQuery: any = {
                        products: productIds,
                        from: "list"
                    };
                    // 自营门店使用 shopId=0，不能用 truthy 判断，否则促销接口会漏传门店 ID
                    const shopId = requestData.shopId;
                    if (shopId !== undefined && shopId !== null && shopId !== "") {
                        promotionListQuery.shopId = shopId;
                    }
                    const resPromotion = await getPromotionList(promotionListQuery);
                    promotionList.value = {
                        ...promotionList.value,
                        ...resPromotion
                    };
                } catch (err) {
                    console.error("获取促销信息失败:", err);
                }
            }

            if (options.manageData) {
                options.manageData(data.value);
            }
        } catch (err) {
            error.value = err instanceof Error ? err : new Error(String(err));
            console.error("获取列表数据失败:", err);
        } finally {
            isLoading.value = false;
            isLoadMore.value = false;
        }
    };

    const reachBottom = () => {
        if (!isLoading.value && !isLoadMore.value && params[pageKey] < Math.ceil(total.value / params[sizeKey])) {
            params[pageKey]++;
            getList();
        }
    };

    if (needReachBottom) {
        onReachBottom(reachBottom);
    }

    if (immediate) {
        getList();
    }

    return {
        data,
        promotionList,
        total,
        isLoading,
        isLoadMore,
        error,
        getList,
        reachBottom,
        resetState
    };
}
