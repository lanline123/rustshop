<template>
    <audio class="audio" ref="audioRef" :src="promptTone" preload="auto" @ended="isPlaying = false"></audio>
</template>

<script setup lang="ts">
import promptTone from "@/assets/im/promptTone.wav";
import { ref } from "vue";
const audioRef = ref();
const isPlaying = ref(false);
const play = () => {
    if (isPlaying.value) return;
    isPlaying.value = true;
    audioRef.value.play();
};
defineExpose({
    play
});
</script>

<style lang="less" scoped>
.audio {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
}
</style>
