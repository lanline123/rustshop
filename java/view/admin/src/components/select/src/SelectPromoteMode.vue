/** 分销员等级选择器 - 用于选择分销员推广等级 */
<template>
    <div class="container">
        <el-select
            v-model="level"
            :placeholder="$t('请选择等级')"
            clearable
            @change="emit('onChange', $event)"
            >
            <el-option
                v-for="item in formState.level"
                :key="item.id"
                :label="item.name"
                :value="item.id"
            />
            </el-select>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { SalesmanConfigFormState } from "@/types/salesman/promoteMode.d";
import { getSalesmanConfig } from "@/api/salesman/promoteMode";

const emit = defineEmits(['onChange'])
/** 选中的等级ID */
const level = defineModel<any>("level");
/** 分销配置数据 */
const formState = ref<SalesmanConfigFormState>({
    saleType: 1,
    level: []
});

/** 加载分销配置（包含等级列表） */
const loadFilter = async () => {
    try {
        const result = await getSalesmanConfig();
        formState.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    loadFilter();
});
</script>

