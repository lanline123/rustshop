<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <el-form :model="filterParams">
                        <div class="list-table-tool-row">
                            <div class="list-table-tool-col">
                                <el-space>
                                    <TigInput v-model="filterParams.keyword" class="width240" name="keyword" :placeholder="$t('输入关键词或IP地址')">
                                        <template #append>
                                            <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                        </template>
                                    </TigInput>
                                </el-space>
                            </div>
                        </div>
                    </el-form>
                </div>
                <div class="table-container">

                    <a-spin :spinning="loading">
                        <el-table :data="filterState" :loading="loading" :total="total" row-key="logId"
                                  @sort-change="onSortChange">
                            <!-- <el-table-column label="编号" prop="logId"></el-table-column> -->
                            <el-table-column :label="$t('操作者')" prop="username"></el-table-column>
                            <el-table-column :label="$t('IP地址')" prop="ipAddress"></el-table-column>
                            <el-table-column :label="$t('操作日期')" prop="logTime" sortable="custom"></el-table-column>
                            <el-table-column :label="$t('操作记录')" prop="logInfo"></el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div v-if="total > 0" class="pagination-con">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total"
                                    @callback="loadFilter"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import {onMounted, reactive, ref} from 'vue';
import {Pagination} from '@/components/list';
import {message} from 'ant-design-vue'
import {useConfigStore} from "@/store/config";
import type {AdminLogFilterParams, AdminLogFilterState} from '@/types/authority/adminLog';
import {getAdminLogList} from "@/api/authority/adminLog";
import {useRouter} from "vue-router";

const config:any = useConfigStore();
// 基本参数定义
const filterState = ref(<AdminLogFilterState[]>[]);
const loading = ref<boolean>(true);
const total = ref<number>(0);
const selectedIds = ref<number[]>([]);
const filterParams = reactive<AdminLogFilterParams>({   //初始化用于查询的参数
    page: 1,
    size: config.get('pageSize'),
    sortField: 'logTime',
    sortOrder: 'desc',
    keyword: '',
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAdminLogList({...filterParams});
        filterState.value = result.records;
        total.value = result.total;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }

}
const route = useRouter()
onMounted(() => {
    if (route.currentRoute.value.query.userId) {
        filterParams.userId = Number(route.currentRoute.value.query.userId)
    }
    loadFilter();
});

// 参数查询
const onSearchSubmit = () => {
    loadFilter()
};
// 修改排序
const onSortChange = ({prop, order}: { prop: string; order?: string }) => {
    filterParams.sortField = prop;
    filterParams.sortOrder = order == 'ascending' ? 'asc' : order == 'descending' ? 'desc' : '';
    loadFilter();
};
</script>
