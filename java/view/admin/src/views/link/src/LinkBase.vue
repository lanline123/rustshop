<template>
    <div class="div0">
        <div class="tag-container">
            <div v-for="(item, index) in filterState" :key="index" :class="{ 'active': activeItem == item.name }" class="tag" @click="selectItem(item)">
                {{ item.name }}
            </div>
        </div>

    </div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { LinkFilterState } from "@/types/decorate/pcNavigation.d";
import { message } from "ant-design-vue";
import { getSelectLinkList } from "@/api/decorate/pcNavigation";
import { useRoute } from "vue-router";
const linkSelectData = defineModel('linkSelectData');
const filterState = ref<LinkFilterState[]>();
const activeItem = ref(null);
const emit = defineEmits(['update:modelValue'])

const route = useRoute();
const selectItem = (value:any) => {
    activeItem.value = value.name;
    link.value = value
    linkSelectData.value = {
        path: 'base',
        label: value.name,
        name: value.name,
        link: value.link
    }
};
// 传值
const props = defineProps({
    modelValue: { type: Object },
    // SelectLink 传入：mobile=移动端装修，pc=PC 装修
    type: { type: String, default: "" }
    //multiple 是否多选，可直接写在父组件
})
const link = computed({
    get: () => props.modelValue,
    set: (newValue) => {
        emit("update:modelValue", newValue)
    }
});
const loading = ref<boolean>(true);
// 仅移动端装修可选：抽奖、选择门店、进入最近/已选门店
const mobileOnlyLinks = ["lottery/index", "store/select/index", "toNearestStore"];
const normalizeBaseLink = (link: unknown) => String(link ?? "").replace(/^\/+|\/+$/g, "");
const isPcDecorateContext = () => {
    if (props.type === "pc") return true;
    const path = String(route.path || "");
    // 不因 type=mobile 直接放行：PC 模块若误传 mobile，仍按路由隐藏门店页
    return (
        path.startsWith("/decorate/pc") ||
        path.includes("/pc-decorate") ||
        path.includes("/pc-navigation") ||
        path.includes("/pc-cat-floor") ||
        path.includes("pcCustomPage")
    );
};
const syncActiveItem = () => {
    const current = linkSelectData.value;
    if (!current || (!current.name && !current.link && !current.path)) return;
    const currentLink = normalizeBaseLink(current.link);
    const matchedByName = filterState.value?.find((item) => current.name && item.name === current.name);
    const matchedByLink = filterState.value?.find((item) => {
        const itemLink = normalizeBaseLink(item.link);
        return itemLink === currentLink && (currentLink !== "" || !current.name);
    });
    const matched = matchedByName || matchedByLink;
    if (matched) {
        activeItem.value = matched.name || null;
        return;
    }
    if (current.path !== "base") return;
    activeItem.value = current.name || null;
};
const loadFilter = async () => {
    loading.value = true;
    try {
        let result: any = await getSelectLinkList();
        if (isPcDecorateContext()) {
            result = result.filter((item: any) => !mobileOnlyLinks.includes(normalizeBaseLink(item.link)));
        }
        filterState.value = result;
        syncActiveItem();
    } catch (error:any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
}
onMounted(() => {
    loadFilter();
});
watch(
    () => linkSelectData.value,
    () => {
        syncActiveItem();
    },
    { deep: true, immediate: true }
);

</script>
<style scoped lang="less">
.div0 {
    .tag-container {
        display: flex;
        gap: 10px;
        flex-wrap: wrap;
    }

    .tag {
        border: 1px solid #eee;
        border-radius: 4px;
        padding: 5px 12px;
        white-space: nowrap;
        cursor: pointer;
        color: #666;
        transition: all 0.3s;
        font-size: 12px;
        line-height: 20px;
    }

    .tag.active {
        border-color: #428ce8;
        color: #428ce8;
    }
}
</style>
