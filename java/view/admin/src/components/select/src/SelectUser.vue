/** * @file 会员选择器组件 * @description 远程搜索会员的下拉选择器，支持按名称或手机号搜索 * @features * - 远程搜索（名称/手机号） * - 手机号脱敏显示 * -
支持回显已选会员 * - 可选显示搜索提示 */
<template>
    <el-select
        style="width: 100%"
        v-model="userId"
        @change="handleChange"
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
import { getUserList } from "@/api/user/user";
import { message } from "ant-design-vue";
import { UserFilterParams } from "@/types/user/user";
import { maskString } from "@/utils/util";

const props = defineProps({
    userId: { type: Number },
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
const loading = ref<boolean>(true);
/** 查询参数 */
const filterParams = reactive<UserFilterParams>({
    page: 1,
    size: 20
});
const emit = defineEmits(["update:userId", "change"]);

/** 选中的会员ID，支持 v-model 双向绑定 */
const userId = computed({
    get: () => props.userId,
    set: (newValue) => emit("update:userId", newValue)
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

/**
 * 更新/插入当前选中的会员到选项列表（用于回显）
 * @description 确保已选会员信息在选项列表中存在
 */
const upsertCurrentOption = () => {
    if (!props.userId || !props.username) return;
    const current = {
        userId: props.userId,
        username: props.username,
        mobile: props.mobile
    };
    const idx = options.value?.findIndex((it: any) => it?.userId === props.userId) ?? -1;
    if (idx === -1) {
        options.value = [current, ...(options.value || [])];
    } else {
        options.value[idx] = { ...options.value[idx], ...current };
    }
};

/**
 * 选择会员时透传完整会员信息
 */
const handleChange = (selectedUserId?: number) => {
    if (!selectedUserId) {
        emit("change", null);
        return;
    }

    const selected = options.value?.find((item: any) => item?.userId === selectedUserId);
    if (selected) {
        emit("change", selected);
        return;
    }

    // 兜底：如果选项列表里暂时没有，至少回传当前回显信息
    if (props.userId === selectedUserId) {
        emit("change", {
            userId: props.userId,
            username: props.username,
            mobile: props.mobile
        });
    }
};

onMounted(() => {
    upsertCurrentOption();
});

// 监听 userId、username、mobile 变化，更新回显选项
watch(
    () => [props.userId, props.username, props.mobile],
    () => {
        upsertCurrentOption();
    },
    { immediate: true }
);

/**
 * 远程搜索会员
 * @param {number|string} query - 搜索关键字
 */
const loadFilter = async (query?: number | string) => {
    loading.value = true;
    filterParams.keyword = "";
    filterParams.keyword = String(query);
    try {
        const result = await getUserList({ ...filterParams });
        options.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped></style>
