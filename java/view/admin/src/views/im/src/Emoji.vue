<template>
    <div class="picker_body">
        <el-scrollbar height="238px">
            <div class="emojipicker">
                <div class="list">
                    <div class="item" v-for="(item, index) in emojiElements" :key="index" @click="insertEmoji(item.name)">
                        <span
                            ><span class="byte-tooltip__wrapper"><emojiImage class="image" :id="item.id" /></span
                        ></span>
                    </div>
                </div>
            </div>
        </el-scrollbar>
    </div>
</template>
<script lang="ts" setup>
import { ref, unref, onMounted } from "vue";
import { emojiImage } from "@/components/emoji";
import emojiElements from "@/components/emoji/src/emojiElements";
const emit = defineEmits(["insertEmoji"]);
const insertEmoji = (emoji: string) => {
    emit("insertEmoji", emoji);
};
</script>
<style lang="less" scoped>
.picker_body {
    width: 100%;
}
.emojipicker .list {
    padding-left: 12px;
    padding-bottom: 12px;
    display: flex;
    flex-wrap: wrap;
}
.emojipicker .list .item {
    position: relative;
    width: 32px;
    height: 32px;
    margin-bottom: 8px;
    margin-right: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
}

.emojipicker .list .item .image {
    transform: translateZ(0);
    transition: transform 0.3s;
    display: block;
    height: 28px;
}
.emojipicker .list .item:hover .image {
    transform: translateZ(0) scale(1.28);
}
</style>
