/** 地区选择器 - 省市区级联选择，数据缓存在 store 中 */
<template>
    <div v-if="props.lazy && lazyError" class="region-error">
        <span>{{ $t("地区加载失败") }}</span>
        <el-button link type="primary" @click="retry">{{ $t("重试") }}</el-button>
    </div>
    <div v-else-if="props.lazy && lazyLoading" class="region-loading">{{ $t("地区加载中") }}</div>
    <el-cascader
        :placement="props.placement"
        style="width: 100%"
        :props="cascaderProps"
        :placeholder="$t(props.placeholder)"
        :options="displayOptions"
        v-model="selectedIds"
        clearable
        @change="onChange"
        @clear="onClear"
    />
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRegionStore } from "@/store/region";
import { message } from "ant-design-vue";
import { getProvinceRegionList, getRegionListByRegionId } from "@/api/setting/region";
import { normalizeRegionId } from "./regionSelection";

const region = useRegionStore();

const props = defineProps({
    /** 是否可以选择任意层级 */
    checkStrictly: { type: Boolean, default: false },
    /** 是否仅显示并单选一级地区 */
    onlyTopLevel: { type: Boolean, default: false },
    /** 占位文字对应的 i18n key */
    placeholder: { type: String, default: "请选择地区" },
    /** 级联面板弹出位置 */
    placement: { type: String, default: "right" },
    /** 跨境地址逐级从公开地区接口懒加载 */
    lazy: { type: Boolean, default: false },
    /** 跨境国家根节点；传入后从该国家继续加载，而非加载全国省份 */
    rootRegionId: { type: [Number, String], default: undefined }
});

/** 选中的地区ID数组 */
const selectedIds = defineModel<number | string | number[]>("selectedIds");
/** 级联选择器配置 */
const cascaderProps = computed(() => ({
    checkStrictly: props.checkStrictly || props.onlyTopLevel,
    label: "regionName",
    value: "regionId",
    children: "children",
    lazy: props.lazy,
    lazyLoad: props.lazy ? loadChildren : undefined
}));

/** 地区选项列表 */
const options = ref<any[]>([]);
const lazyError = ref<unknown>(null);
const lazyLoading = ref(false);
const lazyLoaded = ref(false);
const displayOptions = computed(() => {
    if (!props.onlyTopLevel) {
        return options.value;
    }
    // 仅保留一级节点，隐藏下级地区
    return options.value.map((item: any) => ({
        regionId: item.regionId,
        regionName: item.regionName,
        leaf: true,
        children: [] as any[]
    }));
});
/** 数据加载完成标识 */
const loaded = ref(false);
/** 选中的地区完整信息 */
const selectedRegions = ref<any[]>([]);

const emit = defineEmits(["update:modelValue", "change", "clear"]);

onMounted(() => {
    if (props.lazy) {
        loadLazyRoot();
        return;
    }
    if (region.allRegion == null) {
        loadRegion();
    } else {
        options.value = normalizeOptions(region.allRegion as any[]);
    }
});

const loadLazyRoot = async () => {
    if (lazyLoaded.value) return;
    lazyLoading.value = true;
    lazyError.value = null;
    try {
        const root = props.rootRegionId == null
            ? await getProvinceRegionList()
            : await getRegionListByRegionId(props.rootRegionId);
        options.value = normalizeOptions(root);
        lazyLoaded.value = true;
    } catch (error: any) {
        lazyError.value = error;
        message.error(error?.message || "地区加载失败，请重试");
    } finally {
        lazyLoading.value = false;
    }
};

const normalizeOptions = (items: any[] = []) =>
    (Array.isArray(items) ? items : [])
        .map((item) => {
            const regionId = normalizeRegionId(item?.regionId ?? item?.id);
            if (regionId === null) return null;
            const children = Array.isArray(item.children) ? normalizeOptions(item.children) : [];
            return { ...item, regionId, regionName: item.regionName ?? item.name ?? "", children };
        })
        .filter(Boolean);

const findNode = (id: number, items: any[] = options.value): any => {
    for (const item of items) {
        if (normalizeRegionId(item.regionId) === id) return item;
        const found = findNode(id, item.children || []);
        if (found) return found;
    }
    return null;
};

const loadChildren = async (node: any, resolve: (children: any[]) => void) => {
    const parentId = normalizeRegionId(node?.value ?? node?.regionId);
    if (parentId === null) return resolve([]);
    lazyLoading.value = true;
    lazyError.value = null;
    try {
        const children = normalizeOptions(await getRegionListByRegionId(parentId));
        const target = findNode(parentId);
        if (target) {
            target.children = children;
            target.leaf = children.length === 0;
        }
        resolve(children);
    } catch (error: any) {
        lazyError.value = error;
        selectedIds.value = [];
        emit("update:modelValue", []);
        message.error(error?.message || "地区加载失败，请重试");
        resolve([]);
    } finally {
        lazyLoading.value = false;
    }
};

/** 加载地区数据（优先从 store 缓存获取） */
const loadRegion = async () => {
    if (loaded.value) {
        return;
    }
    loaded.value = true;
    try {
        await (region as any).getRegionList();
        options.value = normalizeOptions((region as any).allRegion);
        lazyLoaded.value = true;
    } catch (error: any) {
        lazyError.value = error;
        selectedIds.value = [];
        emit("update:modelValue", []);
        message.error(error.message);
    } finally {
        loaded.value = false;
    }
};

const retry = () => (props.lazy ? loadLazyRoot() : loadRegion());

/**
 * 选择变更处理
 * @param {number[]} ids - 选中的地区ID数组
 */
const onChange = (ids: number[] | number | string) => {
    const normalizedIds = (Array.isArray(ids) ? ids : [ids])
        .map(normalizeRegionId)
        .filter((id): id is number => id !== null);
    selectedIds.value = normalizedIds;
    selectedRegions.value = findSelectedRegions(normalizedIds, options.value);
    emit("change", selectedRegions.value);
    emit("update:modelValue", normalizedIds);
};

/** 清空选择 */
const onClear = () => {
    selectedIds.value = [];
    selectedRegions.value = [];
    emit("clear");
};

/**
 * 根据ID数组查找对应的地区信息
 * @param {number[]} ids - 地区ID数组
 * @param {any[]} options - 地区选项树
 * @returns {any[]} 地区信息数组
 */
const findSelectedRegions = (ids: number[], options: any[]): any[] => {
    const result: any[] = [];
    if (ids && ids.length > 0) {
        for (const id of ids) {
            const region = findRegionById(id, options);
            if (region) {
                result.push(region);
            }
        }
    }
    return result;
};

/**
 * 递归查找指定ID的地区
 * @param {number} id - 地区ID
 * @param {any[]} options - 地区选项树
 * @returns {any} 地区信息或 null
 */
const findRegionById = (id: number, options: any[]): any => {
    for (const option of options) {
        if (option.regionId === id) {
            return option;
        }
        if (option.children && option.children.length > 0) {
            const found = findRegionById(id, option.children);
            if (found) {
                return found;
            }
        }
    }
    return null;
};
</script>

<style lang="less" scoped>
.region-error {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #f56c6c;
    margin-bottom: 6px;
}

.region-loading {
    color: #909399;
    margin-bottom: 6px;
}

.cascader {
    width: 100%;
}
</style>
