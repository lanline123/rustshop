<template>
    <div class="one-row-style">
        <template v-for="(item, index) in module.picList" :key="index">
            <div :style="{ padding: module.imgPadding + 'px', minWidth: size == 'big' ? '320px' : '160px' }" class="image-container">
                <el-image
                    :class="[module.picRadioStyle == 2 ? 'image-box-radius' : '']"
                    fit="fill"
                    class="image-box"
                    :src="imageFormat(item.picUrl)"
                ></el-image>
            </div>
        </template>
    </div>
</template>

<script setup lang="ts">
const props = defineProps({
    module: { type: Object, default: {} },
    size: { type: String, default: "big" }
});
import { imageFormat } from "@/utils/format";
</script>

<style scoped lang="less">
.one-row-style {
    display: flex;
    overflow-x: auto;
    flex-wrap: nowrap;
    /* 隐藏滚动条但保持滚动功能 */
    -ms-overflow-style: none; /* IE 和 Edge */
    scrollbar-width: none; /* Firefox */
    &::-webkit-scrollbar {
        display: none; /* Chrome, Safari, Opera*/
    }
    .image-container {
        padding: 0;
        text-align: center; /* 图片居中显示 */
        box-sizing: border-box;
        display: flex;
    }
}

.image-box {
    flex: 1;
}

.image-box-radius {
    border-radius: 8px;
}
</style>
