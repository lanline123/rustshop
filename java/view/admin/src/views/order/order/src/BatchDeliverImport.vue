<template>
    <div class="importDeliverDialog">
        <div class="tip-bar"> {{ $t('上传前请先按Excel模板中的格式编辑内容') }} <a class="btn-link" @click="downloadTpl">{{ $t('下载Excel模版') }}</a>
        </div>

        <div class="import-file-layout">
            <el-upload
                class="upload-demo"
                drag
                accept=".xlsx, .xls"
                action=""
                :show-file-list="false"
                :http-request="httpRequest"
                v-model:file-list="fileList"
                :before-upload="beforeUpload"
            >
                <div class="el-upload__text" v-if="fileList.length === 0">
                    <img src="@/assets/icons/product/fileUpload.png" alt="" width="56" height="66" />
                    <div>{{ $t('将文件拖拽至此区域，或') }}<a class="btn-link">{{ $t('点击添加') }}</a></div>
                    <div class="tip">{{ $t('支持 .xls, .xlsx 格式，限10M以内') }}</div>
                </div>

                <div class="errorMessage" v-else-if="errorMessage">
                    <img src="@/assets/icons/product/fileError.png" alt="" width="56" height="66" />
                    <div class="error-text">{{ errorMessage }}</div>
                    <div>
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t('继续导入') }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t('完成') }}</el-button>
                    </div>
                </div>

                <div class="errorMessage" v-else-if="isImported && hasFail">
                    <img src="@/assets/icons/product/fileError.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div class="error-tig"> {{ $t('您可以下载失败数据，修改后再重新导入') }} <span class="btn-link" @click.stop="downloadError">{{ $t('下载失败数据') }}</span>
                    </div>
                    <div class="actions" style="margin-top: 4px">
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t('继续导入') }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t('完成') }}</el-button>
                    </div>
                </div>

                <div class="fileListBox" v-else-if="isImported && !hasFail">
                    <img :src="successImg" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div class="actions">
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t('继续导入') }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t('完成') }}</el-button>
                    </div>
                </div>

                <div class="fileListBox" v-else>
                    <img src="@/assets/icons/product/fileUpload.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div>
                        <a class="btn-link" style="margin-right: 10px" @click.stop="delFile">{{ $t('删除') }}</a>
                    </div>
                    <el-button type="primary" style="width: 120px" :loading="loading" @click.stop="importNow">{{ $t('立即导入') }}</el-button>
                </div>
            </el-upload>
        </div>

        <div class="footer">
            <div class="title">{{ $t('导入规则') }}</div>
            <div>{{ $t('1. 请先下载模板，在模板中按字段填写信息，然后上传该文件。') }}</div>
            <div>{{ $t('2. 文件大小不超过10MB。') }}</div>
            <div>{{ $t('3. 建议先使用少量数据测试，确认无误后再批量导入。') }}</div>
            <div>{{ $t('4. Excel不支持拆单发货，如需拆单，请手动进行发货。') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { message } from "ant-design-vue";
import { batchDeliverImport, downloadDeliverTemplate, getBatchDeliverImportErrorData } from "@/api/order/order";
import requestExport from "@/utils/export";
import successImg from "@/assets/icons/product/fileSucceeded.png";
import errorImg from "@/assets/icons/product/fileError.png";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
type FailDetail = {
    rowNum: number;
    orderSn: string;
    reason: string;
};

type ImportResult = {
    totalCount: number;
    successCount: number;
    failCount: number;
    failDetails: FailDetail[];
};

const props = defineProps<{
    ids?: number[];
}>();

const emit = defineEmits(["close", "submitCallback"]);

const fileList = ref<any[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const isImported = ref(false);
const importResult = ref<ImportResult | null>(null);

const hasFail = computed(() => (importResult.value?.failCount ?? 0) > 0);
const failMessage = computed(() => {
    const failCount = importResult.value?.failCount ?? 0;
    const totalCount = importResult.value?.totalCount ?? 0;
    const successCount = importResult.value?.successCount ?? 0;
    return t('导入存在失败数据：总 {totalCount}，成功 {successCount}，失败 {failCount}', { totalCount, successCount, failCount });
});

const httpRequest = (file: any) => {
    fileList.value = [file.file];
};

const beforeUpload = (file: any) => {
    const isLt = file.size / 1024 / 1024 < 10;
    if (!isLt) {
        message.error(t('只能上传10M以内的文件'));
        return false;
    }
    return true;
};

// 删除文件
const delFile = () => {
    fileList.value = [];
    isImported.value = false;
    importResult.value = null;
    errorMessage.value = "";
    message.success(t('删除成功'));
};

// 下载模板
const downloadTpl = async () => {
    const result = await downloadDeliverTemplate(props.ids);
    requestExport(result, t('批量发货模板'));
};

// 导入数据
const importNow = () => {
    if (!fileList.value?.length) {
        message.error(t('请先选择文件'));
        return;
    }
    const formData = new FormData();
    formData.append("file", fileList.value[0]);
    loading.value = true;
    errorMessage.value = "";
    isImported.value = false;
    importResult.value = null;

    batchDeliverImport(formData)
        .then((data: any) => {
            isImported.value = true;
            importResult.value = data as ImportResult;
            message.success(t('导入完成'));
            loading.value = false;
        })
        .catch((err: any) => {
            loading.value = false;
            isImported.value = false;
            importResult.value = null;
            errorMessage.value = err.message || t('导入失败');
        });
};

// 下载失败数据
const downloadError = async () => {
    if (!fileList.value?.length) {
        message.error(t('请先选择文件'));
        return;
    }
    const formData = new FormData();
    formData.append("file", fileList.value[0]);

    try {
        const res = await getBatchDeliverImportErrorData(formData);
        requestExport(res, t('批量发货失败数据'));
    } catch (err: any) {
        message.error(err?.message || t('下载失败'));
    }
};

// 继续导入
const clearFile = () => {
    fileList.value = [];
    errorMessage.value = "";
    isImported.value = false;
    importResult.value = null;
};

// 完成
const handleComplete = () => {
    emit("submitCallback");
};
</script>

<style scoped lang="less">
.importDeliverDialog {
    .tip-bar {
        width: 100%;
        cursor: pointer;
        display: inline-block;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        background-color: #f5f7fa;
        border-radius: 2px;
        margin: 0 0 20px;
        padding: 11px 0 11px 12px;
        box-sizing: border-box;
    }
    .import-file-layout {
        border: 1px dashed #a1cdff;
        flex-direction: column;
        display: flex;
    }
    .el-upload__text {
        color: #1f2026;
        display: flex;
        flex-direction: column;
        gap: 10px;
        align-items: center;
        font-size: 16px;
        font-weight: 400;
        line-height: 24px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        .btn-link {
            margin-left: 10px;
        }
    }
    .tip {
        color: #9598a6;
        font-size: 14px;
        font-weight: 400;
        line-height: 22px;
        margin-top: 8px;
    }
    .fileListBox {
        display: flex;
        flex-direction: column;
        font-size: 16px;
        font-weight: 400;
        line-height: 24px;
        gap: 8px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        align-items: center;
        width: 100%;
    }
    .btn-link {
        font-size: 14px;
        color: #268bff;
    }
    .name {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100%;
        text-align: center;
    }
    .resultSummary {
        color: #1e2226;
        font-size: 14px;
    }
    .failTable {
        width: 100%;
        white-space: normal;
        padding: 0 10px;
        box-sizing: border-box;
    }
    .actions {
        display: flex;
        gap: 12px;
    }
    .errorMessage {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
        .error-text {
            word-break: break-all;
            line-height: 20px;
        }
        .error-tig {
            color: #626573;
            font-size: 14px;
            font-weight: 400;
        }
    }
}

::deep(.el-upload-dragger) {
    padding: 80px 20px;
    box-sizing: border-box;
    border: none;
}

.footer {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    color: #1e2226;
    font-size: 14px;
    font-weight: 400;
    padding: 20px 0;
    .title {
        color: #1f2026;
        font-size: 16px;
        font-weight: 600;
    }
}
</style>
