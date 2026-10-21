<template>
    <div class="switch-default-page">
        <!-- 筛选区域 -->
        <div class="filter-bar">
            <TigInput v-model="filterParams.keyword" width="240px" clearable :placeholder="$t('请输入页面名称')" @keyup.enter="searchSubmit" @clear="searchSubmit">
                <template #append>
                    <el-button @click="searchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                </template>
            </TigInput>
        </div>

        <!-- 表格区域 -->
        <div class="table-area">
            <a-spin :spinning="loading">
                <el-table :data="filterState" row-key="decorateId">
                    <el-table-column :label="$t('页面名称')" prop="decorateTitle" :min-width="200">
                        <template #default="{ row }">
                            <div class="title-cell">
                                <span v-if="row.decorateId === currentRowId && pageTypeIconMap[pageType]" class="default-page-icon">
                                    <SvgIcon :name="pageTypeIconMap[pageType].icon" />
                                </span>
                                <span>{{ row.decorateTitle }}</span>
                                <el-tag
                                    v-if="decorateType === 2 && row.pageType === 1 && row.homeStyleVersion === 'old'"
                                    class="legacy-tag"
                                    size="small"
                                    type="info"
                                    disable-transitions
                                >
                                    旧版
                                </el-tag>
                            </div>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('发布状态')" prop="status" :width="120">
                        <template #default="{ row }">
                            <span class="text-status" :class="{ success: row.status == 1, default: row.status != 1 }">{{ row.status == 1 ? $t('已发布') : $t('未发布') }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" :width="140" align="right">
                        <template #default="{ row }">
                            <a class="btn-link" :class="{ 'is-disabled': row.decorateId === currentRowId }" @click="handleSelect(row)">{{ setAsText }}</a>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
            </a-spin>
        </div>

        <!-- 分页区域 -->
        <div v-if="total > 0" class="pagination-con">
            <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { computed, onMounted, ref, PropType } from "vue";
import { message } from "ant-design-vue";
import { Pagination } from "@/components/list";
import { useConfigStore } from "@/store/config";
import { getDecorateList, setDefaultPage } from "@/api/decorate/mobileDecorate";
import type { FilterState } from "@/types/decorate/mobileDecorate.d";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    currentRow: { type: Object as PropType<Partial<FilterState>>, default: () => ({}) },
    decorateType: { type: Number, default: 1 }
});

const emit = defineEmits(["submitCallback", "okType", "callback"]);
emit("okType", false);

const config: any = useConfigStore();

const loading = ref(false);
const total = ref(0);
const filterState = ref<FilterState[]>([]);

const pageType = computed(() => props.currentRow?.pageType ?? 1);
const currentRowId = computed(() => props.currentRow?.decorateId);

const pageTypeIconMap: Record<number, { icon: string; title: string }> = {
    1: { icon: "decorate-home", title: t('首页') },
    2: { icon: "decorate-user", title: t('个人中心') },
    3: { icon: "decorate-activity", title: t('活动页') },
    4: { icon: "decorate-classify", title: t('分类页') },
    5: { icon: "decorate-login", title: t('登录页') },
    6: { icon: "decorate-sousuo", title: t('搜索页') },
    8: { icon: "decorate-classify", title: t('商品详情页') }
};

const setAsTextMap: Record<number, string> = {
    1: t('设为首页'),
    2: t('设为个人中心'),
    3: t('设为默认活动页'),
    4: t('设为分类页'),
    5: t('设为登录页'),
    6: t('设为搜索页'),
    7: t('设为默认导航'),
    8: t('设为商品详情页')
};

const setAsText = computed(() => setAsTextMap[pageType.value] ?? t('设为默认'));

const filterParams = ref({
    page: 1,
    size: config.get("pageSize"),
    keyword: "",
    decorateType: props.decorateType,
    pageType: pageType.value
});

const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getDecorateList({
            page: filterParams.value.page,
            size: filterParams.value.size,
            keyword: (filterParams.value.keyword || "").trim(),
            decorateType: filterParams.value.decorateType,
            pageType: filterParams.value.pageType
        });
        // 接口会顺带返回 children（多语言子记录），此处列表不展示层级，剥离后避免 el-table 渲染成树形
        filterState.value = (result.records || []).map((item: any) => {
            const { children, ...rest } = item;
            return rest as FilterState;
        });
        total.value = result.total || 0;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

const searchSubmit = () => {
    filterParams.value.page = 1;
    loadFilter();
};

const handleSelect = async (row: FilterState) => {
    if (row.decorateId === currentRowId.value) return;
    try {
        await setDefaultPage({ id: row.decorateId, pageType: pageType.value });
        message.success(t('已成功{value}', { value: setAsText.value }));
        emit("submitCallback", row);
    } catch (error: any) {
        message.error(error.message);
    }
};

const onFormSubmit = () => {
    // 弹窗 footer 已隐藏，OK 按钮不会触发
};

onMounted(() => {
    loadFilter();
});

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.switch-default-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
    min-height: 360px;

    .filter-bar {
        display: flex;
        align-items: center;
    }

    .table-area {
        flex: 1;
    }
}

.title-cell {
    display: flex;
    align-items: center;
    gap: 8px;
}

.default-page-icon {
    display: inline-flex;
    align-items: center;
    color: var(--el-color-primary);
    :deep(.icon) {
        width: 18px;
        height: 18px;
    }
}

.legacy-tag {
    background-color: #fafafa;
    border-color: transparent;
    color: #8c8c8c;
    font-size: 11px;
    padding: 0 6px;
    height: 18px;
    line-height: 18px;
    border-radius: 2px;
    font-weight: 500;
    transition: none;
    animation: none;
}

.btn-link {
    color: var(--el-color-primary);
    cursor: pointer;
    text-decoration: none;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.8;
    }

    &.is-disabled {
        color: #c0c4cc;
        cursor: not-allowed;
        pointer-events: none;
    }
}

.pagination-con {
    background: #fff;
}

.text-status {
    &.success {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #52c41a;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
    &.default {
        &:before {
            content: "";
            display: inline-block;
            width: 6px;
            height: 6px;
            background-color: #929292;
            border-radius: 50%;
            margin-right: 4px;
            transform: translateY(-2px);
        }
    }
}
</style>
