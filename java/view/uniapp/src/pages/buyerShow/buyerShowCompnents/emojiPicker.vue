<template>
    <view class="emoji-picker">
        <scroll-view scroll-x="true" class="emoji-picker-box">
            <view class="emoji-content">
                <text v-for="(emoji, index) in emojis" :key="index" class="emoji-item" @touchend.prevent.stop="insertEmoji(emoji)">{{ emoji }}</text>
            </view>
        </scroll-view>
    </view>
</template>

<script setup lang="ts">
/**
 * 表情选择器组件
 * - 评论时选择表情
 */
import { ref, shallowRef } from "vue";
const emit = defineEmits(["insertEmoji", "closeEmojiPicker"]);
const inputRefEmoji = shallowRef();
const inputFocus = () => {
    inputRefEmoji.value.doFocus();
};

defineExpose({
    inputFocus
});

const emojis = ref<any[]>([]);

const generateEmojis = () => {
    const emojiArray = [];
    const start = 0x1f600;

    for (let i = 0; i < 80; i++) {
        emojiArray.push(String.fromCodePoint(start + i));
    }

    emojis.value = emojiArray;
};

generateEmojis();

// 插入表情到输入框
const insertEmoji = (emoji: string) => {
    console.log(emoji, 222);
    emit("insertEmoji", emoji);
};
</script>

<style lang="scss" scoped>
.emoji-picker-box {
    width: 100%;
    .emoji-content {
        width: 100%;
        display: flex;
        .emoji-item {
            font-size: 48rpx;
            width: 100rpx;
            cursor: pointer;
            border-radius: 8rpx;
            user-select: none;
            &:hover {
                background-color: #f0f0f0;
                transform: scale(1.2);
                transition: transform 0.1s ease;
            }
        }
    }
}
</style>
