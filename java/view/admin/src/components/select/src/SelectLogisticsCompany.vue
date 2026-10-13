/**
 * @file 物流公司选择器组件
 * @description 支持远程搜索和分页加载的物流公司下拉选择器
 * @features
 * - 远程关键字搜索
 * - 分页加载更多
 * - 搜索图标常驻显示
 */
<template>
    <el-select
        v-model="selectedLogisticsId"
        class="full-width logistics-select search-select"
        filterable
        remote
        :remote-method="remoteSearch"
        :loading="loading"
        :suffix-icon="Search"
        @change="emit('onChange', $event)"
        :placeholder="$t('请选择物流公司')"
        :remote-show-suffix="true"
        :empty-values="[0, undefined, null, '']"
    >
        <el-option :value="0" :label="$t('请选择...')"></el-option>
        <el-option v-for="company in logisticsCompanies" :key="normalizeLogisticsId(company.logisticsId)" :label="company.logisticsName" :value="normalizeLogisticsId(company.logisticsId)" />
        <!-- 加载更多选项，放在列表末尾 -->
        <el-option v-if="loadingMore" disabled value="__loading__" class="load-more-option">
            <span class="load-more-text">{{ $t('加载中...') }}</span>
        </el-option>
        <el-option v-else-if="hasMore && logisticsCompanies.length > 0" disabled value="__loadmore__" class="load-more-option" @click.stop="loadMore">
            <span class="load-more-text" @click.stop="loadMore">{{ $t('加载更多...') }}</span>
        </el-option>
        <el-option v-else-if="!hasMore && logisticsCompanies.length > 0" disabled value="__nomore__" class="load-more-option">
            <span class="no-more-text">{{ $t('没有更多数据了') }}</span>
        </el-option>
    </el-select>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref, watch } from "vue";
import { Search } from "@element-plus/icons-vue";
import { message } from "ant-design-vue";
import { getLogisticsCompanyAll } from "@/api/setting/logisticsCompany";
import { getLogisticsCompanyConfigAll } from "@/api/setting/logisticsCompanyConfig";
import { getLogisticsProvider, LOGISTICS_PROVIDER_KDNIAO } from "@/api/setting/baseLogistics";
import type { LogisticsProvider } from "@/types/setting/baseLogistics.d";
import type { LogisticsCompanyFilterState } from "@/types/setting/logisticsCompany";
import type { LogisticsCompanyConfigFilterState } from "@/types/setting/logisticsCompanyConfig";

type LogisticsOption = LogisticsCompanyFilterState | LogisticsCompanyConfigFilterState;

const props = withDefaults(
    defineProps<{
        source?: "global" | "tenant-config";
        autoSelectDefault?: boolean;
        isEnabled?: number;
    }>(),
    {
        source: "global",
        autoSelectDefault: false,
    }
);

/** 选中的物流公司ID */
const logisticsId = defineModel<any>("logisticsId");
const legacyId = defineModel<any>("id");
const emit = defineEmits(["onChange"]);

const normalizeModelValue = (value: any) => {
    if ([undefined, null, "", 0, "0"].includes(value)) {
        return value;
    }
    const numericValue = Number(value);
    return Number.isNaN(numericValue) ? value : numericValue;
};

const selectedLogisticsId = computed({
    get: () => normalizeModelValue(logisticsId.value ?? legacyId.value),
    set: (value) => {
        const normalizedValue = normalizeModelValue(value);
        logisticsId.value = normalizedValue;
        legacyId.value = normalizedValue;
    },
});

/** 物流公司列表 */
const logisticsCompanies = ref<LogisticsOption[]>([]);
/** 初次加载状态 */
const loading = ref(false);
/** 加载更多状态 */
const loadingMore = ref(false);
/** 当前页码 */
const currentPage = ref(1);
/** 每页数量 */
const pageSize = 100;
/** 总数 */
const total = ref(0);
/** 是否还有更多数据 */
const hasMore = ref(true);
/** 搜索关键字 */
const keyword = ref("");
/** 全局物流服务商，用于只列出支持该服务商的物流公司 */
const provider = ref<LogisticsProvider>(LOGISTICS_PROVIDER_KDNIAO);

const isEmptyValue = (value: any) => [undefined, null, "", 0, "0"].includes(value);

const hasSelectedValue = () => !isEmptyValue(selectedLogisticsId.value);

const normalizeLogisticsId = (value: any) => Number(value || 0);

const getListApi = () => {
    return props.source === "tenant-config" ? getLogisticsCompanyConfigAll : getLogisticsCompanyAll;
};

const buildQueryParams = (page: number) => ({
    page,
    size: pageSize,
    keyword: keyword.value,
    providerType: provider.value,
    ...(props.isEnabled !== undefined ? { isEnabled: props.isEnabled } : {}),
});

const tryAutoSelectDefault = (records: LogisticsOption[]) => {
    if (!props.autoSelectDefault || hasSelectedValue() || keyword.value) {
        return;
    }
    const targetItem = records.find((item) => Number(item.isDefault || 0) === 1) || records[0];
    if (targetItem?.logisticsId) {
        selectedLogisticsId.value = targetItem.logisticsId;
        emit("onChange", targetItem.logisticsId);
    }
};

/**
 * 获取物流公司数据
 * @param {number} page - 页码
 * @param {boolean} isLoadMore - 是否为加载更多操作
 */
const fetchData = async (page: number, isLoadMore = false) => {
    if (isLoadMore) {
        loadingMore.value = true;
    } else {
        loading.value = true;
    }

    try {
        const result = await getListApi()(buildQueryParams(page));
        if (isLoadMore) {
            logisticsCompanies.value = [...logisticsCompanies.value, ...result.records];
        } else {
            logisticsCompanies.value = result.records;
            tryAutoSelectDefault(result.records);
        }
        total.value = result.total;
        currentPage.value = page;
        hasMore.value = logisticsCompanies.value.length < total.value;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
        loadingMore.value = false;
    }
};

/**
 * 远程搜索
 * @param {string} query - 搜索关键字
 */
const remoteSearch = (query: string) => {
    keyword.value = query;
    currentPage.value = 1;
    fetchData(1);
};

/**
 * 加载更多数据
 * @description 点击"加载更多"时触发，获取下一页数据并追加到列表
 */
const loadMore = () => {
    if (hasMore.value && !loadingMore.value) {
        fetchData(currentPage.value + 1, true);
    }
};

onMounted(async () => {
    provider.value = await getLogisticsProvider();
    fetchData(1);
});

watch(
    () => props.source,
    () => {
        keyword.value = "";
        currentPage.value = 1;
        logisticsCompanies.value = [];
        fetchData(1);
    }
);
</script>

<style lang="less" scoped>
.full-width {
    width: 100%;
}
</style>

<style lang="less">
// 搜索图标样式 - 禁止旋转
.search-select {
    .el-select__caret {
        // 禁止展开/收起时的旋转动画
        transform: none !important;
        transition: none !important;
        &.is-reverse {
            transform: none !important;
        }
    }
}

.el-select-dropdown {
    .load-more-option {
        &.el-select-dropdown__item {
            display: flex !important;
            justify-content: center !important;
            align-items: center !important;
        }
        .load-more-text {
            color: var(--el-color-primary);
            cursor: pointer;
            &:hover {
                opacity: 0.8;
            }
        }
        .no-more-text {
            color: #999;
            cursor: default;
        }
    }
}
</style>
