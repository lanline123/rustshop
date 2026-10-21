import { ref, reactive, computed, unref, ComputedRef } from 'vue';
import { message } from 'ant-design-vue';
import { useConfigStore } from '@/store/config';
import { useI18n } from "vue-i18n"

interface BaseFilterParams {
    page?: number;
    size?: number;
    keyword?: string;
    groupName?: string;
    sortField?: string;
    sortOrder?: string;
}

export function useListRequest<T, P extends BaseFilterParams>(options: {
    defaultParams?: P;
    apiFunction: ((params: P) => Promise<{ records: T[]; total: number }>) |
    ComputedRef<(params: P) => Promise<{ records: T[]; total: number }>>;
    getPageSize?: () => number;
    idKey?: string;
},ignoreResetParam: string[] = []) {
    const { t } = useI18n()
    const config = useConfigStore();
    const pageSize = options.getPageSize ? options.getPageSize() : config.get('pageSize');
    const idKey = options.idKey || 'id';

    // 保存初始参数，用于重置
    const initialParams = {
        page: 1,
        size: pageSize,
        ...options.defaultParams
    } as P;

    // 状态管理
    const listData = ref<T[]>([]);
    const loading = ref<boolean>(false);
    const total = ref<number>(0);
    const selectedIds = ref<(number | string)[]>([]);

    // 过滤参数
    const filterParams = reactive<P>({ ...initialParams });

    // 处理动态API函数
    const resolvedApiFunction = computed(() => {
        const fn = unref(options.apiFunction);
        return typeof fn === 'function' ? fn : () => Promise.reject('API function is invalid');
    });

    // 新增重置参数方法
    const resetParams = () => {
        if(ignoreResetParam.length == 0) {
            Object.assign(filterParams, initialParams);
        } else {
            for (let key in initialParams) {
                if (!ignoreResetParam.includes(key)) {
                    // @ts-expect-error
                    filterParams[key] = initialParams[key];
                }
            }
        }
        loadData();
    };

    // 加载数据
    const loadData = async () => {
        loading.value = true;
        try {
            if (filterParams.keyword) {
                filterParams.keyword = filterParams.keyword.trim();
            }
            if (filterParams.groupName) {
                filterParams.groupName = filterParams.groupName.trim();
            }
            const result = await resolvedApiFunction.value(filterParams);
            listData.value = result.records;
            total.value = result.total;
        } catch (error: any) {
            message.error(error.message || t('请求失败'));
        } finally {
            loading.value = false;
        }
    };

    // 其他方法保持不变...
    const onSearchSubmit = () => {
        filterParams.page = 1;
        loadData();
    };

    const onSortChange = ({ prop, order }: { prop: string; order?: string }) => {
        filterParams.sortField = prop;
        filterParams.sortOrder =
            order === 'ascending' ? 'asc' :
                order === 'descending' ? 'desc' : '';
        loadData();
    };

    const onPageChange = (page: number, size: number) => {
        filterParams.page = page;
        filterParams.size = size;
        loadData();
    };

    const onSelectChange = (items: T[]) => {
        console.log(items, 9999);
        selectedIds.value = items.map((item: any) => item[idKey]);
        console.log(selectedIds.value);
    };

    const onBatchAction = async (action: string, apiFunction: (type: string, data: object) => Promise<any>, args?: any) => {
        if (selectedIds.value.length === 0) {
            message.warning(t('请至少选择一项'));
            return;
        }
        try {
            await apiFunction(action, { ids: selectedIds.value, ...args });
            message.success(t('操作成功'));
            loadData();
            selectedIds.value = [];
        } catch (error: any) {
            message.error(error.message || t('操作失败'));
        }
    };

    return {
        listData,
        loading,
        total,
        selectedIds,
        filterParams,
        loadData,
        onSearchSubmit,
        onSortChange,
        onPageChange,
        onSelectChange,
        onBatchAction,
        resetParams
    };
}
