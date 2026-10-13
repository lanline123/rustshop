<template>
    <tig-popup v-model:show="show" position="bottom">
        <view class="select-region-main">
            <view class="popup-title">{{ $t("选择地区") }}</view>
            <view class="content">
                <view class="region-menu">
                    <view
                        v-for="(item, index) in regionNames"
                        :key="index"
                        class="region-menu-item"
                        :class="{ active: showIndex === index }"
                        @click="showIndex = index"
                    >
                        {{ item ? item : $t("请选择") }}</view
                    >
                </view>
                <template v-for="(item, index) in regionList" :key="index">
                    <template v-if="showIndex === index">
                        <scroll-view scroll-y="true" class="region-list">
                            <view
                                v-for="region in item"
                                :key="region.regionId"
                                class="region-list-item"
                                :class="{ active: regionSelected(region.regionId) }"
                                @click.stop="onSelect(index, region.regionId)"
                                >{{ region.regionName }}</view
                            >
                        </scroll-view>
                    </template>
                </template>
            </view>
        </view>
    </tig-popup>
</template>

<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { getRegionByIds, getRegionListByRegionId } from "@/api/region";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    },
    height: {
        type: String,
        default: "60vh"
    },
    modelValue: {
        type: Array,
        default: () => []
    },
    /** 国家根节点；传入后按接口逐级懒加载，不把根节点写入 regionIds */
    rootRegionId: {
        type: [Number, String],
        default: undefined
    }
});

const emit = defineEmits(["update:show", "update:modelValue", "sendRegionNames"]);

const show = computed({
    get: () => props.show,
    set: () => {
        emit("update:show", false);
    }
});

const regionList = ref<any[]>([]);

const showIndex = ref(0);

const getRegions = async () => {
    try {
        let ids = "";
        if (props.modelValue.length > 0) {
            ids = props.modelValue.join(",");
        }
        const result = await getRegionByIds(ids);
        regionList.value = result;

        if (regionList.value.length > 1) {
            if (regionList.value[regionList.value.length - 1].length > 0) {
                showIndex.value = regionList.value.length - 1;
            } else {
                showIndex.value = regionList.value.length - 2;
            }
        } else {
            showIndex.value = 0;
        }
    } catch (error) {
        console.error("Error fetching regions:", error);
    }
};
watch(
    () => props.modelValue,
    (newVal) => {
        if (newVal) {
            getRegions();
        }
    },
    {
        immediate: true
    }
);

const regionSelected = (regionId: any) => {
    return props.modelValue.includes(regionId);
};

const onSelect = async (index: number, regionId: any) => {
    if (!props.modelValue) return;
    if (index >= 0 && index <= props.modelValue.length) {
        props.modelValue[index] = regionId;
        props.modelValue.splice(index + 1);
    }
    await getRegions();
    if (regionList.value.length == index + 1 || regionList.value[index + 1].length == 0) {
        emit("update:modelValue", props.modelValue);
        emit("update:show", false);
        emit("sendRegionNames", regionNames.value);
    } else {
        showIndex.value = index + 1;
    }
};

const regionNames = computed(() => {
    let res: any = [];
    regionList.value.forEach((item: any, index) => {
        if (item.length > 0) {
            let selected = false;
            item.forEach((region: any) => {
                if (regionSelected(region.regionId)) {
                    res[index] = region.regionName;
                    selected = true;
                }
            });
            if (selected == false) {
                res[index] = t("请选择地区");
            }
        }
    });
    return res;
});
</script>

<style lang="scss" scoped>
.select-region-main {
    height: 60vh;
    display: flex;
    flex-direction: column;
}
.content {
    position: relative;
    flex: 1;
    display: flex;
    flex-direction: column;
    .region-menu {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 10;
        width: 100%;
        white-space: nowrap;
        overflow: hidden;
        overflow-x: auto;
        .region-menu-item {
            display: inline-block;
            padding: 0 40rpx;
            height: 80rpx;
            line-height: 80rpx;
            background-color: #f7f7f7;
            border: 1rpx solid #e5e5e5;

            font-size: 28rpx;
            &.active {
                background-color: #fff;
                border-bottom: none;
            }
        }
    }

    .region-list {
        top: 67rpx;
        border-top: 1rpx solid #e5e5e5;
        position: absolute;
        width: 100%;
        height: calc(100% - 67rpx);
        overflow: hidden;
        overflow-y: auto;
        .region-list-item {
            padding: 20rpx 20rpx;
            white-space: nowrap;
            display: block;
            font-size: 28rpx;

            &:nth-child(2n) {
                background-color: #f8f8f8;
            }

            &.active {
                color: #e93b3d;
            }
        }
    }
}
</style>
