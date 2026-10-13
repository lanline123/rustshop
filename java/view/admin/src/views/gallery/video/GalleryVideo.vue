<template>
    <GalleryVideoPicker ref="pickerRef" :is-multiple="isMultiple" @submitCallback="emit('submitCallback', $event)" @okType="emit('okType', $event)" @close="emit('close')" />
</template>

<script setup lang="ts">
import { ref } from "vue";
import GalleryVideoPicker from "./GalleryVideoPicker.vue";

defineProps({
    isMultiple: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(["submitCallback", "okType", "close"]);
const pickerRef = ref();

const onFormSubmit = () => {
    pickerRef.value?.onFormSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" src="../gallery.less"></style>
