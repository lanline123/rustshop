<template>
    <Component v-if="loaded" v-model:module="props.module"></Component>
</template>
<script lang="ts" setup>
import { ref, defineAsyncComponent, shallowRef, onMounted, watch } from "vue";
import { toPascalCase, toCamelCase } from "@/utils/util";
const props = defineProps({
    module: Object,
    moduleType: {
        type: String,
        default: "",
    },
    moduleUrl: {
        type: String,
        default: "",
    }
});
const Component = shallowRef();
const loaded = ref(false);
const _import = (path: string) => defineAsyncComponent(() => import(`./modules/${props.moduleUrl}/${path}/View.vue`));
onMounted(async () => {
    Component.value = _import(toCamelCase(props.moduleType));
    loaded.value = true;
});
watch(
    () => props.moduleType,
    (newVal) => {
        Component.value = _import(toCamelCase(props.moduleType));
    }
);
</script>
