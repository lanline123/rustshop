<template>
    <el-form-item :rules="[{ required: true }]" :label="$t('商品类型')" prop="productType">
        <div>
            <div class="extra mb10" style="margin-top: 3px">{{ $t('不同商品类型可编辑的字段内容不同，商品类型一旦发布后将不可更改') }}</div>
            <RadioType v-model:modelValue="formState.productType" :disabled="action == 'detail'" :radioList="radioList">
                <template #tooltip="{ item }">
                    <div v-if="item.key === 5 && item.disabled" style="width: 240px; text-align: left" @click.stop>
                        <span>{{ $t('该功能已关闭，若开启，请前往设置-->配送设置-->预约上门，开启功能') }}</span>
                        <a class="btn-link ml5" @click.stop="goEnableHomeService">{{ $t('去开启') }}</a>
                    </div>
                    <div v-else-if="item.key === 7 || item.key === 8" class="tooltip-content" @click.stop>
                        <div class="tooltip-text">{{ item.tips }}</div>
                        <el-image :src="getCardExampleImg(item.key)" alt="示例图" class="tooltip-image" fit="contain" @click.stop="openImageViewer(item.key)" />
                    </div>
                    <div v-else style="width: 200px; text-align: left">{{ item.tips }}</div>
                </template>
            </RadioType>
            <div v-if="formState.productType != 1 && formState.productType != 5 && formState.productType != 6 " class="extra mt10">{{ $t('该类型商品同样需要客户填写收货地址') }}</div>
        </div>
    </el-form-item>
    <el-form-item
        v-if="formState.productType === 7 || formState.productType === 8"
        class="inner-item"
        :label="$t('礼品卡名称')"
        prop="cardGroupsId"
        :rules="[{ required: true, validator: validateGiftCard }]"
    >
        <div class="flex flex-justify-between">
            <SelectGiftCardGroup
                ref="selectGiftCard"
                v-if="!loading"
                v-model:cardGroupsId="formState.giftCardGroupsId"
                :cardType="formState.productType === 7 ? 2 : 1"
                :disabled="examine == 1 || type !== ''"
                :productShopId="formState.shopId"
                style="width: 200px"
            ></SelectGiftCardGroup>
            <div class="con-btn">
                <DialogForm
                    ref="giftTypeDialogRef"
                    :title="$t('新建礼品卡')"
                    width="500px"
                    path="product/product/src/components/giftTypeModel"
                    @okCallback="onGiftTypeOk"
                >
                    <el-button link type="primary">{{ $t('新建') }}</el-button>
                </DialogForm>
                <!-- 隐藏触发器：用于脚本打开“新增礼品卡”抽屉 -->
                <DialogForm
                    ref="giftCardAddDialogRef"
                    isDrawer
                    :title="giftCardAddTitle"
                    width="800px"
                    path="promotion/giftCard/Info"
                    :params="giftCardAddParams"
                    style="display: none"
                    @okCallback="onGiftCardAddOk"
                    @closeCallback="refreshGiftCard"
                >
                    <span></span>
                </DialogForm>
                <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                <el-button :loading="giftCardRefreshLoading" link type="primary" @click="refreshGiftCard">{{ $t('刷新') }} </el-button>
            </div>
        </div>
    </el-form-item>
    <el-form-item :rules="[{ required: true, validator: validateImg }]" :label="$t('商品图片')" prop="imgList">
        <FormAddGallery
            v-if="!loading"
            :key="aiBadgeRefreshKey"
            v-model:photos="formState.imgList"
            :disabled="isImageDisabled"
            :isMultiple="true"
            pageType="product"
        >
        </FormAddGallery>
        <div class="extra">{{ $t('您可以通过拖拽来调整相册图片顺序，第一张图将作为商品主图展示') }}</div>
    </el-form-item>
    <el-form-item :label="$t('商品视频')" prop="productVideoInfo">
        <FormAddGalleryVideo v-if="!loading" v-model:video="formState.productVideoInfo" :disabled="examine == 1 || type !== ''"></FormAddGalleryVideo>
        <div class="extra">{{ $t('如有商品视频则商品视频作为商品主图展示') }}</div>
    </el-form-item>
    <el-form-item
        v-if="formState.productType == 3"
        :rules="[{ required: true, validator: validateCardGroup }]"
        class="inner-item"
        :label="$t('电子卡券分组')"
        prop="cardGroupId"
    >
        <div class="flex flex-justify-between">
            <el-select v-model="formState.cardGroupId" :disabled="examine == 1 || type !== ''" :placeholder="$t('请选择')" style="width: 200px">
                <el-option v-for="item in formState.eCardList" :key="item.groupId" :label="item.groupName" :value="item.groupId" />
            </el-select>
        </div>
    </el-form-item>
    <el-form-item :rules="[{ required: true, validator: validateName }]" class="inner-item" :label="$t('商品名称')" prop="productName">
        <BusinessData
            v-model:modelValue="formState.productName"
            :dataId="id"
            :dataType="2"
            :disabled="
                (getShopType() !== 2 && examine == 1) ||
                type !== '' ||
                (getShopType() === 2 && storeSettingInfo.storeAssignProductName === 0 && formState.shopId === 0)
            "
            :maxlength="productNameMaxLength"
            :showWordLimit="true"
            :placeholder="$t('请输入商品名称')"
        ></BusinessData>
    </el-form-item>

    <el-form-item class="inner-item" :label="$t('商品品牌')" prop="brandId">
        <SelectBrand v-if="!loading" v-model:brandId="formState.brandId" :disabled="examine == 1 || type !== ''" style="width: 200px"></SelectBrand>
    </el-form-item>
    <el-form-item :rules="[{ required: true, message: '请选择商品类目!', validator: validateCate }]" class="inner-item" :label="$t('商品类目')" prop="categoryId">
        <div class="flex flex-justify-between">
            <SelectCategory
                v-if="!loading"
                ref="selectCategory"
                v-model:categoryId="formState.categoryId"
                :disabled="examine == 1 || type !== ''"
                :multiple="false"
                :teleported="false"
                style="width: 200px"
            ></SelectCategory>
            <div v-if="examine != 1" class="con-btn">
                <template v-if="getAdminType() === 'admin'">
                    <DialogForm
                        :params="{ act: 'add', parentId: 0 }"
                        isDrawer
                        path="product/category/Info"
                        :title="$t('添加分类')"
                        width="600px"
                        @okCallback="refreshCategory"
                    >
                        <el-button link type="primary">{{ $t('新建') }}</el-button>
                    </DialogForm>
                    <p class="ml10 mr10" style="margin-bottom: 3px">|</p>
                </template>
                <el-button :loading="configLoading" link type="primary" @click="refreshCategory">{{ $t('刷新') }} </el-button>
            </div>
        </div>
    </el-form-item>
    <el-form-item v-if="getAdminType() == 'shop' && getShopType() !== 2" class="inner-item" :label="$t('店铺商品分类')" prop="shopCategoryId">
        <SelectShopProductCategory v-model:categoryId="formState.shopCategoryId" :disabled="examine == 1"> </SelectShopProductCategory>
    </el-form-item>
    <el-form-item class="inner-item" :label="$t('商品重量')" prop="decimal" v-if="type == ''">
        <TigInput
            v-model="formState.productWeight"
            :decimalPlaces="3"
            :disabled="examine == 1"
            class="InputBox"
            :placeholder="$t('请输入')"
            type="decimal"
            width="200px"
        >
            <template #append>Kg</template>
        </TigInput>
    </el-form-item>
    <el-form-item class="inner-item" :label="$t('商品条码')" prop="productTsn" v-if="type == ''">
        <div>
            <TigInput v-model="formState.productTsn" :disabled="examine == 1" classType="tig-form-input" />
            <div class="extra">{{ $t('用于快速识商品所标记的编码，比如：69开头的13位标准码') }}</div>
        </div>
    </el-form-item>
    <el-form-item class="inner-item" :label="$t('商品编码')" prop="productSn" v-if="type == ''">
        <div>
            <TigInput v-model="formState.productSn" :disabled="examine == 1" classType="tig-form-input" />
            <div class="extra">{{ $t('如果您不输入商品编码，系统将自动生成一个唯一的编码') }}</div>
        </div>
    </el-form-item>
    <el-form-item class="inner-item" :label="$t('搜索关键词')" prop="keywords">
        <div class="keywords">
            <div class="keywords-input">
                <BusinessData
                    v-model:modelValue="formState.keywords"
                    :dataId="id"
                    :dataType="2"
                    :disabled="examine == 1 || type !== ''"
                    :placeholder="$t('例如：牙刷 自动 清洁')"
                ></BusinessData>
            </div>
            <el-button :disabled="examine == 1 || !formState.productName" @click="onGetParticiple" style="margin-top: 2px">{{ $t('更新关键词') }}</el-button>
        </div>
        <div class="extra">{{ $t('用空格分隔，为空时会自动根据商品名称分词') }}</div>
    </el-form-item>
    <el-form-item class="inner-item" extra="" :label="$t('商品描述')" prop="productBrief">
        <div>
            <div class="multilingual-textarea-overlay">
                <TigInput
                    v-model="formState.productBrief"
                    :disabled="examine == 1 || type !== ''"
                    :rows="2"
                    classType="tig-form-input"
                    :placeholder="$t('例如：顺丰快递 又快又好')"
                    type="textarea"
                ></TigInput>
                <BusinessData v-model:modelValue="formState.productBrief" :dataId="id" :dataType="2" type="textarea"></BusinessData>
            </div>
            <div class="extra"> {{ $t('该描述可作为“商品卖点”的概述及促销信息补充') }} <el-popover :width="400" placement="right-end" trigger="click">
                    <template #reference>
                        <a>{{ $t('查看示例') }}</a>
                    </template>
                    <template #default>
                        <img src="@/style/images/productBrief.png" style="width: 380px" />
                    </template>
                </el-popover>
            </div>
        </div>
    </el-form-item>
    <el-form-item class="inner-item" :label="$t('是否上架')" prop="productStatus">
        <el-radio-group
            v-model="formState.productStatus"
            :disabled="examine == 1 && getShopType() !== 2"
            style="width: 100%"
            @change="handleProductStatusChange"
        >
            <el-radio :value="1">{{ $t('上架') }}</el-radio>
            <el-radio :value="0">{{ $t('下架') }}</el-radio>
        </el-radio-group>
        <div class="extra">{{ $t('上架则允许销售（未审核的商品无法手动上架）') }}</div>
    </el-form-item>
    <!-- 定时下架 -->
    <el-form-item
        v-if="formState.productStatus"
        class="inner-item"
        label=""
        prop="autoOfflineTime"
        :rules="[{ required: formState.timedOffShelf === 1, message: '请选择定时下架时间', trigger: 'change' }]"
    >
        <div class="timed-offline-box">
            <el-checkbox v-model="formState.timedOffShelf" :true-label="1" :false-label="0" :disabled="examine == 1" @change="handleTimedOffShelfChange"> {{ $t('启用定时下架') }} </el-checkbox>
            <el-date-picker
                v-model="formState.autoOfflineTime"
                type="datetime"
                :placeholder="$t('请选择时间')"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="margin-left: 10px; width: 200px"
                :disabled="!formState.timedOffShelf"
                :disabled-date="disabledDate"
            />
        </div>
    </el-form-item>
    <!-- 定时上架 -->
    <el-form-item
        v-if="!formState.productStatus"
        class="inner-item"
        label=""
        prop="autoOnlineTime"
        :rules="[{ required: formState.timedOffShelf === 1, message: '请选择定时上架时间', trigger: 'change' }]"
    >
        <div class="timed-offline-box">
            <el-checkbox v-model="formState.timedOffShelf" :true-label="1" :false-label="0" :disabled="examine == 1" @change="handleTimedOffShelfChange"> {{ $t('启用定时上架') }} </el-checkbox>
            <el-date-picker
                v-model="formState.autoOnlineTime"
                type="datetime"
                :placeholder="$t('请选择时间')"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="margin-left: 10px; width: 200px"
                :disabled="!formState.timedOffShelf"
                :disabled-date="disabledDate"
            />
        </div>
    </el-form-item>
    <el-image-viewer v-if="showImageViewer" :url-list="[currentPreviewImg]" :initial-index="0" @close="showImageViewer = false" :teleported="true" />
</template>
<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { message } from "ant-design-vue";
import { FormAddGallery, FormAddGalleryVideo } from "@/components/gallery";
import { ProductFormState } from "@/types/product/product.d";
import { getParticiple } from "@/api/product/product";
import { RadioType } from "@/components/radio";
import { SelectBrand, SelectCategory, SelectShopProductCategory, SelectGiftCardGroup } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { isPro, isStore, isOverseas, isS2b2c, isMerchant } from "@/utils/version";
import { getAdminType, getShopType } from "@/utils/storage";
import { getTranslateToSeoIdentification } from "@/api/setting/seo";
import { QuestionFilled } from "@element-plus/icons-vue";
import { useRouter } from "vue-router";
import { ElImageViewer } from "element-plus";
import electronicCardImg from "@/style/images/promotion/giftCard/electronic.png";
import entityCardImg from "@/style/images/promotion/giftCard/entity.png";
import { useServiceConfigStore } from "@/store/serviceStaffConfig";
import { useConfigStore } from "@/store/config";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "closeConfirm"]);
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    examine: {
        type: Number,
        default: 0
    },
    loading: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ""
    },
    action: {
        type: String,
        default: ""
    },
    storeSettingInfo: {
        type: Object,
        default: {}
    },
    formRef: {
        type: Object,
        default: () => {}
    }
});
const router = useRouter();
const configStore = useConfigStore();
const serviceConfigStore = useServiceConfigStore();
const formState = defineModel<ProductFormState>("formState", { default: {} });
const radioList = ref<any[]>([]);
const showImageViewer = ref<boolean>(false);
const currentPreviewImg = ref<string>("");
const isImageDisabled = computed(() => {
    return (
        (getShopType() !== 2 && (props.examine == 1 || props.type !== "")) ||
        (getShopType() === 2 && props.type !== "") ||
        (getShopType() === 2 && props.storeSettingInfo.storeAssignProductImage === 0 && formState.value.shopId === 0)
    );
});

const productNameMaxLength = computed(() => {
    const maxLength = Number(configStore.get("customProductNameLength"));
    return maxLength > 0 ? maxLength : 200;
});

/** 翻译应用后强制画廊重新查询翻译标记 */
const aiBadgeRefreshKey = ref(0);

const resetTimedShelf = () => {
    formState.value.timedOffShelf = 0;
    formState.value.autoOnlineTime = null as any;
    formState.value.autoOfflineTime = null as any;
};

// 切换“上架/下架”时，清理定时上下架残留，避免旧时间回显
const handleProductStatusChange = () => {
    resetTimedShelf();
};

// 勾选/取消定时上下架时，清理对应时间字段
const handleTimedOffShelfChange = (val: number | boolean) => {
    const enabled = val === 1 || val === true;
    if (!enabled) {
        formState.value.autoOnlineTime = null as any;
        formState.value.autoOfflineTime = null as any;
        return;
    }
    // 启用时，确保另一侧时间不残留
    if (formState.value.productStatus) {
        formState.value.autoOnlineTime = null as any;
    } else {
        formState.value.autoOfflineTime = null as any;
    }
};
// 是否是店铺
const isShop = computed(() => {
    return getAdminType() === "shop" && getShopType() === 1;
});

// 根据商品类型获取对应的示例图（7=实体礼品卡，8=电子礼品卡）
const getCardExampleImg = (productType: number) => {
    return productType === 7 ? entityCardImg : electronicCardImg;
};

// 打开图片预览器
const openImageViewer = (productType: number) => {
    currentPreviewImg.value = getCardExampleImg(productType);
    showImageViewer.value = true;
};

const goEnableHomeService = () => {
    router.push({ path: "/setting/home-service" });
};
onMounted(async () => {
    if (isPro()) {
        radioList.value = [
            { key: 1, title: "普通商品", desc: isStore() ? "物流/自提/同城配送" : "物流配送" },
            {
                key: 3,
                title: t('卡密商品'),
                tips: t('可以在菜单中【营销>电子卡券组】创建卡券组，然后在【查看卡列表】添加单个卡密或通过下载【批量导入模板文件】填好卡密后在通过批量导入电子卡券按钮上传该文件批量导入。'),
                desc: t('无需物流')
            },
            {
                key: 2,
                title: t('虚拟商品'),
                tips: t('网盘链接、视频链接，在添加商品添加虚拟商品。用户购买后将在订单详情直接展示。'),
                desc: t('无需物流')
            },
            {
                key: 4,
                title: t('付费内容'),
                tips: t('软件授权、在线课程等。通过简单的操作，即可将这些商品添加到您的商店中，为客户提供更丰富的选择。立即体验，享受便捷的付费内容管理服务。'),
                desc: t('无需物流')
            }
        ];
    } else {
        radioList.value = [{ key: 1, title: t('普通商品') }];
    }
    // 开启o2o版本，并且只有平台端和门店端才有这两个商品类型
    radioList.value.push(
        {
            key: 7,
            title: t('实体礼品卡'),
            desc: t('快递发货实物卡片'),
            tips: t('实体礼品卡：购买后在收到货后刮开图层查看卡密进行兑换，根据礼品卡的类型可用于兑换商品{value}', { value: isShop.value ? "" : t('或充值余额') })
        },
        {
            key: 8,
            title: t('电子礼品卡'),
            desc: t('虚拟卡号，无实体卡片'),
            tips: t('电子礼品卡：购买后可在订单详情查看卡号及卡密，也可转增；根据礼品卡的类型可用于兑换商品{value}', { value: isShop.value ? "" : t('或充值余额') })
        }
    );
});
// 会员名称
const validateName = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('商品名称不能为空')));
        return;
    }
    if (value.length > productNameMaxLength.value) {
        callback(new Error(t('商品名称不能超过{value}个字符', { value: productNameMaxLength.value })));
        return;
    }
    callback();
};
const validateImg = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请选择商品图片')));
        return;
    } else {
        callback();
    }
};
const validateCate = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请选择商品类目')));
        return;
    } else {
        callback();
    }
};
const validateCardGroup = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请选择电子卡券组')));
        return;
    } else {
        callback();
    }
};

const configLoading = ref(false);
const seoLoading = ref<boolean>(false);
const selectCategory = ref<any>();
const refreshCategory = async () => {
    configLoading.value = true;
    try {
        selectCategory.value.loaded = false;
        await selectCategory.value.loadCategory();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            configLoading.value = false;
        }, 200);
    }
};

const selectGiftCard = ref<any>();
const giftCardRefreshLoading = ref(false);
const refreshGiftCard = async () => {
    giftCardRefreshLoading.value = true;
    try {
        selectGiftCard.value.loaded = false;
        await selectGiftCard.value.loadList();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        setTimeout(() => {
            giftCardRefreshLoading.value = false;
        }, 200);
    }
};

const giftTypeDialogRef = ref<any>();
const giftCardAddDialogRef = ref<any>();
const giftCardAddParams = ref<{ act: "add"; groupType: 1 | 2 }>({ act: "add", groupType: 2 });
const giftCardAddTitle = computed(() => (giftCardAddParams.value.groupType === 1 ? t('添加储值卡') : t('添加兑换卡')));

const onGiftTypeOk = (result: any) => {
    if (!result) return;
    const groupType = result.groupType as 1 | 2;
    if (groupType !== 1 && groupType !== 2) return;
    giftCardAddParams.value = { act: "add", groupType };
    nextTick(() => {
        giftCardAddDialogRef.value?.show?.();
    });
};

const onGiftCardAddOk = () => {
    // 新增成功后刷新礼品卡下拉列表
    refreshGiftCard();
};

const onGetParticiple = async () => {
    try {
        const result = await getParticiple({ productName: formState.value.productName });
        formState.value.keywords = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

const onGenerateSeoLink = async () => {
    try {
        seoLoading.value = true;
        const result = await getTranslateToSeoIdentification(formState.value.productName || "");
        formState.value.seoIdentification = result;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        seoLoading.value = false;
    }
};

const validateSeoIdentification = (rule: any, value: any, callback: any) => {
    if (!formState.value.productName && !value) {
        callback(new Error(t('请先输入商品名称')));
        return;
    }
    callback();
};
const validateGiftCard = (rule: any, value: any, callback: any) => {
    if (!formState.value.giftCardGroupsId) {
        callback(new Error(t('请选择礼品卡')));
        return;
    }
    callback();
};

// 禁用当前时间之前的日期
const disabledDate = (time: Date) => {
    return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
};

watch(
    () => formState.value.productName,
    (newVal: string | undefined) => {
        if (newVal) {
            console.log(newVal, "newVal");
            //校验seoIdentification
            props.formRef?.validateField?.("seoIdentification");
        }
    },
    { immediate: true, deep: true }
);
</script>
<style lang="less" scoped>
.con-btn {
    display: flex;
    align-content: center;
    padding-left: 10px;

    span {
        color: #999;
    }

    a {
        display: block;
        word-break: keep-all;
        padding: 0 5px;
    }
}

.rank {
    display: flex;
    width: 100%;
    flex-wrap: wrap;
    /* 允许项目换行 */
    justify-content: flex-start;
    /* 项目在主轴上的对齐方式 */
}

.keywords {
    display: flex;
    align-items: center;
    width: 100%;
    gap: 8px;
}

.keywords-input {
    flex: 1;
    min-width: 0;
}

.multilingual-textarea-overlay {
    position: relative;
    display: block;
    width: 100%;
}

.table-question {
    display: flex;
    align-items: center;
    gap: 4px;
}

.pre-wrap {
    display: block;
    max-width: 250px;
    white-space: pre-wrap;
}

.tooltip-content {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 4px 0;

    .tooltip-text {
        width: 200px;
        text-align: left;
        font-size: 12px;
        line-height: 1.5;
        color: #333;
    }

    .tooltip-image {
        width: 200px;
        height: auto;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        cursor: pointer;

        :deep(.el-image__inner) {
            border-radius: 4px;
        }
    }
}
.timed-offline-box {
    background-color: #f5f7fa;
    padding: 10px 15px;
    display: flex;
    align-items: center;
    width: 100%;
}

.gallery-ai-append {
    height: 80px;
    margin-bottom: 10px;
    margin-left: 6px;
    display: inline-flex;
    align-items: flex-end;
}

.product-translate-entry {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
}

.product-translate-btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 32px;
    padding: 0 14px;
    background: #fff;
    border: 1px solid var(--el-color-primary, #155bd4);
    border-radius: 4px;
    color: var(--el-color-primary, #155bd4);
    font-size: 14px;
    line-height: 1;
    cursor: pointer;

    &:hover {
        background: rgba(21, 91, 212, 0.06);
    }

    &__icon {
        width: 18px;
        height: 18px;
        display: block;
    }
}
</style>
