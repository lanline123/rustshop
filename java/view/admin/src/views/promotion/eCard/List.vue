<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm
                                        :params="{ act: 'add' }"
                                        isDrawer
                                        :delayed="20000"
                                        path="promotion/eCard/Info"
                                        :title="$t('添加电子卡券组')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加电子卡券组') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="wechat_live_title"
                                            :placeholder="$t('输入电子卡券组名称')"
                                            @keyup.enter="onSearchSubmit"
                                            clearable
                                            @clear="onSearchSubmit"
                                        >
                                            <template #append>
                                                <el-button @click="onSearchSubmit"><span class="iconfont icon-chakan1"></span></el-button>
                                            </template>
                                        </TigInput>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="groupId" :total="total">
                            <el-table-column :label="$t('分组ID')" prop="groupId" :width="80"></el-table-column>
                            <el-table-column :label="$t('分组名称')" prop="groupName"></el-table-column>
                            <el-table-column :label="$t('是否使用')" prop="isUse">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isUse" :params="{ id: row.groupId, field: 'isUse' }" :requestApi="updateECardGroupFiled" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('备注')" prop="remark" :width="300">
                                <template #default="{ row }">
                                    {{ row.remark || "--" }}
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime"></el-table-column>
                            <el-table-column :label="$t('修改时间')" prop="upTime"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="200">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :showOnOk="false"
                                        :showClose="false"
                                        :title="$t('查看卡列表')"
                                        width="800px"
                                        path="promotion/eCard/eCardList/List"
                                        :params="{ groupId: row.groupId }"
                                    >
                                        <a class="btn-link">{{ $t('查看卡列表') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑电子卡券组')"
                                        width="600px"
                                        path="promotion/eCard/Info"
                                        :params="{ act: 'detail', id: row.groupId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.groupId }" :requestApi="delECardGroup" @afterDelete="loadFilter()">{{ $t('删除') }} </DeleteRecord>
                                </template>
                            </el-table-column>
                            <template #empty>
                                <div class="empty-warp">
                                    <div v-if="!loading" class="empty-bg">{{ $t('暂无数据') }}</div>
                                </div>
                            </template>
                        </el-table>
                    </a-spin>
                    <div class="pagination-con" v-if="total > 0">
                        <Pagination v-model:page="filterParams.page" v-model:size="filterParams.size" :total="total" @callback="loadFilter" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import "@/style/css/list.less";
import { DialogForm } from "@/components/dialog";
import { ref, reactive, onMounted } from "vue";
import { Switch, Pagination, DeleteRecord } from "@/components/list";
import { message } from "ant-design-vue";
import { ECardGroupFilterState, ECardGroupFilterParams } from "@/types/promotion/eCard.d";
import { getECardGroupList, delECardGroup, updateECardGroupFiled } from "@/api/promotion/eCard";
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
import { useListRequest } from '@/hooks/useListRequest';
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
  onBatchAction,
  resetParams
} = useListRequest<ECardGroupFilterState, ECardGroupFilterParams>({
  apiFunction: getECardGroupList,
  idKey: 'groupId',
  defaultParams: {
      sortField: '',
      sortOrder: '',
      keyword: '',
      page: 1,
      size: config.get("pageSize"),
  }
});

// 初始化加载
loadFilter();
</script>
