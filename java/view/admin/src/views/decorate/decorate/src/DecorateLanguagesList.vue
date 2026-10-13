<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-table
                    :data="filterState"
                    :total="total"
                    @sort-change="onSortChange"
                    v-loading="loading"
                >
                    <el-table-column :label="$t('模板名称')" prop="decorateId" :width="200">
                        <template #default="{ row }">
                            {{ row.decorateTitle }} <span v-if="row.locale" class="red">-{{ row.locale?.language }}</span>
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('模板语言')" prop="locale.language" :width="120">
                        <template #default="{ row }">
                           {{ row.locale?.language || $t('默认语言') }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('更新时间')" prop="updateTime" sortable="custom">
                        <template #default="{ row }">
                            {{ row.updateTime }}
                        </template>
                    </el-table-column>
                    <el-table-column :label="$t('操作')" fixed="right" :width="100">
                        <template #default="{ row }">
                            <el-space>
                                <a class="btn-link" @click="copy(row)">{{ $t('复制') }}</a>
                            </el-space>
                        </template>
                    </el-table-column>
                    <template #empty>
                        <div class="empty-warp">
                            <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                        </div>
                    </template>
                </el-table>
                <div class="pagination-con" v-if="total > 0">
                    <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { Pagination } from "@/components/list";
import { FilterState, FilterParams } from "@/types/decorate/mobileDecorate.d";
import { getDecorateList } from "@/api/decorate/mobileDecorate";
import { message, Modal } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0 // 如果在您的应用中 0 有特殊意义，请选择一个合适的默认值
    },
    decorateType: {
        type: Number,
        default: 0 // 如果在您的应用中 0 有特殊意义，请选择一个合适的默认值
    }
});
const config: any = useConfigStore();
const {
    listData: filterState,
    loading,
    total,
    selectedIds,
    filterParams,
    loadData: loadFilter,
    onSearchSubmit,
    onSortChange,
    onSelectChange,
    onBatchAction
} = useListRequest<FilterState, FilterParams>({
    apiFunction: getDecorateList,
    idKey: "decorateId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        sortField: "",
        sortOrder: "",
        keyword: "",
        parentId: -1,
        decorateType: props.decorateType
    }
});

// 初始化加载
loadFilter();
const copy = (row: FilterState) => {
    Modal.confirm({
        title: t('提示'),
        content: t('复制该模板将清空当前模板装修所有设置，是否继续操作？'),
        onOk: async () => {
            emit("submitCallback", row.decorateId);
        }
    });
};
</script>
