/** 店铺选择器 - 支持远程搜索，S2B2C模式下自动切换为供应商绑定店铺列表 */
<template>
    <el-select
        v-model="shopId"
        :loading="loading"
        filterable
        remote
        remote-show-suffix
        :remoteMethod="remoteMethod"
        :multiple="props.multiple"
        :placeholder="$t(placeholder)"
        popper-class="shop-select-popper"
        clearable
        reserve-keyword
        @change="onChange"
    >
        <el-option v-for="item in options" :key="item.shopId" :label="item.shopTitle" :value="normalizeOptionValue(item.shopId)" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref, type PropType } from "vue";
import { getShop, getShopSelectList } from "@/api/shop/shop";
import { vendorShopBindShopList } from "@/api/vendor/bindShop";
import { isS2b2c } from "@/utils/version";
import { message } from "ant-design-vue";
import type { ShopSelectFilterParams } from "@/types/shop/shop.d";

const props = defineProps({
    modelValue: { type: [Number, String] },
    /** 是否多选 */
    multiple: { type: Boolean, default: false },
    /** 店铺类型：1-店铺 2-门店 */
    type: { type: Number, default: 1 },
    /** 同时展示多种组织类型，例如平台采购可同时选择店铺和门店 */
    shopTypes: { type: Array as PropType<number[]>, default: () => [] },
    placeholder: { type: String, default: "请输入店铺名称" },
    /** 追加店铺列表接口参数 */
    extraParams: { type: Object, default: () => ({}) },
    /** 店铺列表接口，默认使用下拉专用接口 */
    apiList: { type: Function, default: getShopSelectList }
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<ShopSelectFilterParams>({
    keyword: ""
});
const emit = defineEmits(["update:modelValue", "onChange"]);
/** 选中的店铺ID */
const shopId = defineModel<number | string | number[]>("shopId");

/**
 * Element Plus uses strict equality when matching the selected value with an
 * option. Keep option IDs consistent with the model type so values restored
 * from localStorage can still resolve to their shop title.
 */
const normalizeOptionValue = (value: number | string) => {
    const selectedValue = Array.isArray(shopId.value) ? shopId.value[0] : shopId.value;
    return typeof selectedValue === "string" ? String(value) : value;
};

const selectedShopOption = ref<any>();
const appendCurrentShopOption = async (source: any[]) => {
    const selectedValues = (Array.isArray(shopId.value) ? shopId.value : [shopId.value]).filter(
        (value) => value !== undefined && value !== null && value !== ""
    );
    if (!selectedValues.length || source.some((item) => selectedValues.some((value) => String(value) === String(item.shopId)))) return source;

    if (selectedShopOption.value && selectedValues.some((value) => String(value) === String(selectedShopOption.value.shopId))) {
        return [selectedShopOption.value, ...source];
    }

    try {
        const currentShop = JSON.parse(localStorage.getItem("shopInfo") || "{}");
        const currentShopTitle = currentShop.shopTitle || currentShop.shopName;
        if (currentShopTitle && selectedValues.some((value) => String(value) === String(currentShop.shopId))) {
            selectedShopOption.value = { ...currentShop, shopTitle: currentShopTitle };
            return [selectedShopOption.value, ...source];
        }
    } catch {
        // Ignore malformed legacy storage and keep the remote options intact.
    }

    if (selectedValues.length === 1) {
        try {
            const currentShop: any = await getShop("detail", { shopId: selectedValues[0] });
            const currentShopTitle = currentShop?.shopTitle || currentShop?.shopName;
            if (currentShopTitle) {
                selectedShopOption.value = { ...currentShop, shopId: currentShop.shopId ?? selectedValues[0], shopTitle: currentShopTitle };
                return [selectedShopOption.value, ...source];
            }
        } catch {
            // The normal select request remains usable if detail lookup is unavailable.
        }
    }
    return source;
};

/**
 * 选择变更处理
 * @param {any} e - 选中的值
 */
const onChange = (e: any) => {
    emit("update:modelValue", e);
    emit("onChange", e);
};

onMounted(() => {
    loadFilter();
});

/** 是否为供应商角色（S2B2C模式） */
const isVendor = computed(() => {
    const adminType = localStorage.getItem("adminType");
    return isS2b2c() && adminType && adminType === "vendor";
});

/** 店铺选项列表 */
const options: any = ref([]);

/**
 * 加载店铺列表
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || "");
    try {
        let result: any;
        if (isVendor.value) {
            // 供应商模式：获取绑定的店铺列表
            result = await vendorShopBindShopList({ ...filterParams, ...props.extraParams });
            options.value = await appendCurrentShopOption(result.records || []);
        } else {
            const shopTypes = [...new Set(props.shopTypes.length ? props.shopTypes : [props.type])];
            const results = await Promise.all(shopTypes.map((shopType) => props.apiList({ ...filterParams, ...props.extraParams, shopType })));
            const organizationMap = new Map<string, any>();
            results.forEach((response: any) => {
                const records = Array.isArray(response) ? response : response.records || [];
                records.forEach((item: any) => organizationMap.set(String(item.shopId), item));
            });
            options.value = await appendCurrentShopOption([...organizationMap.values()]);
        }
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 远程搜索方法（仅供应商模式生效）
 * @param {string} query - 搜索关键字
 */
const remoteMethod = (query: string) => {
    // if (!isVendor.value) return;
    loadFilter(query);
};
</script>
<style lang="less">
.shop-select-popper {
    min-height: 120px;

    .el-select-dropdown__wrap {
        min-height: 120px;
        max-height: 280px;
    }

    .el-select-dropdown__list {
        min-height: 120px;
        box-sizing: border-box;
    }

    .el-select-dropdown__loading,
    .el-select-dropdown__empty {
        min-height: 120px;
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
    }
}
</style>
