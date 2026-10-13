/**
 * @file 商品范围选择器组件
 * @description 用于营销活动等场景选择商品适用范围
 * @features
 * - 支持全部商品/指定商品/排除商品三种模式
 * - 可扩展支持分类和品牌筛选
 * - 集成分类/品牌/商品选择器
 * - 支持批量添加品牌标签
 */
<template>
    <div class="range-con">
        <el-space wrap>
            <el-radio-group v-model="rangeType" @change="onChange" :disabled="props.disabled">
                <el-radio :value="0">{{ $t('全部商品') }}</el-radio>
                <!-- <el-radio :value="1">以下分类</el-radio>
                <el-radio :value="2">以下品牌</el-radio> -->
                <el-radio :value="3">{{ $t('以下商品') }}</el-radio>
                <el-radio :value="4">{{ $t('不含以下商品') }}</el-radio>
            </el-radio-group>
        </el-space>
    </div>
    <div class="tags" v-if="type == 'productBatch'">
        <el-tag v-for="(tag, index) in tagList" :key="tag" closable @close="onClose(index)">
            {{ tag.brandName }}
        </el-tag>
    </div>
    <div v-if="rangeType == 1">
        <SelectCategory :multiple="true" style="width: 260px" v-model:categoryId="rangeIds" @update:categoryId="selectChange"> </SelectCategory>
    </div>
    <div v-if="rangeType == 2">
        <SelectBrand v-if="type == 'productBatch'" style="width: 260px" v-model:brandForm="brandForm"> </SelectBrand>
        <SelectBrand v-else style="width: 260px" v-model:brandId="rangeIds"> </SelectBrand>
        <el-button v-if="type == 'productBatch'" class="addBtn" type="primary" @click="add">{{ $t('添加') }}</el-button>
    </div>
    <div v-if="rangeType == 3" style="width: 100%;">
        <SelectProduct v-model:ids="rangeIds" :isSelf="props.isSelf" :isFilterPresaleProduct="props.isFilterPresaleProduct" :apiList="props.apiList" :otherParams="props.otherParams"></SelectProduct>
    </div>
    <div v-if="rangeType == 4" style="width: 100%;">
        <SelectProduct v-model:ids="rangeIds" :isSelf="props.isSelf" :isFilterPresaleProduct="props.isFilterPresaleProduct" :apiList="props.apiList" :otherParams="props.otherParams"></SelectProduct>
    </div>
</template>
<script setup lang="ts">
import { ref, nextTick } from "vue";
import { SelectCategory, SelectBrand, SelectProduct } from "@/components/select";
import { message } from "ant-design-vue";
// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    disabled: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: ""
    },
    //是否自营商品
    isSelf: {
        type: Number,
        default: 0
    },
    // 是否过滤预售商品
    isFilterPresaleProduct: {
        type: Boolean,
        default: false
    },
    // 透传商品列表查询参数
    otherParams: {
        type: Object,
        default: () => ({})
    },
    apiList: {
        type: Function,
        default: null
    }
});
const tagList = ref<any>([]);
const brandForm = ref<any>({});
const rangeIds = defineModel<any>("rangeIds");
const rangeType = defineModel<Number>("rangeType");
const emit = defineEmits(["update:rangeType", "update:rangeIds"]);
const onChange = (e: any) => {
    rangeType.value = e;
    tagList.value = [];
    if (e == 2 && props.type != "productBatch") {
        rangeIds.value = "";
    } else {
        rangeIds.value = [];
    }
};
const selectChange = (e: any) => {
    rangeIds.value = e;
};
const onClose = (index: number) => {
    tagList.value.splice(index, 1);
    rangeIds.value = screenBrand(tagList.value);
};
const add = () => {
    const isContain = tagList.value.some((itemB: any) => {
        return itemB.brandId === brandForm.value.brandId && itemB.brandName === brandForm.value.brandName;
    });
    if (!isContain) {
        tagList.value.push(brandForm.value);
        rangeIds.value = screenBrand(tagList.value);
    } else {
        message.error(t('已存在相同品牌'));
    }
};
const screenBrand = (tagList: Object[]) => {
    let arr: number[] = [];
    tagList.some((item: any) => {
        arr.push(item.brandId);
    });
    return arr;
};
</script>

<style lang="less" scoped>
.range-con {
    margin-bottom: 10px;
    width: 100%;
    display: flex;
    align-items: center;
}
.addBtn {
    margin-left: 10px;
}
.tags {
    width: 100%;
    margin-bottom: 10px;
    .el-tag {
        margin-right: 10px;
    }
}
</style>
