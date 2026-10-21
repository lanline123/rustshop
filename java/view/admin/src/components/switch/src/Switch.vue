<template>
  <div class="custom-switch">
    <div class="switch-mask" @click="switchClick({field: params.field },params.row)"></div>
    <el-switch v-bind="$attrs"></el-switch>
  </div>
</template>

<script setup lang="ts">
import { useAttrs } from "vue";
const props = defineProps({
  params: {
    type: Object,
    default: {},
  },
});
// 父组件回调
const emit = defineEmits(["switch-click"]);
const attrs = useAttrs();
const switchClick = (data: any, row: any) => {
  let info = {data, row}
  emit("switch-click", info);
};
</script>
<style lang="less" scoped>
.custom-switch{
    position: relative;
    .switch-mask{
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        cursor: pointer;
        opacity: 0.5;
        z-index: 10;
    }
}
</style>
