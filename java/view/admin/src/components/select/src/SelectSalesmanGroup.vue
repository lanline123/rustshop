/** 分销员分组选择器 - 用于选择分销员所属分组 */
<template>
    <div class="container">
        <el-select
            v-model="groupId"
            :placeholder="$t('请选择分组')"
            clearable
            @change="emit('onChange', $event)"
            >
            <el-option
                v-for="item in filterState"
                :key="item.groupId"
                :label="item.groupName"
                :value="item.groupId"
            />
            </el-select>
    </div>
</template>

<script setup lang="ts">
import '@/style/css/list.less'
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue'
import type {groupFilterState, groupFilterParams} from "@/types/salesman/subgroup.d";
import {getgroupList} from "@/api/salesman/subgroup";

const emit = defineEmits(['onChange'])
/** 选中的分组ID */
const groupId = defineModel<any>("groupId");
/** 分组列表数据 */
const filterState = ref<groupFilterState[]>();
/** 查询参数 */
const filterParams = reactive<groupFilterParams>({
    page: 1,
    size: 9999,
    sortField: '',
    sortOrder: '',
    groupName: '',
});

/** 加载分组列表 */
const loadFilter = async () => {
    try {
        const result = await getgroupList({ ...filterParams });
        filterState.value = result.records;
    } catch (error:any) {
        message.error(error.message);
    }
}

onMounted(() => {
    loadFilter();
});

</script>