<template>
    <Component v-if="loaded" v-model:module="props.module"></Component>
</template>
<script lang="ts" setup>
import { ref, defineAsyncComponent, shallowRef, onMounted, watch } from "vue";
import { toPascalCase } from "@/utils/util";
const props = defineProps({
    module: Object,
    moduleType: {
        type: String,
        default: "",
    },
});
const Component = shallowRef();
const loaded = ref(false);
const _import = (path: string) => defineAsyncComponent(() => import(`./src/${path}.vue`));
onMounted(async () => {
    Component.value = _import(toPascalCase(props.moduleType));
    loaded.value = true;
});
watch(
    () => props.moduleType,
    (newVal) => {
        Component.value = _import(toPascalCase(props.moduleType));
    }
);
</script>
