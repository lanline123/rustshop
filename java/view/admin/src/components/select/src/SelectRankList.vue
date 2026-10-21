/** 会员等级选择器 - 用于选择用户会员等级 */
<template>
    <el-select v-model="rankId">
        <el-option
          :key="0"
          :label="$t('请选择...')"
          :value="0"
        />
        <el-option
          v-for="item in options"
          :key="item.rankId"
          :label="item.rankName"
          :value="item.rankId"
        />
    </el-select>
</template>
<script lang="ts" setup>
import {getUserRankList} from "@/api/user/userRank";
import {message} from "ant-design-vue";
import {computed, onMounted, reactive, ref} from "vue";
import {UserRankFilterParams,UserRankFilterState} from "@/types/user/userRank";

const props = defineProps({
    modelValue: {type: Number},
})
const emit = defineEmits([
    "update:modelValue",
])

/** 选中的会员等级ID，支持 v-model 双向绑定 */
const rankId = computed({
    get: () => {
        return props.modelValue
    },
    set: (newValue) => {
        emit('update:modelValue', newValue)
    }
})

/** 查询参数 */
const filterParams = reactive<UserRankFilterParams>({
    sortField: '',
    sortOrder: '',
});
/** 会员等级列表 */
const options = ref<UserRankFilterState[]>([])

/** 加载会员等级列表 */
const loadFilter = async () => {
    try {
        const result = await getUserRankList({...filterParams});
        options.value = result.userRank.records;
    } catch (error:any) {
        message.error(error.message);
    }
}

onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>

</style>
