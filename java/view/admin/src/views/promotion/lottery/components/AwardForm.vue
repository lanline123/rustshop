<template>
    <div class="award-form">
        <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
            <el-form-item :label="$t('奖品类型')" prop="prizeType">
                <RadioType v-model="prizeTypeKey" :radioList="awardTypeList" width="130px" />
            </el-form-item>

            <!-- 公共：奖品名称 -->
            <el-form-item :label="$t('奖品名称')" prop="prizeName">
                <BusinessData v-model:modelValue="formData.prizeName" :placeholder="$t('请输入奖品名称')" :dataType="12"></BusinessData>
            </el-form-item>

            <!-- 优惠券类型：选择优惠券 -->
            <el-form-item v-if="formData.prizeType === 3" :label="$t('优惠券')" prop="prizeValue">
                <div class="coupon-section">
                    <div class="coupon-add-row">
                        <DialogForm
                            isDrawer
                            @okCallback="onCouponSelected"
                            :title="$t('选择优惠券')"
                            width="700px"
                            path="promotion/lottery/components/CouponSelectList"
                            :params="{ selectedCouponId: Number(formData.prizeValue) || 0 }"
                        >
                            <el-button type="primary" link :disabled="!!selectedCoupon">{{ $t('+ 添加优惠券') }}</el-button>
                        </DialogForm>
                    </div>
                    <el-table v-if="selectedCoupon" :data="[selectedCoupon]" border style="margin-top: 10px">
                        <el-table-column :label="$t('优惠券名称')" prop="couponName" />
                        <el-table-column :label="$t('发放类型')" width="100">
                            <template #default="{ row }">
                                {{ row.couponType == 1 ? $t('满减券') : $t('折扣券') }}
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('优惠券金额(折扣)')" width="140">
                            <template #default="{ row }">
                                <span v-if="row.couponType == 1">{{ row.couponMoney || "0" }}</span>
                                <span v-else>{{ $t('{couponDiscount}折', { couponDiscount: row.couponDiscount || "0" }) }}</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('操作')" width="120">
                            <template #default>
                                <DialogForm
                                    isDrawer
                                    @okCallback="onCouponSelected"
                                    :title="$t('选择优惠券')"
                                    width="700px"
                                    path="promotion/lottery/components/CouponSelectList"
                                    :params="{ selectedCouponId: Number(formData.prizeValue) || 0 }"
                                >
                                    <el-button type="primary" link>{{ $t('更换优惠券') }}</el-button>
                                </DialogForm>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form-item>

            <!-- 商品类型：选择商品 -->
            <el-form-item v-if="formData.prizeType === 2" :label="$t('活动商品')" prop="prizeValue">
                <div class="product-section">
                    <div class="product-add-row">
                        <DialogForm
                            isDrawer
                            @okCallback="onProductSelected"
                            :title="$t('选择商品')"
                            width="1000px"
                            path="promotion/giftCard/src/SelectProduct"
                            :params="{
                                selectedIds: selectedProduct ? [selectedProduct] : [],
                                isMultiple: false,
                                productType: [1, 7, 8],
                                isSaleMode: 0
                            }"
                        >
                            <el-button type="primary" link :disabled="!!selectedProduct">{{ $t('+ 添加商品') }}</el-button>
                        </DialogForm>
                    </div>
                    <el-table v-if="selectedProduct" :data="[selectedProduct]" border style="margin-top: 10px">
                        <el-table-column :label="$t('商品名称')" min-width="200">
                            <template #default="{ row }">
                                <div class="flex">
                                    <div v-if="row.picUrl || row.picThumb" class="span-pic">
                                        <img :src="imageFormat(row.picUrl || row.picThumb)" height="50" width="50" alt="" style="object-fit: cover" />
                                    </div>
                                    <div class="span-product-con">
                                        <div class="span-product-name">{{ row.productName }}</div>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('商品信息')" width="140">
                            <template #default="{ row }">
                                <div>
                                    <div>
                                        <span class="span-tit">{{ $t('编码：') }}</span><span>{{ row.productSn || "--" }}</span>
                                    </div>
                                    <div>
                                        <span class="span-tit">{{ $t('售价：') }}</span><span>{{ row.productPrice || 0.0 }}</span>
                                    </div>
                                </div>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('规格')" width="100">
                            <template #default="{ row }">
                                <div v-if="row.skuText">{{ row.skuText }}</div>
                                <span v-else>--</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('库存')" width="80">
                            <template #default="{ row }">
                                <span v-if="row.skuStock != null || row.productStock != null">
                                    {{ row.skuStock ?? row.productStock }}
                                </span>
                                <span v-else>--</span>
                            </template>
                        </el-table-column>
                        <el-table-column :label="$t('操作')" width="120">
                            <template #default>
                                <DialogForm
                                    isDrawer
                                    @okCallback="onProductSelected"
                                    :title="$t('选择商品')"
                                    width="1000px"
                                    path="promotion/giftCard/src/SelectProduct"
                                    :params="{
                                        selectedIds: selectedProduct ? [selectedProduct] : [],
                                        isMultiple: false,
                                        productType: [1, 7, 8],
                                        isSaleMode: 0
                                    }"
                                >
                                    <el-button type="primary" link>{{ $t('更换商品') }}</el-button>
                                </DialogForm>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form-item>

            <!-- 积分类型：中奖积分 -->
            <el-form-item v-if="formData.prizeType === 1" :label="$t('中奖积分')" prop="prizeValue">
                <TigInput v-model="formData.prizeValue" classType="tig-form-input" type="integer" :min="1" :placeholder="$t('请输入中奖积分')" width="200px" />
                <span class="ml10">{{ $t('积分') }}</span>
            </el-form-item>

            <!-- 公共：发放总量 -->
            <el-form-item :label="$t('发放总量')" prop="prizeStock">
                <TigInput
                    v-model="formData.prizeStock"
                    classType="tig-form-input"
                    type="integer"
                    :min="1"
                    :max="maxPrizeStock"
                    :placeholder="$t('请输入奖品库存')"
                    width="200px"
                />
                <span class="ml10">{{ $t('份') }}</span>
            </el-form-item>

            <!-- 公共：图片 -->
            <el-form-item :label="$t('图片')" prop="prizeImage">
                <div class="image-upload-section">
                    <div style="display: flex; align-items: flex-end">
                        <FormAddGallery v-model:photo="prizeImagePhoto" isLocal pageType="overseas" :data-type="13"></FormAddGallery>
                        <el-button link type="primary" :disabled="!canRestoreDefaultImage" @click="restoreDefaultImage" style="margin-left: 10px">
                            {{ $t('恢复默认图') }}
                        </el-button>
                    </div>
                    <div class="tip">{{ $t('建议尺寸100*100px，支持jpg、png、jpeg格式') }}</div>
                </div>
            </el-form-item>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from "vue";
import { FormAddGallery } from "@/components/gallery";
import { RadioType } from "@/components/radio";
import { DialogForm } from "@/components/dialog";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import type { FormInstance, FormRules } from "element-plus";
import { PrizeItem } from "@/types/promotion/luckyWheel.d";
import { imageFormat } from "@/utils/format";
import prizeTypePointsBg from "@/assets/lottery/points.png";
import prizeTypeProductBg from "@/assets/lottery/product.png";
import prizeTypeCouponBg from "@/assets/lottery/coupon.png";
import { getCoupon } from "@/api/promotion/coupon";
import { getProduct } from "@/api/product/product";

// 奖品类型列表
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const awardTypeList = [
    { key: 1, title: t('积分'), bgImage: prizeTypePointsBg },
    { key: 2, title: t('商品'), bgImage: prizeTypeProductBg },
    { key: 3, title: t('优惠券'), bgImage: prizeTypeCouponBg }
];

// Props 接收参数
const props = defineProps({
    awardData: {
        type: Object as () => PrizeItem | null,
        default: null
    },
    editIndex: {
        type: Number,
        default: -1
    },
    isDialog: {
        type: Boolean,
        default: false
    },
    visible: {
        type: Boolean,
        default: false
    }
});

// Emits 事件
const emit = defineEmits(["submitCallback", "callback", "close"]);

// 加载状态
const confirmLoading = defineModel("confirmLoading", {
    type: Boolean,
    default: false
});

const formRef = ref<FormInstance>();

// 奖品类型 key（用于 RadioType 组件）
const prizeTypeKey = computed({
    get: () => formData.value.prizeType || 2,
    set: (val: number) => {
        formData.value.prizeType = val || 2;
        // 切换类型时清空各类型特有的数据
        // 清空奖品值
        formData.value.prizeValue = "";
        selectedCoupon.value = null;
        selectedProduct.value = null;
        // 清空优惠券扩展字段
        formData.value.couponType = undefined;
        formData.value.couponMoney = undefined;
        formData.value.couponDiscount = undefined;
        // 清空商品扩展字段
        formData.value.productName = undefined;
        formData.value.productSn = undefined;
        formData.value.productPrice = undefined;
        formData.value.picThumb = undefined;
        formData.value.skuId = undefined;
        formData.value.skuText = undefined;

        // 切换类型时，默认图片也跟着切换
        // 商品类型：不提供默认图（由选择商品后自动带出商品主图）
        if (Number(formData.value.prizeType) === 2) {
            formData.value.prizeImage = "";
        } else {
            restoreDefaultImage();
        }
    }
});

const formData = ref<Partial<PrizeItem>>({
    prizeType: 2,
    prizeName: "",
    prizeImage: "",
    prizeStock: 1,
    prizeStockUsed: 0,
    prizeValue: ""
});

// 回显：后端返回字符串（相对路径/绝对 URL），显示时统一走 imageFormat
const prizeImagePhoto = computed<string>({
    get: () => {
        const raw = (formData.value.prizeImage ?? "") as any;
        return imageFormat(String(raw || ""));
    },
    set: (val: string) => {
        formData.value.prizeImage = val as any;
    }
});

// 已选中的优惠券
const selectedCoupon = ref<any>(null);

// 已选中的商品
const selectedProduct = ref<any>(null);

const isProductPrize = computed(() => Number(formData.value.prizeType) === 2);
const canRestoreDefaultImage = computed(() => {
    const prizeType = Number(formData.value.prizeType) || 2;
    if (prizeType === 1 || prizeType === 3) return true;
    // 商品类型：只有已选择商品（有主图）时，才允许“恢复默认图”
    return Boolean(formData.value.picThumb);
});

// 商品类型下：发放总量上限 = 当前选择规格对应库存
// - 单规格：productStock
// - 多规格：skuStock
// 若 productStock 尚未设置（接口未返回），返回 undefined 以避免 watch 误纠正
// 若接口返回库存为 0，则正常返回 0 做限制
const maxPrizeStock = computed(() => {
    if (!isProductPrize.value) return undefined as any;
    const raw = (formData.value as any).productStock;
    // 未设置时返回 undefined，让 watch 跳过纠正逻辑
    if (raw === "" || raw === null || raw === undefined) return undefined as any;
    const max = Number(raw);
    if (!Number.isFinite(max) || max < 0) return undefined as any;
    return max;
});

const rules: FormRules = {
    prizeType: [{ required: true, message: t('请选择奖品类型'), trigger: "change" }],
    prizeName: [{ required: true, message: t('请输入奖品名称'), trigger: "blur" }],
    prizeStock: [
        {
            required: true,
            validator: (rule, value, callback) => {
                const n = Number(value);
                if (!n || n <= 0) {
                    callback(new Error(t('请输入奖品库存')));
                    return;
                }
                callback();
            },
            trigger: ["blur", "change"]
        }
    ],
    prizeImage: [
        {
            required: true,
            validator: (rule, value, callback) => {
                const img = formData.value.prizeImage as any;
                const hasImage =
                    (typeof img === "string" && img.trim().length > 0) ||
                    (Array.isArray(img) && img.length > 0) ||
                    (img && typeof img === "object" && (img.url || img.picUrl));

                if (!hasImage) {
                    callback(new Error(t('请上传图片')));
                    return;
                }
                callback();
            },
            trigger: ["change", "blur"]
        }
    ],
    prizeValue: [
        {
            required: true,
            validator: (rule, value, callback) => {
                const v = (value ?? "").toString().trim();
                if (formData.value.prizeType === 3 && !v) {
                    callback(new Error(t('请选择优惠券')));
                    return;
                }
                if (formData.value.prizeType === 2 && !v) {
                    callback(new Error(t('请选择商品')));
                    return;
                }
                if (formData.value.prizeType === 1) {
                    const points = Number(v);
                    if (!points || points <= 0) {
                        callback(new Error(t('请输入中奖积分，且必须大于0')));
                        return;
                    }
                }
                callback();
            },
            trigger: "change"
        }
    ]
};

watch(
    () => formData.value.prizeImage,
    () => {
        // 选图/删除图片后及时刷新“图片必填”提示
        formRef.value?.validateField?.("prizeImage").catch(() => void 0);
    }
);

// 商品类型：若发放总量大于库存，自动纠正为库存最大值（避免提交时报错）
watch(
    () => [isProductPrize.value, maxPrizeStock.value, formData.value.prizeStock] as const,
    () => {
        if (!isProductPrize.value) return;
        const max = Number(maxPrizeStock.value);
        if (!Number.isFinite(max) || max < 0) return;
        const current = Number(formData.value.prizeStock);
        if (current > max) {
            formData.value.prizeStock = max as any;
            // 纠正后清理一下该字段的校验提示（若有）
            formRef.value?.clearValidate?.(["prizeStock"]);
        }
    },
    { immediate: true }
);

// 优惠券选择回调
const onCouponSelected = (coupon: any) => {
    formData.value.prizeValue = String(coupon.couponId);
    if (!formData.value.prizeName) {
        formData.value.prizeName = coupon.couponName || "";
    }
    selectedCoupon.value = coupon;
    // 保存扩展字段用于回填展示
    formData.value.couponType = coupon.couponType;
    formData.value.couponMoney = coupon.couponMoney;
    formData.value.couponDiscount = coupon.couponDiscount;
    // 选择完成后清理“请选择优惠券”提示
    nextTick(() => formRef.value?.clearValidate?.(["prizeValue"]));
};

// 从 prizeValue 解析商品ID
const getProductIdFromPrizeValue = () => {
    const val = formData.value.prizeValue;
    if (!val) return 0;
    try {
        const parsed = JSON.parse(val as string);
        return parsed.productId || 0;
    } catch {
        return Number(val) || 0;
    }
};

// 从 prizeValue 解析 SKU ID
const getSkuIdFromPrizeValue = () => {
    const val = formData.value.prizeValue;
    if (!val) return 0;
    try {
        const parsed = JSON.parse(val as string);
        return parsed.skuId || 0;
    } catch {
        return 0;
    }
};

// 商品选择回调
const buildSkuTextFromSkuData = (skuData: string) => {
    if (!skuData || !skuData.length) return "";
    try {
        const skuInfo = JSON.parse(skuData);
        return skuInfo
            .map((item: any) => {
                return item.name + ": " + item.value;
            })
            .join("; ");
    } catch {
        return "";
    }
};

const onProductSelected = (rows: any) => {
    const product = Array.isArray(rows) ? rows[0] : rows;
    console.log(product, 9999);
    if (!product) return;

    // 单选选择器返回的是数组，这里取第一项；prizeValue 统一用 JSON 存储
    formData.value.prizeValue = JSON.stringify({ productId: product.productId, skuId: product.skuId });

    if (!formData.value.prizeName) {
        formData.value.prizeName = product.productName || "";
    }

    const skuText = buildSkuTextFromSkuData(product.skuData);
    const showPrice = Number(product.skuId) > 0 ? (product.skuPrice ?? product.productPrice) : product.productPrice;

    // 单规格库存用 productStock；多规格库存用 skuStock
    const isMultiSku = Number(product?.skuId) > 0;
    const productStock = Number((product as any)?.productStock ?? 0);
    const skuStock = Number((product as any)?.skuStock ?? 0);
    const stockValue = isMultiSku ? skuStock || productStock : productStock || skuStock;

    selectedProduct.value = {
        ...product,
        picThumb: product.picUrl,
        skuText,
        productStock: isMultiSku ? undefined : stockValue,
        skuStock: isMultiSku ? stockValue : undefined
    };

    // 保存扩展字段用于回填展示（沿用当前表单字段命名）
    formData.value.productName = product.productName;
    formData.value.productSn = product.productSn;
    formData.value.productPrice = showPrice;
    formData.value.picThumb = product.picUrl;
    formData.value.skuId = product.skuId;
    formData.value.skuText = skuText;
    (formData.value as any).skuData = product.skuData;
    // 统一保存当前规格对应的库存值（便于后续需要时做限制）
    (formData.value as any).productStock = stockValue;

    // 商品类型：选中商品后，奖品图默认使用商品主图（强制同步）
    if (Number(formData.value.prizeType) === 2 && product?.picUrl) {
        formData.value.prizeImage = product.picUrl;
        formRef.value?.clearValidate?.(["prizeImage"]);
    }

    // 选择完成后清理“请选择商品”提示
    nextTick(() => {
        formRef.value?.clearValidate?.(["prizeValue"]);
    });
};
const pointsDefaultImage = "https://oss.tigshop.com/img/gallery/202512/1766467150OobgXTHXBiLfzHdeH4.jpeg";
const couponDefaultImage = "https://oss.tigshop.com/img/gallery/202512/1766467150ehGPho5AEbzAYEu0IU.jpeg";

// 恢复默认图
const restoreDefaultImage = () => {
    // 不同奖品类型恢复不同默认图：
    // - 积分：pointsDefault
    // - 优惠券：couponDefault
    // - 商品：不使用默认图；只有选中商品后才有图（使用 picThumb）
    const prizeType = Number(formData.value.prizeType) || 2;
    if (prizeType === 1) {
        formData.value.prizeImage = pointsDefaultImage as any;
    } else if (prizeType === 3) {
        formData.value.prizeImage = couponDefaultImage as any;
    } else if (prizeType === 2) {
        // 商品类型：恢复为已选商品的主图（若未选择商品则不处理）
        if (formData.value.picThumb) {
            formData.value.prizeImage = formData.value.picThumb as any;
        } else {
            return;
        }
    }
    // 只有在确实设置了图片时才清理校验提示，避免商品类型置空后把必填提示也“抹掉”
    if (formData.value.prizeImage) {
        formRef.value?.clearValidate?.(["prizeImage"]);
    }
};

// 获取优惠券详情
const fetchCouponDetail = async (couponId: number) => {
    try {
        const res = await getCoupon("detail", { id: couponId });
        if (res) {
            const couponData = res;
            selectedCoupon.value = {
                couponId: couponData.couponId,
                couponName: couponData.couponName,
                couponType: couponData.couponType,
                couponMoney: couponData.couponMoney,
                couponDiscount: couponData.couponDiscount
            };
            // 更新扩展字段
            formData.value.couponType = couponData.couponType;
            formData.value.couponMoney = couponData.couponMoney;
            formData.value.couponDiscount = couponData.couponDiscount;
        }
    } catch (error) {
        console.error("获取优惠券详情失败:", error);
        // 失败时使用本地数据兜底
        selectedCoupon.value = {
            couponId: couponId,
            couponName: formData.value.prizeName,
            couponType: formData.value.couponType || 1,
            couponMoney: formData.value.couponMoney || 0,
            couponDiscount: formData.value.couponDiscount || 0
        };
    }
};

// 获取商品详情
const fetchProductDetail = async (productId: number, skuId: number) => {
    try {
        const res = await getProduct("detail", { id: productId });
        if (res) {
            const productData = res as any;
            // 查找对应的 SKU 信息
            let skuText = "";
            let skuPrice = productData.productPrice;
            let skuSn = productData.productSn;
            const productStock = Number(productData.productStock ?? 0);
            let skuStock = productStock;
            console.log(skuId, 9999);
            console.log(productData.productList, 9999);
            if (skuId && productData.productList && productData.productList.length > 0) {
                const targetSku = productData.productList.find((sku: any) => sku.skuId === skuId);
                if (targetSku) {
                    skuText = targetSku.skuValue || targetSku.attrValue || "";
                    skuPrice = targetSku.skuPrice || targetSku.price || skuPrice;
                    skuSn = targetSku.skuSn || targetSku.sn || skuSn;
                    skuStock = targetSku.skuStock ?? skuStock;
                }
            }
            const isMultiSku = Number(skuId) > 0;
            const stockValue = isMultiSku ? Number(skuStock || productStock) : Number(productStock || skuStock);
            selectedProduct.value = {
                productId: productData.productId,
                productName: productData.productName,
                productSn: skuSn,
                productPrice: skuPrice,
                picThumb: productData.picThumb,
                picUrl: productData.picThumb,
                skuId: skuId,
                skuText: skuText,
                productStock: isMultiSku ? undefined : stockValue,
                skuStock: isMultiSku ? stockValue : undefined
            };
            // 更新扩展字段
            formData.value.productName = productData.productName;
            formData.value.productSn = skuSn;
            formData.value.productPrice = skuPrice;
            formData.value.picThumb = productData.picThumb;
            formData.value.skuId = Number(skuId) || undefined;
            formData.value.skuText = skuText;
            (formData.value as any).productStock = stockValue;

            // 商品类型回显：若未设置奖品图，则默认使用商品主图
            if (Number(formData.value.prizeType) === 2 && !formData.value.prizeImage && productData.picThumb) {
                formData.value.prizeImage = productData.picThumb;
                formRef.value?.clearValidate?.(["prizeImage"]);
            }
        }
    } catch (error) {
        console.error("获取商品详情失败:", error);
        // 失败时使用本地数据兜底
        selectedProduct.value = {
            productId: productId,
            productName: formData.value.productName || formData.value.prizeName,
            productSn: formData.value.productSn || "",
            productPrice: formData.value.productPrice || 0,
            picThumb: formData.value.picThumb || formData.value.prizeImage,
            skuId: skuId || formData.value.skuId,
            skuText: formData.value.skuText || "",
            productStock: (formData.value as any).productStock
        };
    }
};

// 初始化表单数据
const initFormData = async () => {
    if (props.awardData) {
        const incoming = props.awardData as Partial<PrizeItem>;
        formData.value = {
            ...incoming,
            prizeType: incoming.prizeType ?? 2,
            prizeName: incoming.prizeName ?? "",
            prizeImage: incoming.prizeImage ?? "",
            prizeStock: incoming.prizeStock ?? 1,
            prizeStockUsed: incoming.prizeStockUsed ?? 0,
            prizeValue: (incoming.prizeValue ?? "").toString()
        };
        // 如果是优惠券类型且有关联ID，请求优惠券详情接口
        if (formData.value.prizeType === 3 && formData.value.prizeValue) {
            const couponId = Number(formData.value.prizeValue);
            if (couponId) {
                await fetchCouponDetail(couponId);
            }
        }
        // 如果是商品类型且有关联ID，请求商品详情接口
        if (formData.value.prizeType === 2 && formData.value.prizeValue) {
            const productId = getProductIdFromPrizeValue();
            const skuId = getSkuIdFromPrizeValue();
            if (productId) {
                await fetchProductDetail(productId, skuId);
            }
        }
    } else {
        formData.value = {
            prizeType: 2,
            prizeName: "",
            prizeImage: "",
            prizeStock: 1,
            prizeStockUsed: 0,
            prizeValue: ""
        };
        selectedCoupon.value = null;
        selectedProduct.value = null;
    }
};

// 监听visible变化
watch(
    () => props.visible,
    (val) => {
        if (val) {
            initFormData();
        }
    },
    { immediate: true }
);

onMounted(() => {
    initFormData();
});

// 表单提交方法（供DialogForm组件调用）
const onFormSubmit = async () => {
    if (!formRef.value) return;

    try {
        // 提交前兜底：商品类型下若发放总量大于库存，自动纠正为库存最大值
        if (isProductPrize.value) {
            const max = Number(maxPrizeStock.value);
            const current = Number(formData.value.prizeStock);
            if (Number.isFinite(max) && max >= 0 && current > max) {
                formData.value.prizeStock = max as any;
            }
        }
        await formRef.value.validate();
        confirmLoading.value = true;

        // 返回数据给父组件（tempIndex 由 Step2Awards 分配）
        emit("submitCallback", {
            award: { ...formData.value },
            editIndex: props.editIndex
        });
    } catch (error) {
        console.error("表单验证失败:", error);
    } finally {
        confirmLoading.value = false;
    }
};

// 暴露方法给父组件
defineExpose({
    onFormSubmit
});
</script>

<style scoped lang="less">
.award-form {
    padding: 20px 20px 0;

    .coupon-section,
    .product-section {
        width: 100%;

        .coupon-add-row,
        .product-add-row {
            margin-bottom: 10px;
        }
    }

    .image-upload-section {
        .tip {
            font-size: 12px;
            color: #999;
            margin-top: 8px;
        }
    }

    .span-pic {
        margin-right: 10px;
        flex-shrink: 0;

        img {
            border: 1px solid #eee;
        }
    }

    .span-product-con {
        flex: 1;

        .span-product-name {
            font-size: 14px;
            color: #333;
            line-height: 1.4;
            word-break: break-all;
        }
    }

    .span-tit {
        color: #999;
    }
}

:deep(.gallery-pic) {
    width: auto;
}

:deep(.radio-style) {
    .has-bg {
        height: 80px;
    }
    .title-row {
        height: 100%;
        .title {
            font-size: 16px;
        }
    }
}
</style>
