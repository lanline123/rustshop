<template>
    <div class="reception-panel-toolbar-waiting" @click="showWin"> {{ $t('转接') }} </div>
    <el-dialog :destroy-on-close="true" v-model="visible" :show-close="false" width="560">
        <template #header="{ close, titleId, titleClass }">
            <div class="header-title">
                <div class="title"> {{ $t('转接客服') }} </div>
                <div @click="closeWin" class="right">
                    <em class="main_pel_m iconfont-admin icon-cha1"></em>
                </div>
            </div>
        </template>
        <ReconnectionList v-model:servantId="servantId" :conversationId="conversationId" v-if="visible" ref="ReconnectionListRef"></ReconnectionList>
        <template #footer>
            <div class="dialog-footer">
                <el-button @click="closeWin">{{ $t('取消') }}</el-button>
                <el-button :disabled="servantId==0" type="primary" @click="save"> {{ $t('转接') }} </el-button>
            </div>
        </template>
    </el-dialog>
</template>
<script setup lang="ts">
import { ref } from "vue";
import ReconnectionList from "@/views/im/src/ReconnectionList.vue";
import { updateSeckill } from "@/api/promotion/seckill";
import { message } from "ant-design-vue";
import { transferConversation } from "@/api/im/conversation";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const ReconnectionListRef:any = ref(null)
const visible = ref(false);
const props = defineProps({
    conversationId: {
        type: Number,
        default: 0
    }
})
const showWin = () => {
    visible.value = true;
    // ReconnectionListRef.value.loadFilter()
};
const closeWin = () => {
    visible.value = false;
};
const emit = defineEmits<{
    (event: 'serversUpdated'): void
}>()
const servantId = ref(0)
const save = async ()=>{
    if(servantId.value==0){
        message.error(t('请选择转接客服'));
        return
    }
    try {
        const result = await transferConversation({ conversationId: props.conversationId, servantId:servantId.value });
        message.success(t('操作成功'));
        visible.value = false;
        //触发父组件的方法
        // 清空当前窗口，刷新列表
        emit('serversUpdated')
    } catch (error: any) {
        message.error(error.message);
    } finally {
    }
}

defineExpose({
    showWin
});
</script>
<style scoped lang="less">
:global(body .el-dialog__header) {
    margin: -16px -16px 16px;
}

.header-title {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .title {
        color: black;
        font-size: 14px;
        display: flex;
        align-items: center;
        font-weight: 500;
        line-height: 20px;
        gap: 10px;

        .circle {
            width: 14px;
            height: 14px;
            background-color: rgb(187, 187, 187);
            border-radius: 50%;
            display: flex;
            justify-content: center;
            align-items: center;
            color: white;
            font-size: 10px;
            line-height: 1;
            font-family: sans-serif;
            box-sizing: border-box;
        }
    }

    .right {
        //cursor: pointer;
    }
}

.width160 {
    width: 160px;
}

.bottom-bar {

    margin-top: 10px;
}


.reception-panel-toolbar-waiting {
    display: inline-block;
    text-decoration: none;
    align-items: center;
    line-height: 20px;
    color: #155bd4;
    //cursor: pointer;
    justify-content: space-between;
    padding: 2px 8px;
    border-radius: 2px;
}
.reception-panel-toolbar-waiting:hover{
    background-color: #f2f3f5;
}
</style>
