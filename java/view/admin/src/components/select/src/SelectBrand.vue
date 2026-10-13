/**
 * @file 品牌选择器组件
 * @description 带首字母快速筛选的品牌下拉选择器
 * @features
 * - 首字母快速筛选品牌
 * - 支持搜索过滤
 * - 支持单选和多选模式
 * - 懒加载品牌数据
 */
<template>
    <el-select
        :multiple="multiple"
        v-model="brandId"
        :allowClear="true"
        :fieldNames="fieldNames"
        :filter-option="filterOption"
        clearable
        filterable
        :placeholder="$t('选择品牌')"
        popper-class="brand-select"
        show-search
        @change="onChange"
        @visible-change="loadBrand"
    >
        <template #default>
            <div class="option">
                <div v-if="firstWordList" class="all_brand">
                    <ul>
                        <li :class="firstWord == '全部' ? 'current' : ''">
                            <a @click="changeBrandWord($event, '全部')">{{ $t('全部') }}</a>
                        </li>
                        <li v-for="(item, key) in firstWordList" :class="firstWord == item ? 'current' : ''">
                            <a @click="changeBrandWord($event, item)">{{ item }}</a>
                        </li>
                    </ul>
                    <el-divider style="margin: 4px 0 12px" />
                </div>
                <div class="right">
                    <div class="scroll-container">
                        <el-option v-for="item in options" :key="item.brandId" :label="item.brandName" :value="item.brandId">
                            {{ item.brandName }}
                        </el-option>
                    </div>
                </div>
            </div>
        </template>
        <template #empty>
            <a-spin :spinning="!loaded" size="small" style="width: 100%; padding-top: 150px" />
        </template>
    </el-select>
</template>
<script lang="ts" setup>
import { onMounted, ref, PropType } from "vue";
import { message } from "ant-design-vue";
import { getBrandSearch } from "@/api/product/brand";
import { BrandFilterState } from "@/types/product/brand";
// 传值
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    // brandId: { type: [Number], default: 0 },
    //multiple 是否多选，可直接写在父组件
    multiple: { type: Boolean, default: false },
    firstWordList: { type: Array as PropType<string[]>, default: () => [] },
    brandList: { type: Array as PropType<BrandFilterState[]>, default: () => [] },
    allBrandList: { type: Array as PropType<BrandFilterState[]>, default: () => [] }
});
const emit = defineEmits(["update:brandId", "update:brandIds", "update:brandForm","onChange"]);
const brandId = defineModel<number|string|number[]>("brandId");
const brandForm = defineModel<any>("brandForm");
const fieldNames = { label: "brandName", value: "brandId" };
const filterOption = (input: string, option: any) => {
    return option.brandName.toLowerCase().indexOf(input.toLowerCase()) >= 0;
};
const onChange = (e: any) => {
    options.value.map((subArray: any) => {
        if (subArray.brandId == e) {
            brandForm.value = subArray;
        }
    });
    if (!props.multiple) {
        emit("update:brandId", e);
    } else {
        emit("update:brandIds", e);
    }
    firstWord.value = t('全部');
    emit("onChange", e);
};
onMounted(() => {
    if (props.brandList.length > 1 && props.firstWordList.length > 1 && props.allBrandList.length > 1) {
        options.value = props.brandList;
        firstWordList.value = props.firstWordList;
        allBrandList.value = props.allBrandList;
    } else {
        loadBrand();
    }
});

const loaded = ref(false);
const firstWord = ref(t('全部'));
//首字母列表
const firstWordList = ref<string[]>([]);
// 实际数据
const options = ref<BrandFilterState[]>([]);
// 全部品牌
const allBrandList = ref<BrandFilterState[]>([]);
const loadBrand = async () => {
    try {
        const result = await getBrandSearch();
        firstWordList.value = result.firstWordList;
        options.value = result.brandList;
        allBrandList.value = result.brandList;
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loaded.value = true;
    }
};

const changeBrandWord = (e: any, keywords: string) => {
    firstWord.value = keywords;
    if (keywords == t('全部')) {
        options.value = allBrandList.value;
        return;
    }
    options.value = allBrandList.value.filter((item) => {
        return item.firstWord == keywords;
    });
};
</script>

<style lang="less" scoped>
.option {
    display: flex;
    overflow: hidden;
    /* 确保所有溢出都在子元素中处理 */
    max-height: 590px !important;
    height: 320px;

    .all_brand {
        width: 50px;
        overflow: hidden;
        border-right: 1px solid #eee;
    }

    .right {
        overflow: auto;
        flex: 1;
    }
}

.all_brand ul {
    width: 80px;
    display: flex;
    flex-wrap: wrap;
    height: 100%;
    box-sizing: border-box;
    overflow-y: auto;
    overflow-x: hidden;
    justify-content: left;
    padding-top: 5px;
}

.all_brand ul li {
    width: 50px;
    box-sizing: border-box;
    padding: 0 5px;
}

.all_brand li a {
    width: 100%;
    padding: 3px 0;
    text-align: center;
    display: inline-block;
    border-radius: 3px;
    line-height: 20px;
    color: #666;
}

.all_brand li.current a {
    color: var(--tig-primary);
}

.all_brand li a:hover {
    background: #f4f4f4;
}
</style>
