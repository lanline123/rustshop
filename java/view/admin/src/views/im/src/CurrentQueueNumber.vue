<template>
    <div class="reception-panel-toolbar-waiting" @click="showWin">
        <slot></slot>
    </div>
    <el-dialog :destroy-on-close="true" v-model="visible" :show-close="false" width="960" @close="$emit('close')">
        <template #header="{ close, titleId, titleClass }">
            <div class="header-title">
                <div class="title"> {{ $t('待接入的客户') }} <el-tooltip :content="$t('仅保留最近15天的接入记录')" placement="right" effect="light">
                        <div class="circle">?</div>
                    </el-tooltip>
                </div>
                <div @click="close" class="right">
                    <em class="main_pel_m iconfont-admin icon-cha1"></em>
                </div>
            </div>
        </template>
       <QueueList v-if="visible" ref="QueueListRef"></QueueList>
    </el-dialog>
</template>
<script setup lang="ts">
import { ref } from "vue";
import QueueList from "@/views/im/src/QueueList.vue";
const QueueListRef:any = ref(null)
const visible = ref<boolean>(false);
const emit = defineEmits(["close"]);
const showWin = () => {
    visible.value = true;
    // QueueListRef.value.loadFilter()
};
const close = () => {
    visible.value = false;
};
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
        cursor: pointer;
    }
}

.width160 {
    width: 160px;
}

.bottom-bar {

    margin-top: 10px;
}


.reception-panel-toolbar-waiting {
    display: flex;
    align-items: center;
    line-height: 20px;
    cursor: pointer;
    justify-content: space-between;
}
</style>
