<template>
    <div class="container">
        <el-select
            v-model="categoryId"
            :placeholder="$t('请选择素材分类')"
            clearable
            @change="emit('onChange', $event)"
            >
            <el-option
                v-for="item in formState"
                :key="item.categoryId"
                :label="item.categoryName"
                :value="item.categoryId"
            />
            </el-select>
    </div>
</template>
<script lang="ts" setup>
import { ref, onMounted, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getcategoryList } from "@/api/salesman/category";
const emit = defineEmits(['onChange'])
const categoryId = defineModel<any>("categoryId");
const formState = ref<any>({
    
});
const loadFilter = async () => {
    try {
        const result = await getcategoryList({size: 9999});
        formState.value = result.records;
    } catch (error: any) {
        message.error(error.message);
    }
};
onMounted(() => {
    loadFilter();
});
</script>

