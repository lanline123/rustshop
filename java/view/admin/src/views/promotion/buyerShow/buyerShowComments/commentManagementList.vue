<script setup lang="ts">
import CommentTable from './src/CommentTable.vue'
import { useConfigStore } from "@/store/config";
import { useListRequest } from "@/hooks/useListRequest";
import { buyerShowCommentFilterParams, buyerShowCommentFilterState } from "@/types/promotion/buyerShowComment";
import { getBuyerShowEvaluationList } from "@/api/promotion/buyShow";
import { onMounted } from "vue";


const config: any = useConfigStore();

const props = defineProps({
    buyerShowId: {
        type: String ||  Number,
        required: true
    },
});

const {
    listData: filterState,
    loading,
    total,
    filterParams,
    loadData: loadFilter,
} = useListRequest<buyerShowCommentFilterState, buyerShowCommentFilterParams>({
    apiFunction: getBuyerShowEvaluationList,
    idKey: "promotionId",
    defaultParams: {
        page: 1,
        size: config.get("pageSize"),
        buyerShowId: props.buyerShowId,
        sortField: "",
        sortOrder: "",
        keyword: "",
        status: '',
        startTime: '',
        endTime: '',
    }
});

onMounted(()=>{
    loadFilter();
})
</script>

<template>
    <CommentTable
        v-model:filterState="filterState"
        v-model:filterParams="filterParams"
        v-model:total="total"
        v-model:loading="loading"
        :showSelection="false"
        :showRelatedComments="false"
        @okCallback="loadFilter">
    </CommentTable>
</template>

<style scoped lang="less">

</style>
