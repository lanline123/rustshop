<template>
    <view class="" />
</template>

<script setup lang="ts">
/**
 * 消息提示音组件
 * - 新消息到达时播放提示音
 */
import { onUnload } from "@dcloudio/uni-app";
import { ref } from "vue";
import { staticResource } from "@/utils";
let audioRef = null;
const isPlaying = ref(false);
audioRef = uni.createInnerAudioContext();
audioRef.src = staticResource("promptTone.wav", "audio");
audioRef.onEnded(() => {
    isPlaying.value = false;
});
// audioRef.onError((err: any) => {
//     console.log("audio error", err);
// });
// audioRef.onPlay(() => {
//     console.log("audio onPlay");
// });
const play = () => {
    if (audioRef != null && !isPlaying.value) {
        audioRef.seek(1);
        audioRef.play();
        isPlaying.value = true;
    }
};
defineExpose({
    play
});

onUnload(() => {
    if (audioRef != null) {
        audioRef!.destroy();
    }
});
</script>

<style lang="scss" scoped></style>
