/**
 * @file 库存预警导入弹窗
 * @description 通过 Excel 模板批量导入商品/SKU 的库存预警规则，交互对齐导入调拨单
 * @features
 * - 下载导入模板（规格编码 / 上限 / 下限 三列）
 * - 拖拽或点击上传，限 10M
 * - 单行失败不影响其他行，失败行可下载失败数据，改完重新导入
 */
<template>
    <div class="import-warning">
        <div class="tip-bar">{{ $t('上传前请先按Excel模板中的格式编辑内容') }} <a class="btn-link" @click="downloadTpl">{{ $t('下载Excel模版') }}</a></div>
        <div class="import-file-layout">
            <el-upload
                v-model:file-list="fileList"
                class="upload-demo"
                drag
                accept=".xlsx, .xls"
                action=""
                :show-file-list="false"
                :http-request="httpRequest"
                :before-upload="beforeUpload"
            >
                <div v-if="fileList.length === 0" class="el-upload__text">
                    <img src="@/assets/icons/product/fileUpload.png" alt="" width="56" height="66" />
                    <div>{{ $t('将文件拖拽至此区域，或') }}<a class="btn-link">{{ $t('点击添加') }}</a></div>
                    <div class="tip">{{ $t('支持 .xls, .xlsx 格式，限10M以内') }}</div>
                </div>
                <div v-else-if="isSuccess && !errorMessage" class="fileListBox">
                    <img src="@/assets/icons/product/fileSucceeded.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div>{{ successText }}</div>
                    <div>
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t('继续导入') }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t('完成') }}</el-button>
                    </div>
                </div>
                <div v-else-if="errorMessage" class="errorMessage">
                    <img src="@/assets/icons/product/fileError.png" alt="" width="56" height="66" />
                    <div class="error-text">{{ errorMessage }}</div>
                    <div v-if="hasFailData" class="error-tig">{{ $t('您可以下载失败数据，修改后再重新导入') }} <span class="btn-link" @click.stop="downloadError">{{ $t('下载失败数据') }}</span></div>
                    <div>
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t('继续导入') }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t('完成') }}</el-button>
                    </div>
                </div>
                <div v-else class="fileListBox">
                    <img src="@/assets/icons/product/fileUpload.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div>
                        <a class="btn-link" style="margin-right: 10px">{{ $t('重新上传') }}</a>
                        <span class="btn-link" @click.stop="delFile">{{ $t('删除') }}</span>
                    </div>
                    <el-button type="primary" style="width: 120px" :loading="loading" @click.stop="importNow">{{ $t('立即导入') }}</el-button>
                </div>
            </el-upload>
        </div>
        <div class="footer">
            <div class="title">{{ $t('导入规则') }}</div>
            <div>{{ $t('1. 请先下载模板，在模板中按字段填写信息，然后上传该文件。') }}</div>
            <div>{{ $t('2. 导入未完成之前，请勿关闭页面，否则可能数据错误。') }}</div>
            <div>{{ $t('3. 文件大小不超过10MB。') }}</div>
            <div>{{ $t('4. 限制导入10000行记录，超出部分请分多次导入。') }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n";
import requestExport from "@/utils/export";
import { getStockWarningImportErrorData, getStockWarningImportTemplate, importStockWarning } from "@/api/product/stockWarning";

const { t } = useI18n();
const emit = defineEmits(["close", "callback", "submitCallback"]);

const fileList = ref<any[]>([]);
const loading = ref(false);
const errorMessage = ref("");
const isSuccess = ref(false);
const successCount = ref(0);
// 整体拒绝（未上传文件 / 非 xlsx / 表格无数据）时没有失败明细可下载
const hasFailData = ref(false);

const successText = computed(() => t("全部导入成功，共{count}条", { count: successCount.value }));

const httpRequest = (file: any) => {
    fileList.value = [file.file];
};

const beforeUpload = (file: any) => {
    const isLt = file.size / 1024 / 1024 < 10;
    if (!isLt) {
        message.error(t("只能上传10M以内的文件"));
        return false;
    }
    return true;
};

const buildFormData = () => {
    const formData = new FormData();
    formData.append("file", fileList.value[0]);
    return formData;
};

const delFile = () => {
    clearFile();
    message.success(t("删除成功"));
};

const downloadTpl = async () => {
    try {
        const result = await getStockWarningImportTemplate();
        requestExport(result, t("库存预警导入模板"));
    } catch (error: any) {
        message.error(error.message || t("操作失败"));
    }
};

const importNow = () => {
    loading.value = true;
    errorMessage.value = "";
    hasFailData.value = false;
    // 接口一旦发出数据就可能已变化，通知外层关闭时刷新，不等结果
    emit("callback", { imported: true });
    importStockWarning(buildFormData())
        .then((data: any) => {
            const failCount = data?.failCount || 0;
            // 部分失败时后端 code 仍是 0，合法行已入库，用失败态承载提示与失败数据下载
            if (failCount > 0) {
                isSuccess.value = false;
                hasFailData.value = true;
                errorMessage.value = t("导入完成，成功{successCount}条，失败{failCount}条", { successCount: data?.successCount || 0, failCount });
                return;
            }
            successCount.value = data?.successCount || 0;
            isSuccess.value = true;
            message.success(t("导入成功"));
        })
        .catch((err: any) => {
            isSuccess.value = false;
            errorMessage.value = err.message;
        })
        .finally(() => {
            loading.value = false;
        });
};

// 回传原文件换取带失败原因的 Excel，与导入用同一个文件
const downloadError = () => {
    getStockWarningImportErrorData(buildFormData())
        .then((res: any) => {
            requestExport(res, t("失败数据"));
        })
        .catch((err: any) => {
            message.error(err.message);
        });
};

const clearFile = () => {
    fileList.value = [];
    errorMessage.value = "";
    isSuccess.value = false;
    successCount.value = 0;
    hasFailData.value = false;
};

const handleComplete = () => {
    emit("submitCallback");
};
</script>

<style scoped lang="less">
.import-warning {
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
        gap: 5px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        align-items: center;
    }
    .btn-link {
        font-size: 14px;
    }
    .errorMessage {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 10px;
        .error-tig {
            color: #626573;
            font-size: 14px;
            font-weight: 400;
        }
    }
    .name {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        width: 100%;
    }
}
:deep(.el-upload-dragger) {
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
.error-text {
    word-break: break-all;
    line-height: 20px;
}
</style>
