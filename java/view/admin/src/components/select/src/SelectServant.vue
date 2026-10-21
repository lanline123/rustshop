/** 客服选择器 - 用于选择客服人员，包含"全部客服"选项 */
<template>
    <el-select
        v-model="modelValue"
        filterable
    >
        <el-option
            v-for="item in options"
            :key="item.adminId"
            :label="item.username"
            :value="item.adminId"
        />
    </el-select>
</template>
<script lang="ts">
type TransferListRecord = {
    user?: {
        adminId: number;
        username?: string;
    } | null;
};

export const createServantOptions = (records: TransferListRecord[], allServantsLabel: string) => [
    {
        adminId: -1,
        username: allServantsLabel
    },
    ...records
        .filter((item): item is { user: NonNullable<TransferListRecord["user"]> } => Boolean(item.user))
        .map((item) => ({
            adminId: item.user.adminId,
            username: item.user.username
        }))
];
</script>
<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { getTransferList } from "@/api/im/servant";
import { message } from "ant-design-vue";
import {AdminUserFilterState} from "@/types/authority/adminUser"

/** 选中的客服ID */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const modelValue = defineModel({
    default: () => 0,
    type: [String]
});
/** 客服列表 */
const options = ref<AdminUserFilterState[]>();

onMounted(() => {
    getUserList();
});

/** 查询参数 */
const filterParams = reactive({
    page: 1,
    size: 999,
});

/** 加载客服列表（包含"全部客服"选项） */
const getUserList = async () => {
    const allServantsLabel = t('全部客服');
    try {
        options.value = createServantOptions([], allServantsLabel);
        const result = await getTransferList({ ...filterParams });
        options.value = createServantOptions(result?.records || [], allServantsLabel);
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>
<style scoped lang="less">

</style>
