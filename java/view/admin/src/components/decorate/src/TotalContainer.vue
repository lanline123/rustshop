<template>
    <div
        class="home-warp"
        :style="{
            paddingTop: (module.boxPaddingTop ?? 0) + 'px',
            paddingBottom: (module.boxPaddingBottom ?? 0) + 'px',
            paddingLeft: (module.boxPadding ?? 0) + 'px',
            paddingRight: (module.boxPadding ?? 0) + 'px'
        }"
    >
        <div class="base-class" :style="{ 
            backgroundColor: module.backgroundColor, 
            borderRadius: module.boxRadius + 'px',
            padding: (module.innerPadding ?? 0) + 'px'
        }">
            <slot></slot>
        </div>
    </div>
</template>
<script setup lang="ts">
const props = defineProps({
    module: { type: Object, default: {} }
});
</script>
<style scoped lang="less">
.ds {
    background-color: rgba(129, 88, 73, 0.68);
}
</style>
