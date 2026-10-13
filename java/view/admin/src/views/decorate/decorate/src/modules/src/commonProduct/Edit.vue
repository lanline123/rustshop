<template>
    <!-- 标题设置 -->
    <div class="dec-edit-group">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('商品池选择') }}</div>
            <div class="value"></div>
        </div>
        <div class="dec-edit-group-con">
            <el-radio-group class="dec-radio-group" v-model="products.productSelectType">
                <el-radio-button :value="1">{{ $t('选商品') }}</el-radio-button>
                <el-radio-button :value="2">{{ $t('按分类') }}</el-radio-button>
                <el-radio-button :value="3">{{ $t('按标签') }}</el-radio-button>
            </el-radio-group>
        </div>
    </div>
    <div class="dec-edit-group dec-edit-group-block" v-if="products.productSelectType == 1">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('添加商品') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-goods-group">
                <ProductSelect v-model:ids="products.productIds" :isMultiple="true"></ProductSelect>
            </div>
        </div>
        <div class="dec-edit-group-desc">
            <div>{{ $t('提示：您可以通过拖拽进行商品排序') }}</div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="products.productSelectType == 2">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('选择分类') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-goods-group">
                <SelectCategory v-model:categoryId="products.productCategoryId"></SelectCategory>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="products.productSelectType == 3">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('选择标签') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-select-group">
                <ProductTagSelect v-model="products.productTag"></ProductTagSelect>
            </div>
        </div>
    </div>
    <div class="dec-edit-group" v-if="products.productSelectType == 2 || products.productSelectType == 3">
        <div class="dec-edit-group-title">
            <div class="label">{{ $t('商品数量') }}</div>
        </div>
        <div class="dec-edit-group-con">
            <div class="dec-input-group">
                <div class="flex" style="justify-content: flex-end; flex-wrap: wrap">
                    <el-input
                        type="number"
                        style="width: 100px"
                        :min="props.numberMin"
                        :max="props.numberMax"
                        :step="props.numberStep"
                        v-model="products.productNumber"
                        @change="handleInput"
                        placeholder=""></el-input>
<!--                    <div v-if="products.productNumber! > 20" class="yellow" style="width: 100%; text-align: right; margin-top: 10px">-->
<!--                        后台编辑时最多只显示20个商品-->
<!--                    </div>-->
                </div>
            </div>
        </div>
    </div>
    <div class="dec-divider-line"></div>
</template>
<script lang="ts" setup>
import { ref, onMounted } from "vue";
import { ModuleProductsType } from "@/types/decorate/decorate.d";
import { SelectCategory } from "@/components/select";
import { ProductSelect, ProductTagSelect } from "@/components/decorate";
const props = defineProps({
    numberMin: { type: Number, default: 1 },
    numberMax: { type: Number, default: null },
    numberStep: { type: Number, default: 1 },
});
const products = defineModel<ModuleProductsType>("modelValue", {
    default: () => ({
        productSelectType: 1,
        productIds: [],
    }),
});
const defaultModule = {};
const dealDefault = () => {
    for (let i in defaultModule) {
        if (typeof (products.value as any)[i] === "undefined") {
            (products.value as any)[i] = (defaultModule as any)[i];
        }
    }
};
const handleInput = (newValue: number) => {
    // 修正输入值为最接近的 step 值
    const step = props.numberStep;
    const correctedValue = Math.round(newValue / step) * step;
    products.value.productNumber = correctedValue;
};
onMounted(async () => {
    dealDefault();
});
</script>
