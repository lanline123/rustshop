<template>
    <web-view class="service" :src="url" @error="error" @load="load" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import { onLoad } from "@dcloudio/uni-app";
const url = ref("");
onLoad((options) => {
    if (options && options.url) {
        url.value = decodeURIComponent(options.url);
    }
});

const error = (e: any) => {
    console.log(e);
};
const load = (e: any) => {
    console.log(e);
};
</script>

<style lang="scss" scoped>
.service {
    width: 100vw;
    height: 100vh;
}
</style>
