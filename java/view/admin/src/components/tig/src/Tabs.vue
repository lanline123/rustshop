<template>
    <div class="tabs flex" :class="{ 'tabs--with-badge': hasBadge }">
        <template v-for="item in tabs">
            <template v-if="item.isShow">
                <el-badge :value="item.count" color="#e5484d" :offset="[-3, 3]" :hidden="item.count === 0">
                    <div class="item" @click="onTabChange(item.value)" :style="{fontSize}" :class="modelValue == item.value ? 'active' : ''">
                        {{ item.label }}
                    </div>
                </el-badge>
            </template>
        </template>
    </div>
</template>
<script lang="ts" setup>
import { computed, PropType } from "vue";
const props = defineProps({
    tabs: {
        type: Array as PropType<Array<{ label: string; value: string | number; count?: number; isShow: boolean }>>,
        default: () => []
    },
    fontSize: {
        type: String,
        default: "14px"
    },
});
const emit = defineEmits(["onTabChange"]);
const modelValue = defineModel<any>("modelValue", { default: "" });
const hasBadge = computed(() => props.tabs.some((item) => Object.prototype.hasOwnProperty.call(item, "count")));
const onTabChange = (value: string) => {
    emit("onTabChange", value);
};
</script>
<style scoped lang="less">
.tabs {
    flex-wrap: wrap;
    gap: 10px !important;

    &.tabs--with-badge {
        gap: 18px !important;

        .item {
            margin-right: 0;
        }
    }

    :deep(.el-badge__content) {
        box-sizing: border-box;
        min-width: 16px;
        height: 16px;
        padding: 0 4px;
        border-radius: 8px;
        font-size: 10px;
        line-height: 14px;
    }

    .item {
        min-width: 40px;
        padding: 0 8px;
        text-align: center;
        height: 25px;
        line-height: 25px;
        color: #333;
        margin-right: 5px;
        border-radius: 2px;
        border: 1px solid #fff;
        cursor: pointer;
        &:hover {
            color: var(--tig-primary);
        }
    }
    .active {
        color: var(--tig-primary);
        background: var(--tig-item-active-bg);
        border: 1px solid var(--tig-primary);
    }
}
</style>
