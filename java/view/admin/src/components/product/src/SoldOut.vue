<template>
    <div class="sold-out" :style="style">
        <div class="sold-out-img" :style="imgStyle"> {{ $t('已售罄') }} </div>
    </div>
</template>


<script setup lang="ts">
const props = defineProps({
    style: {
        type: Object,
        default: () => {}
    },
    imgStyle: {
        type: Object,
        default: () => {}
    }
});
</script>



<style scoped lang="less">
.sold-out {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: 10;
    background-color: rgba(255, 255, 255, 0.4);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    .sold-out-img {
        width: 60%;
        height: 60%;
        border-radius: 50%;
        background-color: rgba(0, 0, 0, 0.4);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: var(--tig-pos-font-size-base);
    }
}
</style>
