<template>
    <tig-layout :title="title">
        <template v-if="id" #navbar-right>
            <view class="navbar-del" @click="handleDel">{{ $t("删除") }}</view>
        </template>
        <view class="address-edit-main">
            <view class="address-edit-content">
                <up-form ref="formRef" :model="formModel" :rules="rules" label-width="165rpx" label-position="top" error-type="message">
                    <up-form-item :label="$t('收货人')" prop="consignee" required :border-bottom="false">
                        <view class="icon">*</view>
                        <up-input v-model="form.consignee" border="none" clearable :placeholder="$t('请输入姓名')" />
                    </up-form-item>
                    <up-form-item class="mobile-form-item" :label="$t('手机号码')" prop="mobile" required :border-bottom="false">
                        <view class="icon">*</view>
                        <up-input v-model="form.mobile" border="none" clearable :placeholder="$t('请输入手机号码')">
                            <template #prefix>
                                <tig-mobile-areacode v-model="form.mobileAreaCode" @update:model-value="handleMobileAreaCodeChange" />
                            </template>
                        </up-input>
                    </up-form-item>

                    <up-form-item v-if="!isOverseasMode" :label="$t('所在地区')" prop="regionNames" required :border-bottom="false">
                        <view class="icon">*</view>
                        <view class="region-input-wrap" :class="{ 'has-locate': isO2o() }" @click="handleShowSelectRegion">
                            <up-input v-model="form.regionNames" border="none" :placeholder="$t('点击选择所在地区')" readonly class="no-pointer" />
                        </view>
                    </up-form-item>
                    <up-form-item v-if="!isOverseasMode" :label="$t('详细地址')" prop="address" required :border-bottom="false">
                        <view class="icon">*</view>
                        <up-input v-model="form.address" border="none" clearable :placeholder="$t('请您填写详细收货地址')" />
                    </up-form-item>
                    <up-form-item v-if="!isOverseasMode" class="postcode-form-item" :label="$t('邮政编码')" prop="postcode" :border-bottom="false">
                        <up-input v-model="form.postcode" border="none" clearable :placeholder="$t('例如：SW1A 1AA, EC1A 1BB')" maxlength="60" />
                    </up-form-item>
                    <template v-if="showContactExtras">
                        <up-form-item :label="$t('固定电话')" prop="telephone" :border-bottom="false">
                            <up-input v-model="form.telephone" border="none" clearable :placeholder="$t('请输入固定电话')" />
                        </up-form-item>
                        <up-form-item :label="$t('邮箱地址')" prop="email" :border-bottom="false">
                            <up-input v-model="form.email" border="none" clearable :placeholder="$t('请输入邮箱地址')" />
                        </up-form-item>
                    </template>
                    <view class="address-tag-box">
                        <view class="tag-label">{{ $t("地址标签") }}</view>
                        <view class="tag-content">
                            <view class="tag-list">
                                <view
                                    v-for="tag in presetTags"
                                    :key="tag"
                                    class="tag-item"
                                    :class="{ active: !isCustomTag && form.addressTag === tag }"
                                    @click="selectPresetTag(tag)"
                                >
                                    {{ $t(tag) }}
                                </view>
                            </view>
                            <view class="custom-tag-row">
                                <view class="tag-item" :class="{ active: isCustomTag }" @click="handleCustomTag">{{ $t("自定义") }}</view>
                                <view v-if="isCustomTag" class="custom-tag-input">
                                    <uni-easyinput
                                        v-model="customTag"
                                        type="text"
                                        :maxlength="5"
                                        primary-color="rgb(192, 196, 204)"
                                        :input-border="false"
                                        :styles="{ backgroundColor: 'transparent' }"
                                        :placeholder="$t('限5字')"
                                        @input="handleCustomTagInput"
                                    />
                                    <text class="count">{{ customTag.length }}/5</text>
                                </view>
                            </view>
                        </view>
                    </view>
                    <view class="cell-warp default">
                        <view class="label">{{ $t("设为默认配送地址") }}</view>
                        <view class="switch"> <up-switch v-model="isDefault" inactive-color="#cccccc" active-color="var(--general)" /> </view>
                    </view>
                </up-form>
            </view>
        </view>
        <tig-fixed-placeholder background-color="#fff">
            <view class="btn-box">
                <tig-button2 :loading="isLoading" theme style="height: 73rpx" @click="onSubmit"> {{ $t("保存") }} </tig-button2>
            </view>
        </tig-fixed-placeholder>
    </tig-layout>
    <selectRegion v-model:show="showSelectRegion" v-model="form.regionIds" :root-region-id="selectedCountryRootId" @send-region-names="getRegionNames" />
</template>

<script lang="ts" setup>
/**
 * 地址编辑页
 * - 新增或编辑收货地址
 * - 省市区三级联动选择
 * - 表单校验收货人、手机号、详细地址
 * - 支持设为默认地址
 */
import selectRegion from "@/components/region/selectRegion.vue";
import { computed, nextTick, reactive, ref } from "vue";
import { onLoad, onShow } from "@dcloudio/uni-app";
import { getAddressData, updateAddressData, addAddressData, getAddressTagList, delAddress } from "@/api/user/address";
import { useI18n } from "vue-i18n";
import { isOverseas, isO2o } from "@/utils";
import { isPostcodeValid } from "./utils/postcode";

const { t } = useI18n();
const isOverseasMode = isOverseas();

const title = ref(t("添加地址"));

const id = ref(null);
const addIsSelected = ref(0);

interface AddressForm {
    consignee: string;
    regionIds: number[];
    address: string;
    postcode: string;
    mobile: string;
    telephone: string;
    email: string;
    regionNames: string;
    isDefault: number;
    mobileAreaCode?: string;
    addressTag: string;
    positionAddress?: string;
    latitude?: number;
    longitude?: number;
}

const form = reactive<AddressForm>({
    consignee: "",
    regionIds: [] as number[],
    address: "",
    postcode: "",
    mobile: "",
    telephone: "",
    email: "",
    regionNames: "",
    isDefault: 0,
    mobileAreaCode: "86",
    addressTag: "",
    positionAddress: "",
});

/** 国家根节点；未选择国家时为空，地区选择器按原有接口取数 */
const selectedCountryRootId = ref<number | undefined>(undefined);


const isDefault = ref(false);
const DEFAULT_ADDRESS_TAGS = ["家", "公司", "学校"];
const presetTags = ref<string[]>([...DEFAULT_ADDRESS_TAGS]);
const isCustomTag = ref(false);
const customTag = ref("");

const loadAddressTags = async () => {
    try {
        const result = await getAddressTagList();
        if (Array.isArray(result) && result.length > 0) {
            presetTags.value = result;
        }
    } catch (error) {
        console.error("获取地址标签失败:", error);
    }
};

const selectPresetTag = (tag: string) => {
    isCustomTag.value = false;
    customTag.value = "";
    form.addressTag = form.addressTag === tag ? "" : tag;
};

const handleCustomTag = () => {
    isCustomTag.value = true;
    form.addressTag = customTag.value;
};

const handleCustomTagInput = (value: string) => {
    customTag.value = value;
    form.addressTag = value;
};


const formModel = computed(() => {
    const model: Record<string, any> = { ...form };
    return model;
});

const showContactExtras = computed(() => {
    return true;
});

const handleMobileAreaCodeChange = (value: string | number) => {
    form.mobileAreaCode = String(value || "").replace(/\D/g, "");
    formRef.value?.validateField?.("mobile");
};

const rules: Record<string, any> = {
    consignee: [
        {
            required: true,
            message: t("请您填写收货人姓名"),
            trigger: ["blur", "change"]
        }
    ],
    regionNames: [
        {
            required: true,
            message: t("请选择所在地区"),
            trigger: ["blur", "change"]
        },
        {
            validator: () => {
                return form.regionIds.length >= 1;
            },
            message: t("请选择所在地区"),
            trigger: ["blur", "change"]
        }
    ],
    address: [
        {
            required: true,
            message: t("请您填写详细收货地址"),
            trigger: ["blur", "change"]
        }
    ],
    postcode: [
        {
            validator: (_rule: unknown, value: string) => isPostcodeValid(value, isOverseasMode),
            message: t("请您填写邮政编码"),
            trigger: ["blur", "change"]
        }
    ],
    mobile: [
        {
            required: true,
            message: t("请您填写收货人手机号码"),
            trigger: ["blur", "change"]
        },
        {
            validator: (_rule: unknown, value: string) => {
                if (isOverseas()) {
                    return !!value;
                }
                const regex = /^(?:(?:\+|00)86)?1[3-9]\d{9}$/;
                return regex.test(value);
            },
            message: t("请输入正确的手机号码"),
            trigger: ["blur", "change"]
        }
    ]
};


onShow(() => {
    nextTick(() => {
        formRef.value?.setRules?.(rules);
    });
});

const isCheck = ref(false);

onLoad((option: any) => {

    loadAddressTags();

    if (option) {
        addIsSelected.value = Number(option.isSelected) === 1 ? 1 : 0;

        if (option.id) {
            title.value = t("编辑地址");
            id.value = option.id;
            __getAddressData();
        }

        if (option.type && option.type === "check") {
            isCheck.value = true;
        }
    }
});

const __getAddressData = async () => {
    try {
        const result = await getAddressData({ id: id.value });
        const { regionNames } = result;
        Object.assign(form, result);
        form.postcode = result.postcode ?? "";
        form.regionNames = regionNames.join(" ");
        if (!form.mobileAreaCode) {
            form.mobileAreaCode = "86";
        }
        isDefault.value = form.isDefault === 1;
        if (form.addressTag && !presetTags.value.includes(form.addressTag)) {
            isCustomTag.value = true;
            customTag.value = form.addressTag;
        }
    } catch (error) {
        console.error(error);
    }
};


const getRegionNames = async (val: string[]) => {
    const regionNames = val.join(" ");
    form.regionNames = regionNames;
};

const showSelectRegion = ref(false);
const handleShowSelectRegion = () => {
    showSelectRegion.value = true;
};

const buildAddressPayload = () => {
    return { ...form, isDefault: isDefault.value ? 1 : 0 };
};

const isLoading = ref(false);
const formRef = ref();
const onSubmit = () => {
    if (isCustomTag.value) {
        form.addressTag = customTag.value;
    }
    formRef.value
        .validate()
        .then(() => {
            form.postcode = form.postcode.trim();
            form.isDefault = isDefault.value ? 1 : 0;
            if (id.value) {
                edit();
            } else {
                add();
            }
        })
        .catch((err: any) => {
            console.log("表单错误信息：", err);
        });
};

const add = async () => {
    isLoading.value = true;
    try {
        await addAddressData({
            ...buildAddressPayload(),
            ...(addIsSelected.value === 1 ? { isSelected: 1 } : {})
        });
        uni.showToast({
            title: t("添加地址成功"),
            icon: "none",
            duration: 1000
        });

        setTimeout(() => {
            uni.navigateBack({
                success: function () {
                    uni.$emit("refreshData"); // 发送刷新信号
                    isLoading.value = false;
                }
            });
        }, 1000);
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
        isLoading.value = false;
    }
};

const edit = async () => {
    isLoading.value = true;
    try {
        // 后端 user/address/update 用 addressId 定位记录，id 保留以兼容旧调用
        await updateAddressData({ id: id.value, addressId: id.value, ...buildAddressPayload() });
        uni.showToast({
            title: t("编辑地址成功"),
            icon: "none",
            duration: 1000
        });

        setTimeout(() => {
            uni.navigateBack({
                success: function () {
                    if (isCheck.value) {
                        uni.$emit("refreshAddress");
                    } else {
                        uni.$emit("refreshData");
                    }

                    isLoading.value = false;
                }
            });
        }, 1000);
    } catch (error: any) {
        console.error(error);
        uni.showToast({
            title: error.message,
            icon: "none",
            duration: 1000
        });
    }
};

const handleDel = () => {
    if (!id.value) return;
    uni.showModal({
        title: t("提示"),
        content: t("确定删除吗？"),
        confirmText: t("确定"),
        cancelText: t("取消"),
        success: async (result) => {
            if (!result.confirm) return;
            try {
                await delAddress({ id: id.value });
                uni.showToast({ title: t("删除成功"), icon: "none", duration: 1000 });
                setTimeout(() => {
                    uni.navigateBack({
                        success: () => {
                            uni.$emit(isCheck.value ? "refreshAddress" : "refreshData");
                        }
                    });
                }, 1000);
            } catch (error: any) {
                console.error(error);
                uni.showToast({ title: error.message, icon: "none", duration: 1000 });
            }
        }
    });
};

</script>

<style lang="scss" scoped>
:deep(.u-form-item) {
    margin-bottom: 0;
    padding-bottom: 20rpx;
    width: 100%;
    background-color: #fff;
    position: relative;
    &:nth-child(1) {
        border-top: 1rpx solid #f5f6fa;
    }
    &:nth-child(n + 2) {
        margin-bottom: 16rpx;
    }
    &:nth-child(n + 5) {
        .u-input__content {
            padding-left: 32rpx !important;
        }
    }
    &:nth-child(2) {
        .u-form-item__body__right {
            padding-left: 55rpx !important;
            .u-input__content {
                padding-left: 0 !important;
            }
        }
    }
    &.mobile-form-item {
        .u-form-item__body__right {
            padding-left: 55rpx !important;
            .u-input__content {
                padding-left: 0 !important;
            }
        }

        .icon {
            left: 24rpx;
        }
    }
    &.postcode-form-item--required {
        .u-input__content {
            padding-left: 55rpx !important;
        }
    }
    // 跨境模式表单项数量随国家变化，不能沿用按 nth-child 给的左内边距，否则必填星号会压住 placeholder
    &.form-item--required {
        .u-input__content {
            padding-left: 55rpx !important;
        }
    }
    &.form-item--optional {
        .u-input__content {
            padding-left: 32rpx !important;
        }
    }
    .u-form-item__body {
        padding: 0 !important;
    }
    .u-form-item__body__left {
        width: 100% !important;
        margin-bottom: 0 !important;
    }
    .u-form-item__body__left__content__label {
        box-sizing: border-box;
        width: 100% !important;
        padding: 0 0 0 32rpx !important;
        border-bottom: 1rpx solid #f5f6fa;
        height: 56rpx;
        background-color: #fff;
        font-size: 24.5rpx;
        align-items: center;
        display: flex;
        font-weight: 700;
        line-height: 33rpx;
        text-align: left;
        color: #1a1a1a;
    }
    .u-form-item__body__left__content__required {
        display: none;
    }
    .u-form-item__body__right {
        position: relative;
        margin-top: 15rpx;
        .icon {
            position: absolute;
            top: 15rpx;
            left: 32rpx;
            color: #cc0c1c;
            font-size: 32rpx;
            z-index: 1;
        }
    }
}
:deep(.u-input__content) {
    height: 57rpx;
    padding-left: 55rpx !important;
    .uni-input-placeholder {
        font-size: 24rpx !important;
        color: #c4c6ce;
    }
}
:deep(.u-form-item__body__right__message) {
    position: relative;
    top: 0;
    left: 32rpx;
    padding-top: 4rpx;
    margin-left: 0 !important;
    transform: none;
}
:deep(.u-input__content__field-wrapper__field) {
    font-size: 24rpx !important;
}

:deep(.u-input--disabled) {
    background-color: #fff;
}

.address-tag-box {
    padding: 24rpx 32rpx;
    background-color: #fff;

    .tag-label {
        margin-bottom: 20rpx;
        color: #1a1a1a;
        font-size: 24.5rpx;
        font-weight: 700;
    }

    .tag-list,
    .custom-tag-row {
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: 20rpx;
    }

    .custom-tag-row {
        margin-top: 20rpx;
    }

    .tag-item {
        padding: 12rpx 34rpx;
        border: 1rpx solid transparent;
        border-radius: 8rpx;
        background-color: #f5f5f5;
        color: #666;
        font-size: 26rpx;

        &.active {
            border-color: var(--general);
            background-color: #fff;
            color: var(--general);
        }
    }

    .custom-tag-input {
        display: flex;
        flex: 1;
        min-width: 0;
        align-items: center;
        padding: 0 20rpx;
        border-radius: 8rpx;
        background-color: #f5f5f5;

        :deep(.uni-easyinput),
        :deep(.uni-easyinput__content),
        :deep(.uni-easyinput__content-input),
        :deep(.uni-input-input),
        :deep(.uni-input-wrapper),
        :deep(.is-input-border),
        :deep(input) {
            min-width: 0;
            background-color: transparent !important;
        }

        .count {
            padding-left: 12rpx;
            color: #999;
            font-size: 22rpx;
            white-space: nowrap;
        }
    }
}

.navbar-del {
    color: #333;
    font-size: 28rpx;
    white-space: nowrap;
}

:deep(.u-input__content) {
    padding: 0 10px 0 0;
}

:deep(.uni-input-wrapper) {
    .uni-input-input {
        caret-color: inherit !important;
    }
}

.btn-box {
    padding: 25rpx;
    font-weight: 600;
}

.address-edit-main {
    padding-bottom: 0rpx;

    .address-edit-content {
        padding-bottom: 5rpx;

        .cell-warp {
            box-sizing: border-box;
            width: 100%;
            padding: 0 28rpx;
            background: #fff;
            .label {
                font-size: 24.5rpx;
                font-weight: 400;
                line-height: 33rpx;
                text-align: left;
                color: #1a1a1a;
                white-space: nowrap;
            }
            .switch {
                display: flex;
                justify-content: end;
                width: 100%;
            }
            &.default {
                display: flex;
                align-items: center;
                height: 85rpx;
            }
        }
    }
}

.switch {
    height: 100%;
    display: flex;
    align-items: center;
}
.no-pointer {
    pointer-events: none;
}

.region-input-wrap {
    position: relative;
    width: 100%;
}

.region-input-wrap.has-locate {
    :deep(.u-input__content) {
        padding-right: 100rpx !important;
    }
}

.location-box {
    display: flex;
    align-items: center;
    column-gap: 6rpx;
    padding-left: 10rpx;
    position: absolute;
    right: 32rpx;
    top: 50%;
    transform: translateY(-50%);
    font-size: 24rpx;
    color: #1a1a1a;
}

.country-row {
    box-sizing: border-box;
    display: flex;
    width: 100%;
    align-items: center;
    min-height: 57rpx;
    // 上方 15rpx 间距来自 .u-form-item__body__right 的 margin-top，这里补等量下内边距，避免内容贴住分割线
    padding: 0 32rpx 15rpx;
    color: #333;
    font-size: 28rpx;
    border-bottom: 1rpx solid #f5f6fa;

    &__label {
        flex: 1;
        min-width: 0;
    }
}

.row-arrow {
    flex-shrink: 0;
    font-size: 17rpx;
    color: #888b94;
    transform: rotate(180deg);
}

.country-picker {
    background-color: #fff;
}

.country-picker__toolbar {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
    border-bottom: 1rpx solid #f2f2f2;
    font-size: 30rpx;
}

.country-picker__cancel {
    color: #909193;
}

.country-picker__confirm {
    color: #3c9cff;
}

.country-picker-view {
    height: 440rpx;
}

.country-picker__item {
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16rpx;
    height: 88rpx;
    font-size: 30rpx;
}

.country-picker__flag,
.country-flag {
    width: 48rpx;
    height: 36rpx;
    flex: 0 0 48rpx;
    margin-right: 16rpx;
    border: 1rpx solid rgba(0, 0, 0, 0.1);
    border-radius: 4rpx;
}

.dynamic-region-picker {
    display: flex;
    flex-direction: column;
    height: 60vh;
    background: #fff;
}

.dynamic-region-picker__title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 96rpx;
    padding: 0 32rpx;
    border-bottom: 1rpx solid #eee;
    font-size: 36rpx;
    font-weight: 600;
}

.dynamic-region-picker__close {
    color: #909193;
    font-size: 48rpx;
    font-weight: 300;
}

.dynamic-region-picker__tabs {
    flex: 0 0 auto;
    white-space: nowrap;
    border-bottom: 1rpx solid #eee;
    background: #f7f7f7;
}

.dynamic-region-picker__tab {
    display: inline-flex;
    align-items: center;
    height: 88rpx;
    padding: 0 28rpx;
    color: #666;
    font-size: 28rpx;
    white-space: nowrap;
    border-bottom: 4rpx solid transparent;

    &.active {
        color: var(--general);
        background: #fff;
        border-bottom-color: var(--general);
    }
}

.dynamic-region-picker__content {
    flex: 1;
    min-height: 0;
}

.dynamic-region-picker__list {
    height: 100%;
}

.dynamic-region-picker__option {
    min-height: 88rpx;
    padding: 24rpx 32rpx;
    box-sizing: border-box;
    color: #333;
    font-size: 28rpx;
    line-height: 40rpx;
    overflow-wrap: anywhere;
    word-break: break-word;

    &.active {
        color: var(--general);
        background: rgba(0, 0, 0, 0.03);
    }
}

.dynamic-region-picker__status {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24rpx;
    min-height: 180rpx;
    color: #909399;
    font-size: 28rpx;
}

.dynamic-region-picker__retry {
    color: var(--general);
}

.region-row__arrow {
    position: absolute;
    right: 32rpx;
    top: 50%;
    transform: translateY(-50%) rotate(180deg);
    pointer-events: none;
}
</style>
