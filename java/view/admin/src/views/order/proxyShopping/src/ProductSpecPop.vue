<template>
    <Modal
        :open="innerOpen"
        :centered="false"
        :footer="null"
        width="650px"
        wrapClassName="proxy-product-pop"
        :title="$t('商品规格')"
        :destroyOnClose="false"
        @cancel="handleCancel"
    >
        <a-spin :spinning="loading">
            <div class="product-info-box">
                <template v-if="loading">
                    <!-- loading 时不渲染任何页面元素，仅做高度占位，避免布局抖动 -->
                    <div class="loading-placeholder" />
                </template>
                <template v-else>
                    <div class="info-top flex">
                        <div class="product-img">
                            <img :src="imageFormat(productImage)" />
                        </div>
                        <div class="info-top-right">
                            <div class="product-name">{{ productData?.item?.productName || productData?.item?.name || "-" }}</div>
                            <div class="productSn">{{ $t('编码：{productSn}', { productSn: productData?.item?.productSn || "-" }) }}</div>
                            <div class="product-price">
                                <Price
                                    :modelValue="price"
                                    :currencyStyle="{ fontSize: '20px' }"
                                    :decimalsStyle="{ fontSize: '22px' }"
                                    :fontStyle="{ fontSize: '28px', fontWeight: '600' }"
                                />
                            </div>
                        </div>
                    </div>

                    <template v-if="specificationList.length > 0">
                        <div class="sku-box">
                            <template v-for="(item, index) in specificationList" :key="index">
                                <div class="sku-items flex-column">
                                    <div class="items-label">{{ item.attrName }}</div>
                                    <div class="items">
                                        <template v-for="(value, subIdx) in item.attrList" :key="value.attributesId ?? subIdx">
                                            <div
                                                class="item"
                                                :class="{
                                                    checked: value.checked,
                                                    disabled: value.skuStock == 0,
                                                    'padding-left': value.attrPicThumb && index === 0
                                                }"
                                                @click="handleChecked(Number(index), Number(subIdx), Number(value.skuStock || 0) === 0)"
                                            >
                                                <template v-if="value.attrPicThumb && index === 0">
                                                    <div class="item-img">
                                                        <img :src="imageFormat(value.attrPicThumb)" />
                                                    </div>
                                                </template>
                                                <div class="item-text">{{ value.attrValue }}</div>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </template>
                    <template v-else>
                        <div class="placeholder" />
                    </template>

                    <template v-if="extraList.length > 0">
                        <div class="sku-box" style="padding-top: 0">
                            <template v-for="(item, index) in extraList" :key="index">
                                <div class="sku-items flex-column">
                                    <div class="items-label">{{ item.attrName }}</div>
                                    <div class="items">
                                        <template v-for="(value, subIdx) in item.attrList" :key="subIdx">
                                            <div class="item" :class="{ checked: value.checked }" @click="handleExtraChecked(Number(index), Number(subIdx))">
                                                <div class="item-text">
                                                    <span>{{ value.attrValue }}</span>
                                                    <span v-if="formatExtraPrice(value?.attrPrice)" class="item-extra-price">
                                                        {{ formatExtraPrice(value?.attrPrice) }}
                                                    </span>
                                                </div>
                                            </div>
                                        </template>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </template>

                    <div class="amount">
                        <div class="amount-label">{{ $t('数量') }}</div>
                        <PosStepper v-model="productNumber" :showDelete="false" :max="stock" size="small" :disabled="stock <= productNumber" />
                    </div>

                    <div class="submit">
                        <el-button class="btn" type="primary" :loading="confirming" @click="handleConfirm">{{ $t('确定') }}</el-button>
                    </div>
                </template>
            </div>
        </a-spin>
    </Modal>
</template>

<script setup lang="ts">
import { computed, ref, shallowRef, watch } from "vue";
import { Modal, message } from "ant-design-vue";
import { imageFormat, priceFormat } from "@/utils/format";
import { getProxyProductDetail, getProxyProductAvailability } from "@/api/proxyShopping/product";
import Price from "./Price.vue";
import PosStepper from "./PosStepper.vue";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = withDefaults(
    defineProps<{
        open: boolean;
        productId?: number;
        productSn?: string;
        userId?: number;
        defaultQuantity?: number;
    }>(),
    {
        open: false,
        defaultQuantity: 1
    }
);

const emit = defineEmits<{
    (e: "update:open", val: boolean): void;
    (
        e: "confirm",
        payload: {
            skuId?: number;
            skuValue?: string;
            quantity: number;
            price?: number;
            stock?: number;
            shopId?: number;
            extraAttrIds?: string;
        }
    ): void;
}>();

const confirming = ref(false);
const loading = ref(false);
// 优化：弹窗先打开再加载数据（用 loading 态兜底），避免点击时“卡住等接口”
const innerOpen = ref(false);
const openRequestId = ref(0);

const price = ref(0);
const productData = shallowRef<any>({});
const productImage = ref<string>("");
const specificationList = ref<any[]>([]);
const extraList = ref<any[]>([]);
const checkedExtraIds = ref<(number | undefined)[]>([]);
const extraAttrIds = computed(() => checkedExtraIds.value.filter((v) => v !== undefined && v !== null && String(v) !== "").join(","));
const skuList = shallowRef<any[]>([]);
const checkedValue = ref<any[]>([]);
// 各规格组当前选中的 attributesId，用于同名规格值区分
const checkedAttrIds = ref<(number | null)[]>([]);
const currentSku = ref<any>({});
const productNumber = ref(1);
const stock = ref<number>(0);

const canSelectSku = computed(() => specificationList.value.length > 0 && skuList.value.length > 0);

type DetailCacheEntry = { at: number; data: any };
const DETAIL_CACHE_TTL = 5 * 60 * 1000; // 5min
const detailCache = shallowRef<Map<string, DetailCacheEntry>>(new Map());
const getDetailCacheKey = () => (props.productId ? `id:${props.productId}` : props.productSn ? `sn:${props.productSn}` : "");

watch(
    () => props.open,
    async (val) => {
        openRequestId.value += 1;
        const rid = openRequestId.value;

        if (!val) {
            innerOpen.value = false;
            return;
        }
        if (!props.productId && !props.productSn) return;

        // 立即打开弹窗，并展示 loading，避免点击等待网络造成“慢”的感受
        innerOpen.value = true;
        loading.value = true;

        try {
            await init();
        } catch (error: any) {
            console.error("Failed to fetch proxy product detail:", error);
            message.error(error?.message || t('获取商品规格信息失败'));
            handleCancel();
            return;
        } finally {
            // 防止快速开关/切换导致异步回调把 loading 误关
            if (rid === openRequestId.value && props.open) {
                loading.value = false;
            }
        }
    }
);

const init = async () => {
    reset();
    productNumber.value = Math.max(1, Number(props.defaultQuantity || 1));

    const params: any = {};
    if (props.productId) params.id = props.productId;
    if (props.productSn) params.productSn = props.productSn;

    const cacheKey = getDetailCacheKey();
    const now = Date.now();
    const cached = cacheKey ? detailCache.value.get(cacheKey) : undefined;
    const result =
        cached && now - cached.at < DETAIL_CACHE_TTL
            ? cached.data
            : await getProxyProductDetail(params).then((res) => {
                  if (cacheKey) detailCache.value.set(cacheKey, { at: now, data: res });
                  return res;
              });

    productData.value = result;
    productImage.value = result?.item?.picThumb || result?.item?.pic || "";
    specificationList.value = result?.attrList?.spe || [];
    extraList.value = result?.attrList?.extra || [];
    skuList.value = result?.skuList || [];

    // 初始化 extra：每组单选，默认不选
    checkedExtraIds.value = (extraList.value || []).map(() => undefined);
    extraList.value.forEach((g: any) => {
        (g?.attrList || []).forEach((opt: any) => {
            opt.checked = false;
        });
    });

    // 初始化默认选中：选第一个有库存的 sku
    if (canSelectSku.value) {
        const index = skuList.value.findIndex((it: any) => Number(it?.skuStock || 0) > 0);
        const skuItem = skuList.value[index >= 0 ? index : 0];
        if (skuItem?.skuValue) {
            checkedValue.value = String(skuItem.skuValue).split("|");
            checkedAttrIds.value = getCheckedAttrIdsBySku(skuItem);
        }
        updateValue();
    } else if (skuList.value.length > 0) {
        currentSku.value = skuList.value[0];
    }

    await getProductAvailabilityData();
};

const getProductAvailabilityData = async () => {
    try {
        if (!props.productId && !productData.value?.item?.productId) return;
        const params: any = {
            id: props.productId || productData.value?.item?.productId,
            userId: props.userId
        };
        if (currentSku.value?.skuId) {
            params.skuId = currentSku.value.skuId;
        }
        if (extraAttrIds.value) {
            params.extraAttrIds = extraAttrIds.value;
        }
        const result = await getProxyProductAvailability(params);
        price.value = Number(result?.price || 0);
        stock.value = Number(result?.stock || 0);
    } catch (error: any) {
        console.error("Failed to fetch proxy product availability:", error);
        message.error(error?.message || t('获取商品库存信息失败'));
    }
};

/**
 * 规格 attributesId 组合 ↔ SKU 映射。
 * 同名规格值导致 skuValue 重复时，按笛卡尔积顺序与 SKU 列表出现顺序一一对应。
 */
const skuSelectionMaps = computed(() => {
    const buckets = new Map<string, any[]>();
    (skuList.value || []).forEach((skuItem: any) => {
        const key = String(skuItem.skuValue || "");
        if (!key) return;
        const bucket = buckets.get(key);
        if (bucket) bucket.push(skuItem);
        else buckets.set(key, [skuItem]);
    });

    const cursor = new Map<string, number>();
    const consumeFromBucket = (key: string) => {
        const bucket = buckets.get(key);
        if (!bucket || bucket.length === 0) return undefined;
        const pos = cursor.get(key) ?? 0;
        cursor.set(key, pos + 1);
        return bucket[Math.min(pos, bucket.length - 1)];
    };

    const map = new Map<string, any>();
    const attrIdsBySkuId = new Map<number, number[]>();

    const walk = (groupIndex: number, selectedAttrs: any[], tokens: string[]) => {
        if (groupIndex >= specificationList.value.length) {
            if (tokens.length === 0) return;
            const sku = consumeFromBucket(tokens.join("|"));
            if (!sku) return;
            const attrIds = selectedAttrs.map((attr) => Number(attr.attributesId));
            map.set(attrIds.join("|"), sku);
            attrIdsBySkuId.set(Number(sku.skuId), attrIds);
            return;
        }
        const group = specificationList.value[groupIndex];
        const list = Array.isArray(group?.attrList) ? group.attrList : [];
        if (list.length === 0) {
            walk(groupIndex + 1, selectedAttrs, tokens);
            return;
        }
        list.forEach((attr: any) => {
            walk(groupIndex + 1, [...selectedAttrs, attr], [...tokens, `${attr.attrName}:${attr.attrValue}`]);
        });
    };

    if (specificationList.value.length > 0) walk(0, [], []);
    return { map, attrIdsBySkuId };
});

// 优先按 attributesId 组合定位 SKU，回退到 skuValue 字符串匹配
const findSkuByCheckedAttrIds = (selectedAttrIds: (number | null)[], selectedValues?: any[]) => {
    const validAttrIds = (selectedAttrIds || []).filter((id): id is number => id != null && Number(id) > 0);
    if (validAttrIds.length > 0 && validAttrIds.length === specificationList.value.length) {
        const matched = skuSelectionMaps.value.map.get(validAttrIds.join("|"));
        if (matched) return matched;
    }
    const values = selectedValues || checkedValue.value;
    return skuList.value.find((it: any) => it.skuValue == values.join("|"));
};

// 根据 SKU 反查各规格组应选中的 attributesId
const getCheckedAttrIdsBySku = (skuItem?: any) => {
    if (!skuItem?.skuId) return specificationList.value.map(() => null);
    const mappedIds = skuSelectionMaps.value.attrIdsBySkuId.get(Number(skuItem.skuId));
    if (mappedIds && mappedIds.length === specificationList.value.length) return mappedIds;
    const tokens = String(skuItem.skuValue || "").split("|");
    return specificationList.value.map((group: any, index: number) => {
        const token = tokens[index];
        const attr = (group.attrList || []).find((it: any) => `${it.attrName}:${it.attrValue}` === token);
        return attr?.attributesId != null ? Number(attr.attributesId) : null;
    });
};

const updateValue = () => {
    specificationList.value.forEach((item: any, index: number) => {
        const checkedAttrId = checkedAttrIds.value[index];
        item.attrList.forEach((value: any) => {
            const attrNameValue = `${value.attrName}:${value.attrValue}`;
            const attrId = value.attributesId != null ? Number(value.attributesId) : null;
            // 优先按 attributesId 判断选中，避免同名规格值同时高亮
            value.checked = attrId != null && checkedAttrId != null ? checkedAttrId === attrId : checkedValue.value[index] === attrNameValue;

            const trialAttrIds = [...checkedAttrIds.value];
            const trialValues = [...checkedValue.value];
            trialAttrIds[index] = attrId;
            trialValues[index] = attrNameValue;
            const result = findSkuByCheckedAttrIds(trialAttrIds, trialValues);
            value.skuStock = result ? result.skuStock : 0;
        });
    });

    const selectedItem: any = findSkuByCheckedAttrIds(checkedAttrIds.value, checkedValue.value);
    if (selectedItem) {
        currentSku.value = selectedItem;
    }
};

const handleChecked = (index: number, subIdx: number, disabled: boolean) => {
    if (disabled) return;
    const checkedItem = specificationList.value[index].attrList[subIdx];
    if (index === 0 && checkedItem?.attrPic) {
        productImage.value = checkedItem.attrPic;
    }
    checkedValue.value[index] = checkedItem.attrName + ":" + checkedItem.attrValue;
    checkedAttrIds.value[index] = checkedItem.attributesId != null ? Number(checkedItem.attributesId) : null;
    updateValue();
    getProductAvailabilityData();
};

const handleExtraChecked = (groupIndex: number, optionIndex: number) => {
    const group = extraList.value[groupIndex];
    const option = group?.attrList?.[optionIndex];
    if (!group || !option) return;

    const nextId = option?.attributesId !== undefined && option?.attributesId !== null ? Number(option.attributesId) : undefined;
    const currentId = checkedExtraIds.value[groupIndex];
    const willSelect = nextId !== undefined && currentId !== nextId;

    checkedExtraIds.value[groupIndex] = willSelect ? nextId : undefined;
    (group.attrList || []).forEach((opt: any) => {
        const oid = opt?.attributesId !== undefined && opt?.attributesId !== null ? Number(opt.attributesId) : undefined;
        opt.checked = willSelect && oid !== undefined && oid === nextId;
    });

    getProductAvailabilityData();
};

const formatExtraPrice = (val: any) => {
    const n = Number(val);
    if (!Number.isFinite(n) || n === 0) return "";
    return priceFormat(n, true);
};

const handleConfirm = async () => {
    confirming.value = true;
    try {
        // 有规格但没选到 sku 的情况，提示用户
        if (canSelectSku.value && !currentSku.value?.skuId) {
            message.warning(t('请选择商品规格'));
            return;
        }
        const quantity = Math.max(1, Number(productNumber.value || 1));
        const rawShopId =
            productData.value?.item?.shopId ?? productData.value?.item?.shop_id ?? productData.value?.item?.storeId ?? productData.value?.item?.store_id;
        const shopId = rawShopId === undefined || rawShopId === null ? undefined : Number(rawShopId);
        emit("confirm", {
            skuId: currentSku.value?.skuId,
            skuValue: currentSku.value?.skuValue,
            quantity,
            price: price.value,
            stock: stock.value,
            shopId: Number.isNaN(shopId as any) ? undefined : shopId,
            extraAttrIds: extraAttrIds.value || undefined
        });
        innerOpen.value = false;
        emit("update:open", false);
    } finally {
        confirming.value = false;
    }
};

const handleCancel = () => {
    innerOpen.value = false;
    emit("update:open", false);
};

const reset = () => {
    price.value = 0;
    productData.value = {};
    productImage.value = "";
    specificationList.value = [];
    extraList.value = [];
    checkedExtraIds.value = [];
    skuList.value = [];
    checkedValue.value = [];
    checkedAttrIds.value = [];
    currentSku.value = {};
    productNumber.value = 1;
    stock.value = 0;
};
</script>

<style lang="less">
.proxy-product-pop {
    .ant-modal {
        top: 14vh;
    }
    .ant-modal-content {
        border-radius: 8px;
    }
    .ant-modal-header {
        border-radius: 8px 8px 0 0;
    }
    .ant-modal-title {
        font-weight: 500;
    }
}
</style>

<style lang="less" scoped>
.loading-placeholder {
    height: 300px;
}

.info-top {
    column-gap: 15px;
    .product-img {
        width: 100px;
        height: 100px;
        background-color: #f5f5f5;
        border-radius: 8px;
        overflow: hidden;
        img {
            width: 100%;
            height: 100%;
            object-fit: contain;
        }
    }
    .info-top-right {
        flex: 1;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        .product-name {
            font-size: 18px;
            font-weight: 500;
        }
        .productSn {
            font-size: 16px;
            color: #999999;
        }
        .product-price {
            display: flex;
            gap: 10px;
            align-items: baseline;
            .price {
                color: var(--tig-primary);
                font-weight: 600;
                font-size: 18px;
            }
            .stock {
                color: #909399;
                font-size: 12px;
            }
        }
    }
}

.sku-box {
    padding-top: 20px;
    .sku-items {
        gap: 10px;
        padding-bottom: 20px;
        .items-label {
            font-weight: 500;
        }
        .items {
            display: flex;
            flex-wrap: wrap;
            gap: 12px;
            .item {
                display: flex;
                align-items: center;
                padding: 6px 12px;
                background-color: #f5f5f5;
                border-radius: 8px;
                border: 1px solid #f5f5f5;
                box-sizing: border-box;
                position: relative;
                cursor: pointer;
                transition: all 0.2s;
                &.padding-left {
                    padding-left: 45px;
                }
                &:hover {
                    border-color: var(--tig-primary);
                    background-color: var(--el-color-primary-light-9);
                    color: var(--tig-primary);
                }
                &.checked {
                    border-color: var(--tig-primary);
                    background-color: var(--el-color-primary-light-9);
                    color: var(--tig-primary);
                }
                &.disabled {
                    cursor: not-allowed;
                    opacity: 0.6;
                    &:hover:not(.checked) {
                        border-color: #f5f5f5;
                        background-color: #f5f5f5;
                        color: inherit;
                    }
                }
                .item-img {
                    height: 85%;
                    position: absolute;
                    left: 1px;
                    top: 50%;
                    transform: translateY(-50%);
                    img {
                        width: 100%;
                        height: 100%;
                        object-fit: cover;
                        border-radius: 8px;
                    }
                }
            }
            .item-extra-price {
                margin-left: 6px;
                font-weight: 500;
                white-space: nowrap;
            }
        }
    }
}

.placeholder {
    height: 80px;
}

.amount {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px;
}

.submit {
    padding-top: 18px;
    .btn {
        width: 100%;
        height: 40px;
        border-radius: 8px;
    }
}
</style>
