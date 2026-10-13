<template>
    <div class="container">
        <!-- <div class="notice-warp">
            <p>
                请先下载<a class="btn-link" @click="exportTemplate()">批量导入模板文件.xlsx</a>， 然后点击批量导入{{ cardLabel }}按钮， 导入{{
                    cardLabel
                }}信息。
            </p>
        </div> -->
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="list-table-tool-row">
                        <div class="simple-form-warp">
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <DialogForm
                                        :params="{ act: 'add', cardGroupsId: cardGroupsId, groupType: groupType, cardType: cardType }"
                                        isDrawer
                                        :delayed="20000"
                                        path="promotion/giftCard/giftCardList/Info"
                                        :title="$t('添加') + cardLabel"
                                        width="600px"
                                        @okCallback="loadFilter"
                                    >
                                        <el-button type="primary">{{ $t('添加{cardLabel}', { cardLabel }) }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <!-- <el-upload
                                        ref="upload"
                                        :auto-upload="false"
                                        :limit="1"
                                        :on-change="handleChange"
                                        :on-exceed="handleExceed"
                                        :show-file-list="false"
                                        accept=".xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                    >
                                        <template #trigger>
                                            <el-button>批量导入{{ cardLabel }}</el-button>
                                        </template>
                                    </el-upload> -->
                                    <DialogForm
                                        :maskClose="false"
                                        :params="{
                                            importTitle: $t('导入{cardLabel}', { cardLabel }),
                                            cardGroupsId: cardGroupsId
                                        }"
                                        isDrawer
                                        path="promotion/giftCard/src/ImportGiftCard"
                                        :title="$t('导入{cardLabel}', { cardLabel })"
                                        width="600px"
                                        @okCallback="loadFilter"
                                        :showClose="false"
                                        :showOnOk="false"
                                    >
                                        <el-button>{{ $t('批量导入{cardLabel}', { cardLabel }) }}</el-button>
                                    </DialogForm>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <el-button :loading="batchGenerateLoading" @click="batchItemGenerate">{{ $t('批量生成') }}</el-button>
                                </div>
                            </div>
                            <div class="simple-form-field">
                                <div class="form-group">
                                    <div class="control-container">
                                        <TigInput
                                            v-model="filterParams.keyword"
                                            name="wechat_live_title"
                                            :placeholder="$t('输入卡号查找')"
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
                            <div class="simple-form-field" style="margin-left: auto">
                                <div class="form-group">
                                    <el-button :loading="exportLoading" @click="exportGiftCards()">{{ $t('批量导出') }}</el-button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-container">
                    <a-spin :spinning="loading">
                        <el-table :data="filterState" row-key="giftCardsId" :total="total">
                            <el-table-column label="ID" prop="giftCardsId" :width="80"></el-table-column>
                            <el-table-column :label="$t('卡号')" prop="cardNumber" :width="200"></el-table-column>
                            <el-table-column :label="$t('密码')" prop="cardPassword" :width="150"></el-table-column>
                            <el-table-column :label="cardLabel + $t('状态')" prop="isAvailable" align="center" width="100">
                                <template #default="{ row }">
                                    <Switch
                                        v-model:checked="row.isAvailable"
                                        :params="{ giftCardsId: row.giftCardsId }"
                                        field="isAvailable"
                                        :requestApi="updateGiftCardStatus"
                                        :switchingCode="[0, 1]"
                                        @callback="loadFilter"
                                    />
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否售出')" prop="isSold" align="center">
                                <template #default="{ row }">
                                    <span v-if="cardType === 2">{{ row.isExchange === $t('是') ? $t('是') : "-" }}</span>
                                    <span v-else>{{ row.isSold === $t('是') ? $t('是') : $t('否') }}</span>
                                </template>
                            </el-table-column>
                            <el-table-column :label="$t('是否兑换')" prop="isExchange" align="center"></el-table-column>
                            <el-table-column :label="$t('创建时间')" prop="addTime" :min-width="150"></el-table-column>
                            <el-table-column :label="$t('操作')" fixed="right" :width="180">
                                <template #default="{ row }">
                                    <DialogForm
                                        isDrawer
                                        :title="$t('礼品卡信息')"
                                        width="880px"
                                        path="promotion/giftCard/giftCardList/Detail"
                                        :show-on-ok="false"
                                        :params="{ id: row.giftCardsId, groupType: groupType }"
                                    >
                                        <a class="btn-link">{{ $t('查看') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DialogForm
                                        isDrawer
                                        @okCallback="loadFilter"
                                        :title="$t('编辑') + cardLabel"
                                        :disabled="row.isSold === '是' || row.isExchange === '是'"
                                        width="600px"
                                        path="promotion/giftCard/giftCardList/Info"
                                        :params="{ act: 'detail', id: row.giftCardsId, groupType: groupType, cardType: cardType }"
                                    >
                                        <a class="btn-link" :class="{ disabled: row.isSold === '是' || row.isExchange === '是' }">{{ $t('编辑') }}</a>
                                    </DialogForm>
                                    <el-divider direction="vertical" />
                                    <DeleteRecord
                                        :params="{ cardGroupsId: row.giftCardsId, count: 1 }"
                                        :requestApi="delGiftCard"
                                        :disabled="row.isSold === '是' || row.isExchange === '是'"
                                        @afterDelete="loadFilter()"
                                        >{{ $t('删除') }} </DeleteRecord>
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
import { ref, reactive, onMounted, computed } from "vue";
import { Pagination, DeleteRecord } from "@/components/list";
import Switch from "../src/Switch.vue";
import { ElMessageBox } from "element-plus";
import { message } from "ant-design-vue";
import { useConfigStore } from "@/store/config";
import requestExport from "@/utils/export";
import { genFileId } from "element-plus";
import type { UploadInstance, UploadProps, UploadRawFile } from "element-plus";
import { useListRequest } from "@/hooks/useListRequest";
import {
    giftCardsExport,
    importGiftCard,
    giftCardBatchGenerate,
    getGiftCardList,
    delGiftCard,
    giftCardExportTemplate,
    updateGiftCardStatus
} from "@/api/promotion/giftCard";
import type { GiftCardFilterParams, GiftCardItem, GiftCardImportFormState } from "@/types/promotion/giftCard";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    cardGroupsId: {
        type: Number,
        default: 0
    },
    giftGroupsName: {
        type: String,
        default: ""
    },
    groupType: {
        type: Number,
        default: 1
    },
    cardType: {
        type: Number,
        default: 1
    }
});
const emit = defineEmits(["submitCallback"]);
const config: any = useConfigStore();
const cardTypes = ref({
    1: t('储值卡'),
    2: t('兑换卡')
} as const);
const cardLabel = computed(() => {
    return cardTypes.value[props.groupType as keyof typeof cardTypes.value];
});
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
} = useListRequest<GiftCardItem, GiftCardFilterParams>({
    apiFunction: getGiftCardList,
    idKey: "giftCardsId",
    defaultParams: {
        sortField: "",
        sortOrder: "",
        keyword: "",
        page: 1,
        size: config.get("pageSize"),
        cardGroupsId: props.cardGroupsId
    }
});

// 初始化加载
loadFilter();
const formState = reactive<GiftCardImportFormState>({
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
    _importGiftCard();
};
const _importGiftCard = async () => {
    try {
        const result = await importGiftCard({ cardGroupsId: props.cardGroupsId }, { ...formState });
        message.success(t('操作成功'));
        loadFilter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        upload.value!.clearFiles();
    }
};
// 下载模版
const exportTemplate = async () => {
    try {
        const result = await giftCardExportTemplate();
        requestExport(result, t('批量导入模板文件'));
    } catch (error: any) {
        message.error(error.message);
    }
};
// 导出
const exportGiftCards = async () => {
    try {
        exportLoading.value = true;
        message.loading("正在导出数据...", 0);
        const result = await giftCardsExport({ cardGroupsId: props.cardGroupsId });
        requestExport(result, t('批量导出') + cardLabel.value);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(message.destroy, 300);
        exportLoading.value = false;
    }
};
const batchGenerateLoading = ref(false);
const exportLoading = ref(false);

const batchItemGenerate = async () => {
    try {
        const inputValue = await ElMessageBox.prompt(`请输入要生成的${cardLabel.value}数量`, t('批量生成'), {
            confirmButtonText: t('确定'),
            cancelButtonText: t('取消'),
            inputPattern: /^[1-9]\d*$/,
            inputErrorMessage: t('请输入有效的数量'),
            inputValue: "10"
        });

        const count = parseInt(inputValue.value);
        if (isNaN(count) || count <= 0) {
            message.error(t('请输入有效的数量'));
            return;
        }

        batchGenerateLoading.value = true;
        try {
            await giftCardBatchGenerate({
                cardGroupsId: props.cardGroupsId,
                count: count
            });
            message.success(t('操作成功'));
            loadFilter();
        } catch (error: any) {
            message.error(error.message);
        } finally {
            batchGenerateLoading.value = false;
        }
    } catch {
        // 用户取消操作，不执行任何操作
    }
};
// 表单提交
const onFormSubmit = () => {
    emit("submitCallback");
};
defineExpose({
    onFormSubmit
});
</script>

<style lang="less" scoped>
.container {
    :deep(.el-upload-list) {
        margin: 0;
    }
}
.disabled {
    cursor: not-allowed;
    color: rgb(199.5, 201, 204);
}
</style>
