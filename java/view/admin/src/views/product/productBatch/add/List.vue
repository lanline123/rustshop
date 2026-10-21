/** * @file 商品批量导入 * @description 通过Excel模板批量导入商品数据 * @features * - 下载导入模板 * - 上传Excel文件导入商品 * - 自动创建分类和品牌选项 * -
实时显示导入进度和结果 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="lyecs-form-table">
                        <div class="form-and-notice" style="width: 700px; max-width: 100%">
                            <el-form ref="formRef" :model="formState" label-width="auto">
                                <el-form-item prop="file" :rules="[{ required: true, message: '请选择文件!', trigger: 'change' }]" label-width="0">
                                    <el-upload
                                        ref="uploadRef"
                                        :auto-upload="false"
                                        :limit="1"
                                        :on-change="handleChange"
                                        :on-exceed="handleExceed"
                                        accept=".xls, .xlsx, .csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                                        class="upload-dragger"
                                        drag
                                    >
                                        <div class="upload-dragger-content">
                                            <SvgIcon name="product-wenjian" class="upload-icon" />
                                            <el-button link>{{ $t('添加文件') }}</el-button>
                                            <div class="upload-tip">
                                                <p>{{ $t('仅支持格式为 .xls , .xlsx 和 .csv 的Excel文件') }}</p>
                                                <p>{{ $t('一次最多导入2000条数据，超出时请分批导入。') }}</p>
                                            </div>
                                        </div>
                                    </el-upload>
                                </el-form-item>
                                <el-form-item v-if="showAutoCreateOptions" :label="$t('是否自动添加分类')">
                                    <el-radio-group v-model="formState.isAutoCat" class="width100">
                                        <el-radio :value="1">{{ $t('是') }}</el-radio>
                                        <el-radio :value="0">{{ $t('否') }}</el-radio>
                                    </el-radio-group>
                                    <div class="extra">{{ $t('如果选指否，则分类不存在时会输出错误；如果选指是，则会自动新建分类！') }}</div>
                                </el-form-item>
                                <el-form-item v-if="showAutoCreateOptions" :label="$t('是否自动添加品牌')">
                                    <el-radio-group v-model="formState.isAutoBrand">
                                        <el-radio :value="1">{{ $t('是') }}</el-radio>
                                        <el-radio :value="0">{{ $t('否') }}</el-radio>
                                    </el-radio-group>
                                </el-form-item>
                            </el-form>

                            <div class="notice-warp">
                                <div class="notice-title">{{ $t('上传说明') }}</div>
                                <el-timeline>
                                    <el-timeline-item>
                                        <template #dot>
                                            <div class="timeline-dot"></div>
                                        </template>
                                        <div class="timeline-title">{{ $t('下载模板') }}</div>
                                        <ul class="timeline-list">
                                            <li>
                                                <span class="text-gray">{{ $t('请先') }} </span><a href="#" @click.prevent="productBatchDownload">{{ $t('下载实物商品模板文件') }}</a
                                                ><span class="text-gray"> {{ $t('然后通过上传导入商品') }}</span>
                                            </li>
                                        </ul>
                                    </el-timeline-item>
                                    <el-timeline-item>
                                        <template #dot>
                                            <div class="timeline-dot"></div>
                                        </template>
                                        <div class="timeline-title">{{ $t('上传前准备') }}</div>
                                        <ul class="timeline-list">
                                            <li> {{ $t('商品分类：请按分类级别依次填写名称，使用 | 分隔，比如：家用电器 | 电视 | 平板电视') }} </li>
                                            <li>{{ $t('商品品牌：填写品牌名称') }}</li>
                                            <li>{{ $t('关键词：请用空格分隔') }}</li>
                                            <li>{{ $t('是否上架：填1代表上架，0代表下架') }}</li>
                                        </ul>
                                    </el-timeline-item>
                                    <el-timeline-item>
                                        <template #dot>
                                            <div class="timeline-dot"></div>
                                        </template>
                                        <div class="timeline-title">{{ $t('上传图片') }}</div>
                                        <ul class="timeline-list">
                                            <li>{{ $t('单次最多上传3000张（超出时上传失败）') }}</li>
                                            <li>
                                                <span class="text-dark">{{ $t('请先将图片上传至服务器站点目录') }}</span>{{ $t('（推荐上传至 OSS 或站点根目录 /img/upload/ 下），例如图片完整访问地址为：https://域名/img/upload/2022/1.jpg，填写时请直接填写完整 http 链接，例如 https://域名/img/upload/2022/1.jpg；') }}<span class="text-dark">{{ $t('多张图片请用符号 {sep} 分隔，第一张图片将作为商品主图。', { sep: '|' }) }}</span>
                                            </li>
                                            <li>
                                                <span class="text-dark">{{ $t('商品规格图：') }}</span>{{ $t('非必填，仅多规格商品需要，每行（每个规格）单独填写，单规格商品无需填写。') }}
                                            </li>
                                            <li>{{ $t('规格图的填写格式与商品图片一致，同样需先上传至服务器站点目录后填写完整 http 链接。') }}</li>
                                            <li>{{ $t('每个规格仅支持一张规格图，同一行填写多张时只取第一张。') }}</li>
                                            <li>{{ $t('同一规格值在多行填写了不同图片时，只取第一次出现的那张。') }}</li>
                                            <li>{{ $t('重复导入已有商品时，规格图列有值则覆盖原规格图，留空则保留原有规格图。') }}</li>
                                        </ul>
                                    </el-timeline-item>
                                </el-timeline>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div style="height: 30px"></div>
        <div class="selected-action-warp selected-warp-left"
            :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
            <div class="selected-action">
                <el-button class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { reactive, ref, shallowRef } from "vue";
import { ProductBatchModifyFormState } from "@/types/product/productBatch.d";
import { productBatchDownloadTemplateSubmit, productBatchModifySubmit } from "@/api/product/productBatch";
import { vendorProductBatchDownloadTemplateSubmit, vendorProductBatchModifySubmit } from "@/api/vendor/product";
import { getAdminType } from "@/utils/storage";
import { Modal, message } from "ant-design-vue";
import { genFileId } from "element-plus";
import type { UploadInstance, UploadProps, UploadRawFile } from "element-plus";
import requestExport from "@/utils/export";
import { useThemeStore } from "@/store/theme";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const formRef = shallowRef();
const adminType = getAdminType();
const isVendorAdmin = adminType === "vendor";
const isShopAdmin = adminType === "shop";
const showAutoCreateOptions = !isVendorAdmin && !isShopAdmin;
// 基本参数定义
const formState = reactive<ProductBatchModifyFormState>({
    //初使化用于查询的参数
    isAutoCat: 0,
    isAutoBrand: 0,
    isChangePic: 0,
    isUpload: 1,
    file: ""
});
const uploadRef = ref<UploadInstance>();
const handleExceed: UploadProps["onExceed"] = (files) => {
    uploadRef.value!.clearFiles();
    const file = files[0] as UploadRawFile;
    file.uid = genFileId();
    uploadRef.value!.handleStart(file);
};
const handleChange = (data: any) => {
    let formData = new FormData();
    formData.append("file", data.raw);
    formState.file = formData.get("file");
};
const productBatchDownload = async () => {
    const downloadApi = isVendorAdmin ? vendorProductBatchDownloadTemplateSubmit : productBatchDownloadTemplateSubmit;
    const result = await downloadApi();
    requestExport(result, t('模板文件'));
};
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        const submitApi = isVendorAdmin ? vendorProductBatchModifySubmit : productBatchModifySubmit;
        const submitData = isVendorAdmin ? { file: formState.file } : { ...formState };
        const result = await submitApi(submitData);
        // 后端按组拼接结果文案，单行 toast 会截断，改用可手动关闭的弹框完整展示
        Modal.info({
            title: t('导入结果'),
            content: result.msg,
            okText: t('确定'),
            maskClosable: true
        });
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #fff;
    padding: 20px 0;
    margin-top: 20px;
}

.notice-title {
    font-size: 16px;
    font-weight: bold;
    color: #333;
    margin-bottom: 24px;
}

.timeline-dot {
    width: 14px;
    height: 14px;
    border: 2px solid var(--tig-primary);
    border-radius: 50%;
    background-color: #fff;
    box-sizing: border-box;
}

.timeline-title {
    font-size: 14px;
    color: #333;
    margin-bottom: 12px;
    line-height: 1;
}

.timeline-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
        position: relative;
        padding-left: 14px;
        font-size: 13px;
        color: #999;
        margin-bottom: 10px;
        line-height: 1.5;
        &::before {
            content: "";
            position: absolute;
            left: 0;
            top: 7px;
            width: 4px;
            height: 4px;
            border-radius: 50%;
            background-color: #ccc;
        }

        a {
            color: var(--tig-primary);
            background-color: var(--tig-button-text-hover-bg);
            padding: 2px 6px;
            border-radius: 4px;
        }
    }
}

.text-gray {
    color: #999;
}

.text-dark {
    color: #333;
}

:deep(.el-timeline-item__tail) {
    border-left: 1px solid #e4e7ed;
    left: 6px;
}
:deep(.el-timeline-item__wrapper) {
    padding-left: 24px;
    top: -2px;
}

.lyecs-form-table {
    padding: 14px 0;
    background: #fff;
    border-radius: 6px;
    display: flex;
    justify-content: center;
    align-content: center;
}
.width100 {
    width: 100%;
}

.upload-dragger {
    width: 100%;

    :deep(.el-upload-dragger) {
        border: 1px dashed #d9d9d9;
        border-radius: 6px;
        padding: 40px 0;
        display: flex;
        justify-content: center;
        align-items: center;

        &:hover {
            border-color: #409eff;
        }
    }

    :deep(.el-upload) {
        width: 100%;
    }
}

.upload-dragger-content {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.upload-icon {
    width: 30px;
    height: 30px;
    margin-bottom: 12px;
}

.upload-tip {
    font-size: 13px;
    color: #999;
    line-height: 1.8;
    text-align: center;

    p {
        margin: 0;
    }
}
</style>
