<!--
 * @file 商品编辑/添加页面
 * @description 商品信息编辑页面，支持以下功能：
 *   - 基本信息：商品名称、分类、品牌、主图、轮播图等
 *   - 销售信息：价格、规格（单规格/多规格）、库存、限购等
 *   - 物流及配送：运费模板、支持物流/自提/同城配送
 *   - 商品详情：富文本描述、详情图文等
 *   - 高级信息：关联商品、商品服务、SEO信息等
 *   - 商品类型支持：普通商品、虚拟商品、卡密商品、付费内容、预约商品
 *   - 预售支持：全款预售、定金预售
 * @module views/product/product/Info
-->
<template>
    <div class="product-container">
        <div class="anchor">
            <el-anchor :container="containerRef" :offset="10" direction="horizontal" @change="onChange" @click="handleClick">
                <div class="tab-row" ref="tabRowRef">
                    <el-anchor-link
                        v-for="(item, index) in tabList"
                        :key="item.id"
                        :class="{ active: partTab === '' && index === 0 ? true : partTab === '#' + item.id }"
                        :href="'#' + item.id"
                    >
                        <template #default>
                            <div :class="{ active: partTab === '' && index === 0 ? true : partTab === '#' + item.id }" class="item">
                                {{ item.title }}
                            </div>
                        </template>
                    </el-anchor-link>
                </div>
            </el-anchor>
        </div>
        <div ref="containerRef" class="form-container">
            <el-form v-if="!loading" ref="formRef" :model="formState" label-width="110px" name="form_in_modal">
                <div id="part1" class="form-warp">
                    <div class="title">
                        <p class="xian"></p>
                        {{ $t("基本信息") }}
                    </div>
                    <BasicInfo
                        :type="type"
                        :id="id"
                        :shopId="shopId"
                        :examine="examine"
                        :loading="loading"
                        :action="action"
                        :storeSettingInfo="storeSettingInfo"
                        v-model:form-state="formState"
                        :formRef="formRef"
                    >
                    </BasicInfo>
                </div>
                <div id="part2" class="form-warp">
                    <div class="title">
                        <p class="xian"></p>
                        {{ $t("销售信息") }}
                    </div>
                    <PriceInfo
                        v-if="type !== 'vendor'"
                        ref="priceInfoRef"
                        :id="id"
                        :shopId="shopId"
                        :act="act"
                        :examine="examine"
                        :attrTplList="attrTplList"
                        v-model:isLimit="isLimit"
                        v-model:attrTplId="formState.attributesTplId"
                        :loading="loading"
                        v-model:form-state="formState"
                        :storeSettingInfo="storeSettingInfo"
                        v-model:skuType="skuType"
                        v-model:productList="formState.productList"
                    ></PriceInfo>
                    <VendorPriceInfo
                        v-if="type == 'vendor'"
                        ref="priceInfoRef"
                        :id="id"
                        :shopId="shopId"
                        :act="act"
                        :examine="examine"
                        :attrTplList="attrTplList"
                        v-model:isLimit="isLimit"
                        :loading="loading"
                        v-model:form-state="formState"
                    ></VendorPriceInfo>
                </div>
                <div id="part3" class="form-warp">
                    <div class="title">
                        <p class="xian"></p>
                        {{ $t("物流及配送") }}
                    </div>
                    <LogisticsInfo
                        ref="logisticsInfoRef"
                        v-if="
                            type !== 'vendor' || (type == 'vendor' && formState.vendorProductId > 0 && formState.productType != 5 && formState.productType != 6)
                        "
                        :id="id"
                        :shopId="shopId"
                        :examine="examine"
                        :loading="loading"
                        :storeSettingInfo="storeSettingInfo"
                        v-model:form-state="formState"
                        :act="act"
                    >
                    </LogisticsInfo>
                    <!-- 预约类型对应组件 -->
                    <OrderInfo v-model:form-state="formState" v-if="formState.productType == 5 || formState.productType == 6" />
                </div>
                <div id="part4" class="form-warp">
                    <div class="title">
                        <p class="xian"></p>
                        {{ $t("商品详情") }}
                    </div>
                    <DescInfo
                        :type="type"
                        :id="id"
                        :shopId="shopId"
                        :examine="examine"
                        :loading="loading"
                        :storeSettingInfo="storeSettingInfo"
                        v-model:form-state="formState"
                    ></DescInfo>
                </div>
                <div id="part5" class="form-warp">
                    <div class="title">
                        <p class="xian"></p>
                        {{ $t("高级信息") }}
                    </div>
                    <AdvancedInfo :id="id" :shopId="shopId" :examine="examine" :loading="loading" v-model:form-state="formState"></AdvancedInfo>
                </div>
            </el-form>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef, nextTick, watch } from "vue";
import { BasicInfo, PriceInfo, LogisticsInfo, DescInfo, AdvancedInfo, VendorPriceInfo, OrderInfo } from "@/views/product/product/src/components";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { ProductFormState, ServiceList } from "@/types/product/product.d";
import { getProduct, getProductConfig, updateProduct, AuditAgainProduct } from "@/api/product/product";
import { getProductAttributesTplListByCategory } from "@/api/product/productAttributesTpl";
import { isPro, isStore } from "@/utils/version";
import { getShopType } from "@/utils/storage";
import { getEnabledSkus, isSkuEnabled } from "@/utils/productSkuStatus";

/**
 * 组件事件定义
 * @event submitCallback - 提交成功回调
 * @event update:confirmLoading - 确认按钮加载状态更新
 * @event close - 关闭页面/弹窗
 * @event closeConfirm - 关闭确认状态
 */
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close", "closeConfirm"]);

/**
 * 组件属性定义
 * @property {number} id - 商品ID，编辑时必传
 * @property {number} shopId - 店铺ID
 * @property {string} act - 操作类型：add/detail/copy/again
 * @property {string} type - 商品来源类型：空或'vendor'（供应商商品）
 * @property {number} examine - 审核状态
 * @property {Object} storeSettingInfo - 门店设置信息
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    shopId: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    type: {
        type: String,
        default: ""
    },
    examine: {
        type: Number,
        default: 0
    },
    storeSettingInfo: {
        type: Object,
        default: {}
    },
    isDialog: Boolean
});
/** Tab导航配置列表 */
const tabList = [
    {
        title: t("基本信息"),
        id: "part1"
    },
    {
        title: t("销售信息"),
        id: "part2"
    },
    {
        title: t("物流及配送"),
        id: "part3"
    },
    {
        title: t("商品详情"),
        id: "part4"
    },
    {
        title: t("高级信息"),
        id: "part5"
    }
];

/** 商品来源类型：空或'vendor' */
const type = ref<string>(props.type);

/** 表单容器DOM引用，用于锚点滚动 */
const containerRef = ref<HTMLElement | null>(null);

/** Tab行DOM引用，用于移动端滚动 */
const tabRowRef = ref<HTMLElement | null>(null);

/** 当前激活的锚点Tab */
const partTab = ref("");

/**
 * 锚点点击处理，阻止默认行为
 * @param {MouseEvent} e - 鼠标事件
 */
const handleClick = (e: MouseEvent) => {
    e.preventDefault();
};

/**
 * 锚点切换回调
 * @param {string} e - 新的锚点值
 */
const onChange = (e: string) => {
    partTab.value = e;
    // 移动端下，将激活的tab滚动到可视区域
    nextTick(() => {
        if (tabRowRef.value && window.innerWidth <= 768) {
            // 找到当前激活的 tab 元素（el-anchor-link 渲染出的外层节点通常带有 el-anchor__item 类，或者直接找 active 的元素）
            const activeEl = tabRowRef.value.querySelector(".active") as HTMLElement;
            if (activeEl) {
                // 向上寻找包含 offsetLeft 的实际包裹节点，通常是 .el-anchor__item
                const linkEl = (activeEl.closest(".el-anchor__item") || activeEl) as HTMLElement;
                if (linkEl) {
                    const containerWidth = tabRowRef.value.clientWidth;
                    const scrollLeft = tabRowRef.value.scrollLeft;
                    const elLeft = linkEl.offsetLeft;
                    const elWidth = linkEl.offsetWidth;

                    // 无论是否在可视区域外，都将其平滑滚动到容器中间位置，体验更好
                    tabRowRef.value.scrollTo({
                        left: elLeft - containerWidth / 2 + elWidth / 2,
                        behavior: "smooth"
                    });
                }
            }
        }
    });
};

/** 加载状态 */
const loading = ref<boolean>(true);

/** 路由查询参数 */
const query = useRouter().currentRoute.value.query;

/** 操作类型：add/detail/copy/again */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
console.log(action.value, "action.valueaction.valueaction.value");

/** 商品ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));

/** API操作类型：create/copy/update */
const operation = action.value === "add" ? "create" : action.value === "copy" ? "copy" : "update";

/** 表单引用 */
const formRef = shallowRef();

/** 是否启用限购 */
const isLimit = ref(false);
/**
 * 商品表单状态
 * @type {ProductFormState}
 * @property {number} snType - 货号生成方式：1自动生成，2手动输入
 * @property {number} productType - 商品类型：1普通，2虚拟，3卡密，4付费内容，5预约服务，6预约商品，7同城配送
 * @property {number} saleMode - 销售模式：0现货，1全款预售，2定金预售
 * @property {Array} productList - SKU规格列表
 * @property {Object} attrList - 属性列表：normal普通属性，spe规格属性，extra额外属性
 */
const formState = ref<ProductFormState>({
    snType: 1,
    productType: 1,
    // freeShipping 仅兼容详情接口中的历史字段，保存时统一由后端判断，不由前端提交。
    freeShipping: undefined,
    shopCategoryId: 0,
    productStatus: 1,
    giveIntegral: -1,
    rankIntegral: -1,
    integral: 0,
    isSupportCod: 0,
    productVideoInfo: [],
    productDescArr: [],
    productRelated: [],
    serviceList: [],
    eCardList: [],
    productList: [],
    limitNumber: "",
    virtualSample: "",
    paidContent: [],
    productArticleList: [],
    noShipping: 0,
    fixedShippingType: 1,
    isShopPickup: 1,
    isLogistics: 1,
    skuType: 1,
    timedOffShelf: 0,
    autoOfflineTime: "",
    autoOnlineTime: "",
    attrList: {
        normal: [],
        spe: [],
        extra: []
    },
    vendorProductId: 0,
    reservationTplId: null as any,
    reservationTimes: [],
    isShopDelivery: 1,
    saleMode: 0, // 新增：默认销售模式为0（现货销售）
    attributesTplId: null,
    serviceFeeList: [],
    deliveryFeeList: [],
    deliveryMode: 1, // 交付模式：1-单次交付，2-多次交付（周期购/次卡）
    cycleConfig: {
        // 交付模式：1-按计划配送，2-按需兑换（次卡）
        deliveryType: 1,
        settingMode: 1,
        cycleSpecs: [],
        timesPerPeriod: 1,
        cycleType: 1,
        specs: [],
        specDetails: [],
        skuSpecDetails: {}
    }
});
watch(formState.value, (val) => {
    if (val.productType != 1 && val.productType != 7) {
        formState.value.noShipping = 1;
        // 只有普通商品才能设置预售，其他商品类型清空预售配置
        if (val.saleMode !== 0 || val.presaleConfig) {
            formState.value.saleMode = 0;
            formState.value.presaleConfig = undefined;
        }
    }
});
/** 商品类型选项列表 */
const radioList = ref<any[]>([]);

/**
 * 组件挂载时初始化
 * @description 加载商品配置、初始化商品类型选项、获取商品详情（编辑模式）
 */
onMounted(async () => {
    await fetchProductConfig(false);
    if (isPro()) {
        radioList.value = [
            { key: 1, title: "普通商品", desc: isStore() ? "物流/自提/同城配送" : "物流配送" },
            {
                key: 3,
                title: t("卡密商品"),
                tips: t(
                    "可以在菜单中【营销>电子卡券组】创建卡券组，然后在【查看卡列表】添加单个卡密或通过下载【批量导入模板文件】填好卡密后在通过批量导入电子卡券按钮上传该文件批量导入。"
                ),
                desc: t("无需物流")
            },
            {
                key: 2,
                title: t("虚拟商品"),
                tips: t("网盘链接、视频链接，在添加商品添加虚拟商品。用户购买后将在订单详情直接展示。"),
                desc: t("无需物流")
            },
            {
                key: 4,
                title: t("付费内容"),
                tips: t("软件授权、在线课程等。通过简单的操作，即可将这些商品添加到您的商店中，为客户提供更丰富的选择。立即体验，享受便捷的付费内容管理服务。"),
                desc: t("无需物流")
            }
        ];
    } else {
        radioList.value = [{ key: 1, title: t("普通商品") }];
    }
    if (action.value === "detail" || action.value === "copy" || action.value === "again") {
        // 获取详情数据
        await fetchProduct();
    } else {
        loading.value = false;
    }
    setTimeout(() => {
        emit("closeConfirm", true);
    }, 10000);
});

/** 属性模板列表：按商品类目加载，未选类目时为空 */
const attrTplList = ref<Object[]>([]);

/**
 * @description 加载当前商品类目下的属性模板
 * 属性模板已与商品类目绑定，商品配置接口不再返回全量模板
 */
const loadAttrTplList = async (categoryId?: number | string) => {
    if (!categoryId) {
        attrTplList.value = [];
        return;
    }
    try {
        const result = await getProductAttributesTplListByCategory(categoryId);
        attrTplList.value = result || [];
    } catch {
        // 属性模板为选填项，且接口受属性模板管理权限约束，失败时静默降级为无可选模板
        attrTplList.value = [];
    }
};

watch(
    () => formState.value.categoryId,
    (categoryId, oldCategoryId) => {
        // 切换类目后原模板可能不属于新类目，清空已选模板；详情回填（oldCategoryId 为空）时保留原值
        if (oldCategoryId && categoryId !== oldCategoryId) {
            formState.value.attributesTplId = null;
        }
        loadAttrTplList(categoryId);
    },
    { immediate: true }
);

/** SKU规格类型：1统一规格，2多规格 */
const skuType = ref(1);

/**
 * @description 获取商品详情数据
 * 处理规格类型判断、预售信息转换、服务列表回显等
 */
const fetchProduct = async () => {
    try {
        const result = await getProduct("detail", { id: id.value });
        if (result.productRelated == null) {
            result.productRelated = [];
        }
        if (result.brandId == 0) {
            result.brandId = "";
        }
        if (result.shippingTplId == 0) {
            result.shippingTplId = "";
        }
        if (result.productPrice == 0) {
            result.productPrice = "0.00";
        }
        if (result.limitNumber === 0) {
            result.limitNumber = "";
            isLimit.value = false;
        } else {
            isLimit.value = true;
        }
        if (result.vendorId) {
            type.value = "vendor";
        }
        // 判断规格类型：有productList为多规格，否则为统一规格
        if (result.productList?.length) {
            skuType.value = 2;
        } else {
            // 统一规格时，将商品价格信息回显到 productList
            result.productList = [
                {
                    skuPrice: String(result.productPrice || ""),
                    skuStock: Number(result.productStock) || 0,
                    marketPrice: result.marketPrice || "",
                    costPrice: result.costPrice || "",
                    skuSn: result.productSn || "",
                    skuTsn: result.productTsn || ""
                }
            ] as any;
            skuType.value = 1;
        }

        formState.value.serviceList = formState.value.serviceList.map((item: ServiceList) => {
            return {
                ...item,
                check: result.productServiceIds?.includes(item.productServiceId) ? 1 : 0
            };
        });

        // 处理预售信息：将 presaleInfo 转换为 presaleConfig
        if (result.presaleInfo) {
            // 详情返回的是 presaleInfo，需要转换为 presaleConfig 用于编辑
            const presaleInfo = result.presaleInfo;
            formState.value.presaleConfig = {
                saleMode: presaleInfo.saleMode,
                depositType: presaleInfo.depositType,
                depositRatio: presaleInfo.depositRatio,
                isRoundDown: presaleInfo.isRoundDown,
                depositFixedAmount: presaleInfo.depositFixedAmount,
                finalPaymentType: presaleInfo.finalPaymentType,
                finalPaymentRelativeDays: presaleInfo.finalPaymentRelativeDays,
                finalPaymentDurationDays: presaleInfo.finalPaymentDurationDays,
                finalPaymentDurationHours: presaleInfo.finalPaymentDurationHours,
                finalPaymentStartTime: presaleInfo.finalPaymentStartTime,
                finalPaymentEndTime: presaleInfo.finalPaymentEndTime,
                deliveryType: presaleInfo.deliveryType,
                deliveryRelativeDays: presaleInfo.deliveryRelativeDays,
                deliveryRelativeHours: presaleInfo.deliveryRelativeHours,
                deliveryDate: presaleInfo.deliveryDate,
                status: presaleInfo.status
            };
            // PriceInfo 中的 saleMode: 0-现货，1-全款预售，2-定金预售
            // presaleConfig.saleMode: 1-全款预售，2-定金预售（由 PresaleConfig 组件管理）
            // 编辑时，如果存在预售信息，使用 presaleInfo.saleMode 的值（1 或 2），让 PriceInfo 显示预售配置区域
            formState.value.saleMode = presaleInfo.saleMode; // 使用预售信息中的 saleMode（1 或 2），显示 PresaleConfig 组件
        } else {
            // 非预售商品，确保 saleMode 为 0（现货销售）
            formState.value.saleMode = result.saleMode ?? 0;
            formState.value.presaleConfig = undefined;
        }


        // 后端无 timedOffShelf；有待执行自动上下架任务时详情带回时间，据此回显勾选
        if (result.autoOnlineTime || result.autoOfflineTime) {
            result.timedOffShelf = 1;
        } else {
            result.timedOffShelf = 0;
        }

        await syncShippingTplListByProductShop(result);

        // 使用展开运算符重新赋值，确保触发响应式更新
        formState.value = { ...formState.value, ...result };
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

/**
 * @description 获取商品配置数据
 * 包含属性模板、服务列表、供应商列表、运费模板、电子卡券等
 */
const getConfigShopId = (shopId?: number) => shopId || props.shopId || Number(localStorage.getItem("shopId")) || 0;

const hasShippingTpl = (list: any[] | undefined, shippingTplId: any) => {
    const id = Number(shippingTplId);
    return !!id && (list || []).some((item: any) => Number(item?.shippingTplId) === id);
};

const syncShippingTplListByProductShop = async (product: ProductFormState) => {
    const productShopId = Number(product.shopId || 0);
    if (!productShopId || productShopId === getConfigShopId()) return;
    if (!product.shippingTplId || hasShippingTpl(formState.value.shippingTplList, product.shippingTplId)) return;

    const result = await getProductConfig({ shopId: productShopId });
    if (hasShippingTpl(result.shippingTplList, product.shippingTplId)) {
        product.shippingTplList = result.shippingTplList;
    }
};

const fetchProductConfig = async (isMsg: boolean = true, shopId?: number) => {
    try {
        const result = await getProductConfig({ shopId: getConfigShopId(shopId) });
        // 仅允许选择后台设置为默认显示的商品服务，隐藏服务不应继续被商品关联。
        formState.value.serviceList = result.serviceList?.filter((item: ServiceList) => Number(item.defaultOn ?? 0) === 1).map((item: ServiceList) => {
            return {
                ...item,
                check: result.item.productServiceIds?.includes(item.productServiceId) ? 1 : 0
            };
        });
        formState.value.suppliersList = result.suppliersList;
        formState.value.shippingTplList = result.shippingTplList;
        formState.value.eCardList = result.eCardList;
        formState.value.productList = [
            {
                skuPrice: "",
                skuStock: 0,
                marketPrice: "",
                costPrice: "",
                skuSn: "",
                skuTsn: ""
            }
        ];
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    }
    // 详情/复制/再审核模式下由 fetchProduct 结束后再解除 loading，避免子组件在数据回填前提前挂载读取到占位数据；
    // 新增模式由 onMounted 中的 else 分支解除 loading
};

/**
 * @description 验证SKU规格数据完整性
 * 多规格时验证规格属性值，所有规格都需验证库存
 * @param skus SKU列表
 * @returns 验证是否通过
 */
function validateSkus(skus: any) {
    const shouldValidateStock = ![5, 6].includes(Number(formState.value.productType));
    for (let i = 0; i < skus.length; i++) {
        const sku = skus[i];
        if (skuType.value === 2 && !isSkuEnabled(sku)) continue;
        // 统一规格时（skuType === 1），只验证库存等必填项，不验证规格属性
        if (skuType.value === 2) {
            // 多规格：验证规格属性值
            if (!sku.attrValues || sku.attrValues.length === 0 || sku.attrValues.some((value: string) => value === "")) {
                console.error("请填写完整的规格信息");
                return false;
            }
        }
        // 统一规格和多规格都需要验证库存
        if (shouldValidateStock && (sku.skuStock === null || sku.skuStock === undefined)) {
            console.error("请填写库存信息");
            return false;
        }
    }
    return true;
}

/** 销售信息组件引用 */
const priceInfoRef = ref<any>();
const logisticsInfoRef = ref<any>();

/**
 * 自定义表单验证，支持排除指定字段
 * @description 用于处理条件性必填字段（如定时上下架时间）
 * @param {string[]} excludeFields - 需要排除验证的字段名数组
 * @returns {Promise<boolean>} 验证是否通过
 */
const validateForm = async (excludeFields: string[] = []) => {
    return new Promise((resolve, reject) => {
        formRef.value.validate((valid: boolean, fields: any) => {
            if (valid) {
                resolve(true);
            } else {
                // 检查失败的字段是否都在排除列表中
                const failedFields = Object.keys(fields || {});
                const realFailedFields = failedFields.filter((field) => !excludeFields.includes(field));

                if (realFailedFields.length === 0) {
                    // 所有失败的字段都在排除列表中，视为验证通过
                    // 清除这些字段的错误状态
                    formRef.value.clearValidate(excludeFields);
                    resolve(true);
                } else {
                    reject(fields);
                }
            }
        });
    });
};

/**
 * 按当前运费方式推导 freeShipping，避免详情回填的历史包邮标记随运费模板一起提交。
 * 与后端 ProductServiceImpl#syncFreeShippingWithShippingType 语义一致：
 * 运费模板强制不包邮；固定运费为 0 才视为包邮。
 */
const syncFreeShippingWithShippingType = (data: ProductFormState) => {
    if (data.fixedShippingType === 2) {
        data.freeShipping = 0;
        return;
    }
    if (data.fixedShippingType === 1) {
        const fee = Number(data.fixedShippingFee);
        data.freeShipping = !Number.isFinite(fee) || fee === 0 ? 1 : 0;
    }
};

/**
 * @description 提交商品表单
 * 验证表单、处理规格数据、计算价格库存、调用保存接口
 */
const onSubmit = async () => {
    try {
        // 根据是否勾选定时上/下架，决定是否排除对应字段的校验
        const excludeFields = !formState.value.timedOffShelf ? ["autoOfflineTime", "autoOnlineTime"] : [];
        await validateForm(excludeFields);
        if (formState.value.productType == 4 && formState.value.paidContent.length == 0) {
            message.error(t("付费内容信息不能为空!"));
            await nextTick();
            scrollIntoError(".goods-info-edit-row");
            return;
        }
        if (formState.value.productType !== 3 && formState.value.productType !== 7 && formState.value.productType !== 8) {
            if (type.value !== "vendor" && !validateSkus(formState.value.productList)) {
                message.error(t("请将商品明细的信息填写完整!"));
                priceInfoRef.value.skuErrorText = t("请将商品明细的信息填写完整");
                await nextTick();
                scrollIntoError(".sku-error");
                return;
            } else {
                priceInfoRef.value.skuErrorText = "";
            }
            // 单独验证规格图片
            if (priceInfoRef.value.attrForm.validateSpecImages && !priceInfoRef.value.attrForm.validateSpecImages()) {
                await nextTick();
                // 滚动到规格区域
                const specSection = document.querySelector("#spec-image-section");
                if (specSection) {
                    specSection.scrollIntoView({ behavior: "smooth", block: "center" });
                }
                return;
            }
            await priceInfoRef.value.attrForm.validateProductSku();
        }

        try {
            emit("update:confirmLoading", true);
            // 提交时使用副本，避免接口报错后污染页面表单状态，导致规格明细被隐藏
            const submitData = JSON.parse(JSON.stringify(formState.value));
            // 商品编辑时固定关闭包邮，确保后端收到明确的 0 值。
            if (operation === "update") {
                submitData.freeShipping = 0;
            }
            // 同步独立 skuType ref 到提交数据，确保切换规格类型后传参正确
            submitData.skuType = skuType.value;
            if (submitData.attrList?.spe?.length > 0) {
                submitData.productStock = 0;
            }
            submitData.productServiceIds = submitData.serviceList.filter((item: any) => item.check === 1).map((item: any) => item.productServiceId);
            submitData.limitNumber = isLimit.value ? submitData.limitNumber : 0;
            // 周期购：可配送时间段从小到大排序再提交给后端
            // 清理不需要的字段（presaleInfo 是详情返回的，提交时不需要）
            if (submitData.presaleConfig && submitData.presaleConfig?.saleMode) {
                submitData.saleMode = submitData.presaleConfig?.saleMode;
            }
            delete submitData.presaleInfo;
            if (skuType.value === 1) {
                submitData.productPrice = submitData.productList?.[0]?.skuPrice;
                submitData.productStock = submitData.productList?.[0]?.skuStock;
                submitData.marketPrice = submitData.productList?.[0]?.marketPrice;
                submitData.costPrice = submitData.productList?.[0]?.costPrice;
                submitData.productSn = submitData.productList?.[0]?.skuSn;
                // 商品条码以基础信息 productTsn 为准，不用规格明细 skuTsn 覆盖
                delete submitData.productList;
            } else if (skuType.value === 2) {
                // 聚合价格和库存只统计启用 SKU，完整 productList 仍提交给接口保存状态。
                const enabledProductList = getEnabledSkus(submitData.productList || []);
                const prices =
                    enabledProductList
                        ?.filter((item: any) => item.skuPrice && !isNaN(Number(item.skuPrice)))
                        .map((item: any) => Number(item.skuPrice)) || [];
                submitData.productPrice = prices.length > 0 ? Math.min(...prices) : enabledProductList[0]?.skuPrice;
                submitData.productStock = enabledProductList.reduce((sum: number, item: any) => {
                    const stock = Number(item?.skuStock);
                    return sum + (Number.isFinite(stock) ? stock : 0);
                }, 0);
            }


            // 预约类商品（预约服务 / 预约商品）不涉及配送方式，统一清零，避免残留脏数据
            if ([5, 6].includes(Number(submitData.productType))) {
                submitData.isShopPickup = 0;
                submitData.isShopDelivery = 0;
                submitData.isLogistics = 0;
            }

            // 关闭定时上下架：两个时间都传 null，后端据此取消全部自动上下架任务
            if (!submitData.timedOffShelf) {
                submitData.autoOnlineTime = null;
                submitData.autoOfflineTime = null;
            } else if (submitData.productStatus) {
                submitData.autoOnlineTime = null;
            } else {
                submitData.autoOfflineTime = null;
            }
            delete submitData.timedOffShelf;

            syncFreeShippingWithShippingType(submitData);

            const result = await updateProduct(operation, submitData);
            if (props.act === "again") {
                await AuditAgainProduct({ id: id.value });
            }
            emit("submitCallback", result);
            message.success(result as any);
        } catch (error: any) {
            message.error(error.message);
        } finally {
            console.log("提交完成");
            emit("update:confirmLoading", false);
        }
    } catch (error) {
        await nextTick();
        scrollIntoError();
        console.error("表单验证失败:", error);
    }
};

/**
 * 滚动到表单错误项
 * @param {string} selector - CSS选择器，默认为错误表单项
 */
const scrollIntoError = (selector = ".el-form-item.is-error") => {
    const errorElement = document.querySelector(selector);
    if (errorElement) {
        errorElement.scrollIntoView({
            behavior: "instant",
            block: "center"
        });
        const inputElement = errorElement.querySelector("input, textarea, select") as HTMLElement | null;
        if (inputElement) {
            inputElement.focus();
        }
    }
};

/**
 * 表单提交入口
 * @description 供父组件调用的提交方法
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.product-container {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;

    :deep(.el-anchor__marker) {
        display: none !important;
        opacity: 0 !important;
        z-index: -10 !important;
    }

    .inner-item {
        :deep(.el-textarea) {
            width: 450px;
        }

        :deep(.el-select) {
            width: 450px;
        }
    }

    .anchor {
        padding-bottom: 10px;
        background-color: #fff;

        .tab-row {
            display: flex;
            width: 100%;
            height: 38px;
            box-sizing: border-box;

            :deep(.el-anchor__item) {
                height: 100%;
                flex: 1;
                background-color: var(--tig-item-active-bg);
                color: #314f73;
                margin-right: -10px;
                padding-left: 10px;
                clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%, 15px 50%);

                &:first-child {
                    padding-left: 0;
                    clip-path: polygon(0 0, calc(100% - 15px) 0, 100% 50%, calc(100% - 15px) 100%, 0 100%);
                }

                &:last-child {
                    margin-right: 0 !important;
                    clip-path: polygon(0 0, 100% 0, 100% 100%, 0 100%, 15px 50%);
                }
            }

            :deep(.el-anchor__link) {
                height: 100%;
                width: 100%;
            }

            .item {
                position: relative;
                display: flex;
                align-items: center;
                justify-content: center;
                height: 100%;
                width: 100%;
            }

            .active {
                color: #fff;
                background-color: var(--tig-primary);
            }
        }
    }

    .form-container {
        flex: 1;
        overflow-y: auto;

        .title {
            background-color: #f5f7fa;
            font-size: 14px;
            font-weight: bold;
            padding: 12px 10px;
            display: flex;
            align-items: center;
            margin-bottom: 20px;

            .xian {
                width: 3px;
                height: 14px;
                background-color: var(--tig-primary);
                margin-right: 5px;
            }
        }

        .form-warp {
            margin-top: 10px;
        }
    }

    /* 隐藏滚动条 */

    .form-container::-webkit-scrollbar {
        display: none;
        /* 对于 WebKit 浏览器（如 Chrome 和 Safari） */
    }

    .fixed-shipping-type {
        position: relative;

        .extra {
            position: absolute;
        }
    }
}

@media (max-width: 768px) {
    .product-container {
        .anchor {
            .tab-row {
                overflow-x: auto;
                flex-wrap: nowrap;
                -webkit-overflow-scrolling: touch;

                &::-webkit-scrollbar {
                    display: none;
                }

                :deep(.el-anchor__item) {
                    flex: 0 0 auto;
                    min-width: 100px;
                    padding-left: 20px;
                    margin-right: -15px;
                }

                :deep(.el-anchor__item:first-child) {
                    padding-left: 10px;
                }
            }
        }

        :deep(.el-form-item) {
            display: flex;
            flex-direction: column;
            align-items: flex-start;

            .el-form-item__label {
                width: auto !important;
                text-align: left;
                padding: 0 0 8px 0;
                line-height: 1.5;
            }

            .el-form-item__content {
                width: 100%;
                margin-left: 0 !important;
            }
        }

        .form-container {
            padding: 0 10px;
        }
    }

    .inner-item {
        :deep(.el-textarea) {
            width: 100% !important;
        }

        :deep(.el-select) {
            min-width: 0 !important;
            width: 100% !important;
        }

        :deep(.el-input) {
            width: 100% !important;
        }

        :deep(.el-input-number) {
            width: 100% !important;
        }
    }
}
</style>
