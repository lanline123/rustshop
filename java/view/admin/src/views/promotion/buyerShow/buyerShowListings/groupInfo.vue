<template>
    <div class="groupingBox">
        <div class="groupingList" v-for="(item, index) in buyerShowGroupingList" :key="item.buyerShowGroupId">
            <div :class="[{groupingItem_active: item.active}, 'groupingItem']" @click="changeGroup(item, index)">
                {{item.groupName}}
                <div v-if="item.active" class="checkmark"></div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { getBuyerShowGroupList } from "@/api/promotion/buyShow";
import { ref, onMounted, defineProps, watch } from "vue";
import { message } from "ant-design-vue";

const props = defineProps({
    groupData: {
        type: Array,
        default: () => []
    }
});


const buyerShowGroupingList: any = ref([]);
// 获取买家秀分组列表
const getBuyerShowGroup = async () => {
    const res = await getBuyerShowGroupList({page: 1, size: 1000});
    buyerShowGroupingList.value = res.records || []
    // 回显勾选的数据
    if(props.groupData.length) {
        props.groupData.forEach((valItem: any) => {
            const index = buyerShowGroupingList.value.findIndex((item: any) => valItem === item.buyerShowGroupId)
            if(index > -1) {
                buyerShowGroupingList.value[index].active = true
            }
        })
    }
};

const changeGroup = (item: any, index:  number) => {
    buyerShowGroupingList.value[index].active = !buyerShowGroupingList.value[index]?.active;
};

onMounted(()=>{
    getBuyerShowGroup();
})
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

// 表单通过验证后提交
const onSubmit = async () => {
    try {
        const result = buyerShowGroupingList.value.filter((item: any) => item.active)
        emit('update:confirmLoading', true);
        emit('submitCallback', result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit('update:confirmLoading', false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};

defineExpose({ onFormSubmit });
</script>

<style scoped lang="less">
.groupingBox{
    display: flex;
    flex-wrap: wrap;
    background: #f7f8fa;
    box-sizing: border-box;
    padding: 20px;
    gap: 10px;
}
.groupingItem{
    white-space: nowrap;
    text-overflow: ellipsis;
    background-color: rgb(255, 255, 255);
    color: rgb(38, 38, 38);
    border: 1px solid rgb(217, 217, 217);
    display: inline-block;
    padding: 0 10px;
    font-size: 16px;
    line-height: 30px;
    cursor: pointer;
    box-sizing: border-box;
    position: relative;
}
.groupingItem_active{
    color: #006aff !important;
    background: #f2f7ff !important;
    border: 1px solid #006aff !important;
    border-radius: 2px !important;
    box-sizing: border-box;
}

/* 修改勾选标记样式为三角形 */
.checkmark {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 0;
    height: 0;
    /* 创建三角形 */
    border-style: solid;
    border-width: 0 0 16px 16px;
    border-color: transparent transparent #006aff transparent;
    border-radius: 0 0 2px 0;
}

.checkmark::after {
    content: "✓";
    position: absolute;
    bottom: -13px;
    right: 1px;
    font-size: 10px;
    color: white;
    line-height: 1;
}
</style>
