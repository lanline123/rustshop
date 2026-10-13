<template>
    <div class="container product-style-info">
        <div class="content_wrapper">
            <div class="step-header">
                <div class="tab-row">
                    <div :class="['tab-item', { active: currentStep === 0 }]" @click="handleStepChange(0)">{{ $t('1. 基本信息设置') }}</div>
                    <div :class="['tab-item', { active: currentStep === 1 }]" @click="handleStepChange(1)">{{ $t('2. 管理商品') }}</div>
                </div>
            </div>

            <div class="step-content" v-if="!loading">
                <!-- Step 1: 基本信息设置 -->
                <div v-show="currentStep === 0" class="lyecs-form-table">
                    <el-form ref="formRef" :rules="rules" :model="formState" label-width="auto">
                        <el-form-item prop="styleName" :label="$t('活动名称')">
                            <el-input v-model="formState.styleName" style="width: 500px" :maxlength="120" :placeholder="$t('请输入活动名称')" />
                        </el-form-item>
                        <el-form-item prop="timeRange" :label="$t('活动时间')">
                            <div>
                                <SelectTimeInterval
                                    width="250px"
                                    v-model:start-date="startDateStr"
                                    v-model:end-date="endDateStr"
                                    :clearable="false"
                                    type="datetime"
                                    value-format="YYYY-MM-DD HH:mm:ss"
                                    disable-before-today
                                />
                                <div class="extra">{{ $t('请选择开始日期和结束日期') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item prop="styleImage" :label="formState.styleType === 1 ? $t('边框图') : $t('H5背景图')">
                            <div>
                                <FormAddGallery
                                    :photo="formState.styleImage"
                                    pageType="overseas"
                                    :dataType="19"
                                    @update:photo="handleImageChange('styleImage', $event)"
                                />
                                <div class="extra">
                                    <span v-if="formState.styleType === 1"
                                        >{{ $t('图片上传建议png格式，大小建议800x800，请保持图片中心为透底状态，以免遮住商品主图。') }}</span
                                    >
                                    <span v-else
                                        >{{ $t('大小建议{isOverseas750x184px710x180x}，请尽量上传颜色较深的背景图，以免影响文字内容展示，', { isOverseas750x184px710x180x: isOverseas() ? "750x184px" : "710x180x" }) }}</span
                                    >

                                    <el-popover placement="right-end" :width="300" trigger="click">
                                        <template #reference>
                                            <a class="btn-link">{{ $t('示例') }}</a>
                                        </template>
                                        <img :src="formState.styleType === 1 ? borderStyleImg : activityStyleImg" style="width: 280px" />
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item prop="pcImage" :label="$t('PC背景图')" v-if="formState.styleType === 2">
                            <div>
                                <FormAddGallery
                                    :photo="formState.pcImage"
                                    pageType="overseas"
                                    :dataType="19"
                                    @update:photo="handleImageChange('pcImage', $event)"
                                />
                                <div class="extra">{{ $t('大小建议{isOverseas740x110px810x50x}，请尽量上传颜色较深的背景图，以免影响文字内容展示，', { isOverseas740x110px810x50x: isOverseas() ? "740x110px" : "810x50x" }) }}<el-popover placement="right-end" :width="700" trigger="click">
                                        <template #reference>
                                            <a class="btn-link">{{ $t('示例') }}</a>
                                        </template>
                                        <img :src="pcImage" style="width: 680px" />
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item prop="status" :label="$t('是否开启')">
                            <div>
                                <el-radio-group v-model="formState.status">
                                    <el-radio :label="1">{{ $t('开启') }}</el-radio>
                                    <el-radio :label="0">{{ $t('关闭') }}</el-radio>
                                </el-radio-group>
                                <div class="extra">{{ $t('开启后，将根据设置的活动时间自动展示商品主图{styleType1}；关闭则不展示。', { styleType1: formState.styleType === 1 ? "边框" : "背景" }) }}</div>
                            </div>
                        </el-form-item>
                    </el-form>
                </div>

                <!-- Step 2: 管理商品 -->
                <div v-show="currentStep === 1" class="step-product">
                    <SelectStyleProduct
                        v-model:productIds="formState.productIds"
                        v-model:groupIds="formState.groupIds"
                        v-model:brandIds="formState.brandIds"
                        :initialTab="initialSelectTab"
                        :initProductList="initProductList"
                        :initGroupList="initGroupList"
                        :initBrandList="initBrandList"
                    />
                </div>
            </div>

            <!-- 底部操作按钮 -->
            <div class="step-footer">
                <template v-if="currentStep === 0">
                    <el-button @click="onCancel">{{ $t('取消') }}</el-button>
                    <el-button type="primary" @click="goToNextStep">{{ $t('下一步') }}</el-button>
                </template>
                <template v-else>
                    <el-button @click="goToPrevStep">{{ $t('上一步') }}</el-button>
                    <el-button type="primary" :loading="submitLoading" @click="onSubmit">{{ $t('确认') }}</el-button>
                </template>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { ProductStyleFormState } from "@/types/promotion/productStyle";
import { getProductStyleDetail, createProductStyle, updateProductStyle } from "@/api/promotion/productStyle";
import { FormAddGallery } from "@/components/gallery";
import { SelectTimeInterval } from "@/components/select";
import SelectStyleProduct from "./src/SelectStyleProduct.vue";
import { timestampToDate } from "@/utils/time";
import borderStyleImg from "@/assets/promotion/productStyle/borderStyle.png";
import { isOverseas } from "@/utils/version";
import activityStyleImage from "@/assets/promotion/productStyle/activityStyle.png";
import activityStyleOverseasImage from "@/assets/promotion/productStyle/activityStyleOverseas.png";
import pcImageDefault from "@/assets/promotion/productStyle/pcImage.png";
import pcImageOverseas from "@/assets/promotion/productStyle/pcImageOverseas.png";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: { type: Number, default: 0 },
    act: { type: String, default: "" },
    styleType: { type: Number, default: 1 },
    initStep: { type: Number, default: 0 },
    isDialog: { type: Boolean, default: false }
});

const loading = ref<boolean>(true);
const submitLoading = ref<boolean>(false);
const initialSelectTab = ref<"product" | "group" | "brand">("product");
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const styleType = ref<number>(props.isDialog ? props.styleType : Number(query.styleType) || 1);
const currentStep = ref<number>(props.isDialog ? props.initStep : 0);
const formRef = shallowRef();

const formState = ref<ProductStyleFormState>({
    styleName: "",
    styleType: styleType.value,
    styleImage: "",
    pcImage: "",
    startTime: "",
    endTime: "",
    status: 1,
    productIds: [],
    groupIds: [],
    brandIds: []
});

const initProductList = ref<any[]>([]);
const initGroupList = ref<any[]>([]);
const initBrandList = ref<any[]>([]);

const startDateStr = ref<string>("");
const endDateStr = ref<string>("");
const defaultBorderImage = "https://oss.tigshop.com/img/gallery/202604/17760479268MCVYCNZIPaROrIiWP.jpeg?x-oss-process=image";
const normalH5BackgroundImage = "https://oss.tigshop.com/img/gallery/202604/1775723771kNgAOEN7kmo2SWGHaq.jpeg?x-oss-process=image";
const normalPcBackgroundImage = "https://oss.tigshop.com/img/gallery/202604/1775723771q6oPZFIoetf9EpG8w7.jpeg?x-oss-process=image";
const overseasPcBackgroundImage = "https://oss.tigshop.com/img/gallery/202604/1775723771tbAWsfKQzpzSYXzV8i.jpeg?x-oss-process=image";
const overseasH5BackgroundImage = "https://oss.tigshop.com/img/gallery/202604/17757237712Z1VAvJFySoAf3EezG.jpeg?x-oss-process=image";
const defaultH5BackgroundImage = isOverseas() ? overseasH5BackgroundImage : normalH5BackgroundImage;
const defaultPcBackgroundImage = isOverseas() ? overseasPcBackgroundImage : normalPcBackgroundImage;
const activityStyleImg = isOverseas() ? activityStyleOverseasImage : activityStyleImage;
const pcImage = isOverseas() ? pcImageOverseas : pcImageDefault;

const applyDefaultBackgroundImages = (styleTypeValue: number) => {
    if (styleTypeValue === 1) {
        formState.value.styleImage = formState.value.styleImage || defaultBorderImage;
        return;
    }
    if (styleTypeValue !== 2) return;
    formState.value.styleImage = formState.value.styleImage || defaultH5BackgroundImage;
    formState.value.pcImage = formState.value.pcImage || defaultPcBackgroundImage;
};

const validateImage = (_rule: any, _value: any, callback: any) => {
    if (!formState.value.styleImage) {
        callback(new Error(t('请上传样式图片')));
        return;
    }
    callback();
};

const validatePcImage = (_rule: any, _value: any, callback: any) => {
    if (formState.value.styleType === 2 && !formState.value.pcImage) {
        callback(new Error(t('请上传PC背景图')));
        return;
    }
    callback();
};

const validateTime = (_rule: any, _value: any, callback: any) => {
    if (!startDateStr.value || !endDateStr.value) {
        callback(new Error(t('请选择活动时间')));
        return;
    }
    callback();
};

const handleImageChange = async (field: "styleImage" | "pcImage", value: string) => {
    formState.value[field] = value;
    if (!formRef.value) return;
    if (field === "pcImage" && formState.value.styleType !== 2) return;
    if (value) {
        formRef.value.clearValidate(field);
        return;
    }
    try {
        await formRef.value.validateField(field);
    } catch {
        // keep validation message displayed
    }
};

const rules = {
    styleName: [{ required: true, message: t('请输入活动名称'), trigger: "blur" }],
    styleImage: [{ required: true, validator: validateImage, trigger: "change" }],
    pcImage: [{ required: true, validator: validatePcImage, trigger: "change" }],
    timeRange: [{ required: true, validator: validateTime, trigger: "change" }]
};

onMounted(() => {
    if (action.value === "detail") {
        fetchDetail();
    } else {
        applyDefaultBackgroundImages(formState.value.styleType);
        loading.value = false;
    }
});

const fetchDetail = async () => {
    try {
        const result = await getProductStyleDetail({ id: id.value });
        const toDateStr = (val: any) => (typeof val === "string" ? val : timestampToDate(val));
        const hasGroupIds = Array.isArray(result.groupIds) && result.groupIds.length > 0;
        const hasBrandIds = Array.isArray(result.brandIds) && result.brandIds.length > 0;
        const selectedTab = hasGroupIds ? "group" : hasBrandIds ? "brand" : "product";
        initialSelectTab.value = selectedTab;
        formState.value = {
            styleId: result.styleId,
            styleName: result.styleName,
            styleType: result.styleType,
            styleImage: result.styleImage,
            pcImage: result.pcImage || "",
            startTime: result.startTime ? toDateStr(result.startTime) : "",
            endTime: result.endTime ? toDateStr(result.endTime) : "",
            status: result.status,
            productIds: selectedTab === "product" ? result.productIds || [] : [],
            groupIds: selectedTab === "group" ? result.groupIds || [] : [],
            brandIds: selectedTab === "brand" ? result.brandIds || [] : []
        };
        applyDefaultBackgroundImages(formState.value.styleType);
        initProductList.value = selectedTab === "product" ? result.productList || [] : [];
        initGroupList.value = selectedTab === "group" ? result.groupList || [] : [];
        initBrandList.value = selectedTab === "brand" ? result.brandList || [] : [];
        startDateStr.value = formState.value.startTime;
        endDateStr.value = formState.value.endTime;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

const goToNextStep = async () => {
    try {
        await formRef.value.validate();
        currentStep.value = 1;
    } catch {
        // validation failed
    }
};

const goToPrevStep = () => {
    currentStep.value = 0;
};

const handleStepChange = async (step: number) => {
    if (step === currentStep.value) return;
    if (step === 0) {
        goToPrevStep();
        return;
    }
    await goToNextStep();
};

const onCancel = () => {
    emit("close");
};

const onSubmit = async () => {
    formState.value.startTime = startDateStr.value;
    formState.value.endTime = endDateStr.value;

    if (formState.value.endTime <= formState.value.startTime) {
        message.error(t('结束时间必须大于开始时间'));
        return;
    }

    try {
        submitLoading.value = true;
        emit("update:confirmLoading", true);
        const submitData = { ...formState.value };
        let result;
        if (action.value === "add") {
            result = await createProductStyle(submitData);
        } else {
            result = await updateProductStyle(submitData);
        }
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        submitLoading.value = false;
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    if (currentStep.value === 0) {
        goToNextStep();
    } else {
        onSubmit();
    }
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.product-style-info {
    display: flex;
    flex-direction: column;
    height: 100%;

    .content_wrapper {
        display: flex;
        flex-direction: column;
        height: 100%;
    }

    .step-header {
        padding-bottom: 20px;
        background-color: #fff;
        flex-shrink: 0;

        .tab-row {
            display: flex;
            width: 100%;
            height: 38px;
            box-sizing: border-box;

            .tab-item {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100%;
                background-color: var(--tig-item-active-bg);
                color: #314f73;
                font-size: 14px;
                cursor: pointer;
                margin-right: -10px;
                padding-left: 10px;
                clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%, 15px 50%);

                &:first-child {
                    padding-left: 0;
                    clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%);
                }

                &:last-child {
                    margin-right: 0;
                    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%, 15px 50%);
                }

                &.active {
                    color: #fff;
                    background-color: var(--tig-primary);
                }
            }
        }
    }

    .step-content {
        flex: 1;
        overflow: hidden;
    }

    .step-product {
        height: 100%;
    }

    .step-footer {
        display: flex;
        justify-content: flex-end;
        padding-top: 20px;
        // border-top: 1px solid #f0f0f0;
        flex-shrink: 0;
        gap: 8px;
        position: relative;
        &::before {
            position: absolute;
            content: "";
            display: block;
            width: calc(100% + 48px);
            height: 1px;
            background-color: #f0f0f0;
            top: 0;
            left: -24px;
        }
    }

    .extra {
        color: #999;
        font-size: 12px;
        line-height: 1.5;
        margin-top: 4px;
    }
}
</style>
