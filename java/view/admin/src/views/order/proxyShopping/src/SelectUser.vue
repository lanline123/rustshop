/**
 * @file 会员选择器组件（代客下单专用）
 * @description 远程搜索会员的下拉选择器，支持按名称或手机号搜索
 * @features
 * - 远程搜索（名称/手机号）
 * - 手机号脱敏显示
 * - 支持回显已选会员
 * - 可选显示搜索提示
 */
<template>
    <el-select
        style="width: 100%"
        v-model="userId"
        :loading="loading"
        :remote-method="loadFilter"
        filterable
        :multiple="props.multiple"
        :placeholder="$t('请输入会员名称/手机号')"
        remote
        remote-show-suffix
        reserve-keyword
    >
        <el-option
            v-for="item in options"
            :key="item.userId"
            :label="(props.showMemberNamePrefix ? $t('会员名称：') : '') + item.username + (item.mobile ? '（' + maskMobile(item.mobile) + '）' : '')"
            :value="item.userId"
        />
    </el-select>
    <template v-if="showTips">
        <div class="extra">{{ $t('注意：搜索结果只显示前20条记录，如果没有找到相应会员，请更精确地查找；') }}</div>
        <div class="extra">{{ $t('另外，如果该会员是从论坛注册的且没有在商城登录过，也无法找到，需要先在商城登录。') }}</div>
    </template>
</template>

<script lang="ts" setup>
import { computed, reactive, ref, watch, onMounted } from "vue";
import type { PropType } from "vue";
import { getProxyShoppingUserList } from "@/api/proxyShopping/user";
import { message } from "ant-design-vue";
import type { UserFilterParams } from "@/types/user/user";
import { maskString } from "@/utils/util";

const props = defineProps({
    userId: { type: Number },
    /** v-model:user：选中的用户对象（单选时为对象，多选时可自行约定为数组） */
    user: { type: Object as PropType<any>, default: undefined },
    /** 用于回显的用户名 */
    username: { type: String, default: "" },
    /** 用于回显的手机号 */
    mobile: { type: String, default: "" },
    /** 是否显示搜索提示 */
    showTips: { type: Boolean, default: false },
    /** 是否显示"会员名称："前缀 */
    showMemberNamePrefix: { type: Boolean, default: true },
    /** 是否多选 */
    multiple: { type: Boolean, default: true }
});

/** 加载状态 */
const loading = ref<boolean>(false);
/** 查询参数 */
const filterParams = reactive<UserFilterParams>({
    page: 1,
    size: 20
});
const emit = defineEmits(["update:userId", "update:user"]);

/** 选中的会员ID，支持 v-model 双向绑定 */
const userId = computed({
    get: () => props.userId,
    set: (newValue) => emit("update:userId", newValue)
});

/** 选中的会员对象，支持 v-model 双向绑定 */
const user = computed({
    get: () => props.user,
    set: (newValue) => emit("update:user", newValue)
});

/** 会员选项列表 */
const options: any = ref([]);

/** 是否为演示模式 */
const isDemo = ref(import.meta.env.VITE_IS_DEMO || 0);

/**
 * 手机号脱敏处理
 * @param {string} mobile - 手机号
 * @returns {string} 脱敏后的手机号
 */
const maskMobile = (mobile?: string) => {
    if (!mobile) return "";
    if (isDemo.value == 0) {
        return mobile;
    }
    return maskString(mobile, 3, 4);
};

const syncUserByValue = (val: any) => {
    // 单选：val 为 userId；多选：val 为 userId[]（此处仅做最基础同步）
    if (Array.isArray(val)) {
        const list = val
            .map((id) => options.value?.find((it: any) => Number(it?.userId) === Number(id)))
            .filter(Boolean);
        user.value = list;
        return;
    }
    const selected = options.value?.find((it: any) => Number(it?.userId) === Number(val));
    user.value = selected;
};

// 当用户在下拉框里“选中/取消选中”时，同步输出选中的用户对象
watch(
    () => userId.value,
    (val) => {
        syncUserByValue(val);
    }
);

/**
 * 远程搜索会员
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    const q = String(query ?? "").trim();
    // 只有输入了内容才远程搜索；清空输入时不请求、清空列表（保留回显项）
    if (!q) {
        loading.value = false;
        return;
    }
    loading.value = true;
    filterParams.keyword = q;
    try {
        const result = await getProxyShoppingUserList({ ...filterParams });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>

<style lang="less" scoped></style>


