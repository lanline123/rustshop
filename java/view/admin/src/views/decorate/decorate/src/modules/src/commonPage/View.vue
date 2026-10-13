<template>
    <div class="module-ad-con module-topbar-warp" :style="format.titleBackgroundColor">
        <div class="module-topbar-con">
            <div class="module-topbar-title-con">
                <div class="title-item" :style="format.titleColor">{{ module?.title || $t('页面标题') }}</div>
            </div>
        </div>
    </div>
    <!--  -->
</template>
<script lang="ts" setup>
import { ref, onMounted, computed, watch } from "vue";
const props = defineProps({
    module: Object
});
const module = ref(props.module);
// 页面设置
const defaultModule = {
    title: "",
    titleColor: "",
    headerStyle: 1,
    immersive: false,
    titleBackgroundColor: "",
    backgroundImage: {
        picUrl: "",
        picThumb: ""
    },
    backgroundColor: "",
    backgroundRepeat: 1,
    backgroundSize: 1
};
const dealDefault = () => {
    for (let i in defaultModule) {
        if (typeof module.value[i] === "undefined") {
            module.value[i] = defaultModule[i];
        }
    }
};
onMounted(async () => {
    dealDefault();
});

const format = computed(() => {
    return {
        titleBackgroundColor: module.value.titleBackgroundColor ? "background-color:" + module.value.titleBackgroundColor + ";" : "",
        titleColor: "color:" + module.value.titleColor + ";"
    };
});
const backgroundColor = ref("#000000");
// background-position: top center;
// {{format.backgroundColor}}
// {{format.backgroundImage}}
// {{format.backgroundRepeat}}
// {{format.backgroundSize}}

watch(
    () => props.module,
    (newVal: any) => {
        if (newVal) {
            module.value = newVal;
            dealDefault();
        }
    }
);
</script>
<style lang="less"></style>
