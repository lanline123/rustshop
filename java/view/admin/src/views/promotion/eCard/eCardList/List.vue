<template>
    <div class="container">
        <div class="notice-warp">
            <p>{{ $t('请先下载') }}<a class="btn-link" @click="exportECard()">{{ $t('批量导入模板文件.xlsx') }}</a>{{ $t('， 然后点击批量导入电子卡券按钮， 导入电子卡券信息。') }}</p>
        </div>
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm
                                        :params="{ act: 'add', groupId: groupId }"
                                        isDrawer
                                        :delayed="20000"
                                        path="promotion/eCard/eCardList/Info"
                                        :title="$t('添加电子卡券')"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加电子卡券') }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-upload
                                        ref="upload"
                                        :auto-upload="false"
                                        :limit="1"
                                        :on-change="handleChange"
                                        :on-exceed="handleExceed"
                                        :show-file-list="false"
                                        accept=".xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                    >
                                        <template #trigger>
                                            <el-button type="success">{{ $t('批量导入电子卡券') }}</el-button>
                                        </template>
                                    </el-upload>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="wechat_live_title"
                                            :placeholder="$t('输入电子卡券名称')"
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
                        <el-table :data="filterState" row-key="cardId" :total="total">
                            <el-table-column :label="$t('卡券ID')" prop="cardId" :width="80"></el-table-column>
                            <el-table-column :label="$t('卡券名称')" prop="cardNumber" :width="200"></el-table-column>
                            <el-table-column :label="$t('卡券密码')" prop="cardPwd" :width="150"></el-table-column>
                            <el-table-column :label="$t('是否使用')" prop="isUse">
                                <template #default="{ row }">
                                    <Switch v-model:checked="row.isUse" :params="{ id: row.cardId, field: 'isUse' }" :requestApi="updateECardFiled" />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" :width="150"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="120">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑电子卡券')"
                                        width="600px"
                                        path="promotion/eCard/eCardList/Info"
                                        :params="{ act: 'detail', id: row.cardId }"
                                    >
                                        <a class="btn-link">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord :params="{ id: row.cardId }" :requestApi="delECard" @afterDelete="loadFilter()">{{ $t('删除') }} </DeleteRecord>
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
import { ECardFilterState, ECardFilterParams, ECardFormState } from "@/types/promotion/eCard.d";
import { getECardList, delECard, updateECardFiled, importECardGroup, ECardGroupExport } from "@/api/promotion/eCard";
import { useConfigStore } from "@/store/config";
import requestExport from "@/utils/export";
import { genFileId } from "element-plus";
import type { UploadInstance, UploadProps, UploadRawFile } from "element-plus";
import { useListRequest } from '@/hooks/useListRequest';
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    groupId: {
        type: Number,
        default: 0
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
  onBatchAction,
  resetParams
} = useListRequest<ECardFilterState, ECardFilterParams>({
  apiFunction: getECardList,
  idKey: 'cardId',
  defaultParams: {
      sortField: '',
      sortOrder: '',
      keyword: '',
      page: 1,
      size: config.get("pageSize"),
      groupId: props.groupId
  }
});

// 初始化加载
loadFilter();
const formState = reactive<ECardFormState>({
    //初使化用于查询的参数
    groupId: props.groupId,
    file: ""
});
const upload = ref<UploadInstance>();
const handleExceed: UploadProps["onExceed"] = (files) => {
    upload.value!.clearFiles();
    const file = files[0] as UploadRawFile;
    file.uid = genFileId();
    upload.value!.handleStart(file);
};
const handleChange = (data: any) => {
    let formData = new FormData();
    formData.append("file", data.raw);
    formState.file = formData.get("file");
    _importECardGroup();
};
const _importECardGroup = async () => {
    try {
        const result = await importECardGroup({ ...formState });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        upload.value!.clearFiles();
    }
};
// 导出模板
const exportECard = async () => {
    try {
        const result = await ECardGroupExport({ isDownload: 1 });
        requestExport(result, t('批量导入模板文件'));
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>

<style lang="less" scoped>
.container {
    :deep(.el-upload-list) {
        margin: 0;
    }
}
</style>
