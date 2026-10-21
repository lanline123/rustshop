<template>
    <div>
        <div class="region-con" ref="cascaderConRef"></div>
        <Cascader ref="cascaderRef" v-model:value="ids" :open="open" multiple :options="regionList" :placeholder="$t('请添加地区')"
            :fieldNames="{ label: 'regionName', value: 'regionId', children: 'children' }" :getPopupContainer="getCascaderRef" @change="onChange">
            <template #tagRender="data">
            </template>
            <div></div>
        </Cascader>
        <div class="region-checkAll"> <el-button type="primary" size="small" @click="onCheckAll">{{ $t('全选') }}</el-button><el-button size="small" @click="onClear">{{ $t('清空') }}</el-button></div>
    </div>
</template>
<script lang="ts" setup>
import { ref, toRef, toRefs, computed } from "vue";
import { message, Cascader, Checkbox } from "ant-design-vue";
import { useRegionStore } from "@/store/region";
import type { CascaderProps } from 'ant-design-vue';
import { cloneDeep } from 'lodash';


// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const props = defineProps({
    ids: Array,
    itemIndex: Number,
    index: Number,
    isDialog: Boolean,
    other: {
        type: Object,
        default() {
            return {};
        }
    },
});
// 获取地区，深拷贝防止修改store
const region:any = useRegionStore();

const regionList = cloneDeep(region.allRegion);
// 获取同级所有已设置的地区
const seletedIdsArray = props.other.map((e:any) => e.regionData.areaRegions);
// 地区排除自身的，排除为空的，然后flat平铺
const disabledIds = ref(seletedIdsArray.filter((_v:any, i:number) => i !== props.itemIndex && _v.length > 0).flat());
// 取地区最后一个元素
disabledIds.value = disabledIds.value.map((subArr:any) => subArr[subArr.length - 1]);

const regionNames = ref<any>([]);
const cascaderRef = ref(null)
function setDisabled(node:any, _ids:any) {
    if (node.children) {
        node.children.forEach((child:any) => setDisabled(child, _ids));
    }
    if (_ids.includes(node.regionId)) {
        node.disabled = true;
    }
}
// 设置禁选项
regionList.forEach((node:any) => setDisabled(node, disabledIds.value));

const onChange: CascaderProps['onChange'] = (_value, selectedOptions) => {
    regionNames.value = selectedOptions.map((subArr:any) => subArr[subArr.length - 1].regionName);
};

const open = ref(true);
const cascaderConRef = ref<any>(null);
const ids = ref(<number[][]>props.ids);
const getCascaderRef = () => {
    return cascaderConRef.value;
}

const onCheckAll = () => {
    ids.value = regionList.filter((item:any) => !item.disabled).map((item:any) => [item.regionId]);
    regionNames.value = regionList.filter((item:any) => !item.disabled).map((item:any) => item.regionName);
}
const onClear = () => {
    ids.value = [];
    regionNames.value = [];
}

// 表单通过验证后提交
const onSubmit = async () => {
    emit('update:confirmLoading', true);
    emit('submitCallback', {
        regionIds: ids.value,
        regionNames: regionNames.value,
        itemIndex: props.itemIndex,
        index: props.index
    });
};
// 表单提交
const onFormSubmit = () => {
    onSubmit()
};
defineExpose({ onFormSubmit });

</script>
<style lang="less" scoped>
.region-con {
    width: 100%;
    height: 300px;
}

.region-checkAll {
    padding-left: 0;
    padding-top: 10px;
}

.region-con :deep(.ant-select-dropdown) {
    display: block;
    position: relative;
    top: 0 !important;
    left: 0 !important;
    height: 300px !important;
    box-shadow: none;
    border: 1px solid #eee;
    border-radius: 4px;

    .ant-cascader-menu {
        width: 220px;
        flex: none;
        height: 300px !important;
    }
}
</style>
