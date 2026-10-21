/** 区域选择器 - 用于选择门店所属区域，支持远程搜索 */
<template>
    <el-select
        v-model="areaStoreManagerId"
        :loading="loading"
        filterable
        remote
        :remoteMethod="remoteMethod"
        :multiple="props.multiple"
        :placeholder="$t('请输入区域名称')"
        clearable
        reserve-keyword
        @change="onChange"
    >
        <el-option v-for="item in options" :key="item.areaStoreManagerId" :label="item.areaStoreName" :value="item.areaStoreManagerId" />
    </el-select>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from "vue";
import { areaStoreManagerList } from "@/api/store/areaManage";
import { message } from "ant-design-vue";
import type { RequestList } from "@/types/store/areaManage";

const props = defineProps({
    modelValue: { type: [Number, String] },
    /** 是否多选 */
    multiple: { type: Boolean, default: false }
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<RequestList>({
    keyword: ""
});
const emit = defineEmits(["update:modelValue", "onChange"]);
/** 选中的区域ID */
const areaStoreManagerId = defineModel<number | string | number[]>("areaStoreManagerId");

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

/** 区域选项列表 */
const options: any = ref([]);

/**
 * 加载区域列表
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = String(query || "");
    try {
        let result: any;
        result = await areaStoreManagerList({ ...filterParams, size: 99 });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};

/**
 * 远程搜索方法
 * @param {string} query - 搜索关键字
 */
const remoteMethod = (query: string) => {
    loadFilter(query);
};
</script>
<style lang="less" scoped></style>
