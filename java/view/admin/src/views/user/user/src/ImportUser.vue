/** 客户导入组件 - 通过Excel文件批量导入客户数据 */
<template>
    <div class="importUserDialog">
        <div class="tip-bar">
            {{ $t("上传前请先按Excel模板中的格式编辑内容") }} <a class="btn-link" @click="downloadTpl">{{ $t("下载Excel模版") }}</a>
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
                    <div>
                        {{ $t("将文件拖拽至此区域，或") }}<a class="btn-link">{{ $t("点击添加") }}</a>
                    </div>
                    <div class="tip">{{ $t("支持 .xls, .xlsx 格式，限10M以内") }}</div>
                </div>
                <div class="fileListBox" v-else-if="isSuccess && !errorMessage">
                    <img src="@/assets/icons/product/fileSucceeded.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div>{{ $t("成功导入") }}</div>
                    <div>
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t("继续导入") }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t("完成") }}</el-button>
                    </div>
                </div>
                <div class="fileListBox" v-else-if="fileList.length && !errorMessage">
                    <img src="@/assets/icons/product/fileUpload.png" alt="" width="56" height="66" />
                    <div class="name">{{ fileList[0].name }}</div>
                    <div>
                        <a class="btn-link" style="margin-right: 10px">{{ $t("重新上传") }}</a>
                        <span class="btn-link" @click.stop="delFile">{{ $t("删除") }}</span>
                    </div>
                    <el-button type="primary" style="width: 120px" @click.stop="importNow">{{ $t("立即导入") }}</el-button>
                </div>
                <div class="errorMessage" v-else-if="errorMessage">
                    <img src="@/assets/icons/product/fileError.png" alt="" width="56" height="66" />
                    <div class="error-text">{{ errorMessage }}</div>
                    <div class="error-tig">
                        {{ $t("您可以下载失败数据，修改后再重新导入") }} <span class="btn-link" @click.stop="downloadError">{{ $t("下载失败数据") }}</span>
                    </div>
                    <div>
                        <el-button style="width: 120px" @click.stop="clearFile">{{ $t("继续导入") }}</el-button>
                        <el-button type="primary" style="width: 120px" @click.stop="handleComplete">{{ $t("完成") }}</el-button>
                    </div>
                </div>
            </el-upload>
        </div>
        <div class="footer">
            <div class="title">{{ $t("导入规则") }}</div>
            <div>{{ $t("1. 请先下载模板，在模板中按字段填写信息，然后上传该文件。") }}</div>
            <div>{{ $t("2. 导入未完成之前，请勿关闭页面，否则可能数据错误。") }}</div>
            <div>{{ $t("3. 文件大小不超过10MB。") }}</div>
            <div>{{ $t("4. 限制导入10000行记录，超出部分请分多次导入。") }}</div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, defineEmits } from "vue";
import { message } from "ant-design-vue";
import { userImportTemplate, userImportSubmit, getUserImportErrorData } from "@/api/user/user";
import requestExport from "@/utils/export";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["close", "submitCallback"]);
/** 上传文件列表 */
const fileList = ref<any[]>([]);
const loading = ref(false);
/** 错误信息 */
const errorMessage = ref("");
/** 是否导入成功 */
const isSuccess = ref(false);

/** 自定义上传处理 */
const httpRequest = (file: any) => {
    fileList.value = [file.file];
};

/** 上传前校验 - 限制10MB */
const beforeUpload = (file: any) => {
    // 限制不超过10mb的文件
    const isLt = file.size / 1024 / 1024 < 10;
    if (!isLt) {
        message.error(t("只能上传10M以内的文件"));
        return false;
    }
    return true;
};

/** 删除已上传文件 */
const delFile = () => {
    fileList.value = [];
    isSuccess.value = false;
    message.success(t("删除成功"));
};

/** 下载导入模板 */
const downloadTpl = async () => {
    const result = await userImportTemplate();
    requestExport(result, t("导入客户模板"));
};

/** 执行导入 */
const importNow = () => {
    let formData = new FormData();
    formData.append("file", fileList.value[0]);
    loading.value = true;
    errorMessage.value = "";
    userImportSubmit(formData)
        .then((data: any) => {
            isSuccess.value = true;
            message.success(t("导入成功"));
            loading.value = false;
        })
        .catch((err: any) => {
            loading.value = false;
            isSuccess.value = false;
            errorMessage.value = err.message;
        });
};

/** 下载导入失败数据 */
const downloadError = async () => {
    let formData = new FormData();
    formData.append("file", fileList.value[0]);
    getUserImportErrorData(formData)
        .then((res: any) => {
            requestExport(res, t("失败数据"));
        })
        .catch((err: any) => {
            message.error(err.message);
        });
};

/** 清空并继续导入 */
const clearFile = () => {
    fileList.value = [];
    errorMessage.value = "";
    isSuccess.value = false;
};

/** 完成导入 */
const handleComplete = () => {
    emit("submitCallback");
};
</script>

<style scoped lang="less">
.importUserDialog {
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
    .download-tpl-link {
        color: #268bff;
        margin-left: 10px;
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
        font-family:
            PingFangSC-Regular,
            PingFang SC;
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
        font-family:
            PingFangSC-Regular,
            PingFang SC;
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
            font-family: PingFangSC-Regular;
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
}
.footer {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
    color: #1e2226;
    font-family:
        PingFangSC-Regular,
        PingFang SC;
    font-size: 14px;
    font-weight: 400;
    padding: 20px 0;
    .title {
        color: #1f2026;
        font-family:
            PingFangSC-Semibold,
            PingFang SC;
        font-size: 16px;
        font-weight: 600;
    }
}
:deep(.el-upload-dragger) {
    border: none;
}
.error-text {
    word-break: break-all;
    line-height: 20px;
}
</style>
