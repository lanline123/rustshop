<template>
    <div class="entry-main">
        <div class="module-part" v-for="(item, index) in module">
            <h3 class="part-title">{{ item.title }}</h3>
            <div class="content" v-html="item.content"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from "vue";

const props = defineProps({
    module: {
        type: Array<any>,
        default: []
    }
});
</script>
<style lang="less" scoped>
.entry-main {
    box-sizing: border-box;
    transform: scale(0.5);
    transform-origin: top left;
    width: 200%;
    height: 700px;
    display: flex;
    align-items: center;
    flex-direction: column;
    .module-part {
        display: flex;
        width: 95%;
        flex-direction: column;
        gap: 15px;
        box-sizing: border-box;
        .part-title {
            color: #333;
            font-size: 26px;
            font-weight: 400;
            margin-bottom: 10px;
            margin-top: 20px;
            padding: 10px;
            text-align: center;
            box-sizing: border-box;
        }

        .content {
            box-sizing: border-box;
            background: #fff;
            padding: 35px 30px 40px;
            font-size: 14px;
            line-height: 2.5;
        }
    }
}
</style>
