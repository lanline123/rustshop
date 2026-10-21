<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('添加分类')" prop="brandName">
                        <DynamicList
                            v-model:modelValue="formState.categoryList"
                            :templateList="['categoryId', 'categoryName']"
                            :templateListType="['number', 'string']"
                            buttonName="增加"
                            buttonType="primary"
                            style="width: 100%"
                        >
                            <template #dynamicSlot="{ item }">
                                <div class="dynamic-div">
                                    <SelectCategory
                                        v-if="!loading"
                                        v-model:categoryId="(item as CategoryListItem).categoryId"
                                        :multiple="false"
                                        style="width: 100%"
                                    ></SelectCategory>
                                    <span>{{ $t('重命名：') }}</span>
                                    <TigInput classType="tig-form-input" v-model="(item as CategoryListItem).categoryName"></TigInput>
                                </div>
                            </template>
                        </DynamicList>
                        <div class="extra">{{ $t('提示：只能设置为一级或二级分类，且必须含有一个一级分类，如果存在二级分类，则必须存在该二级分类上一级分类；') }}</div>
                        <div class="extra">{{ $t('重命名为空则显示原分类名称，重命名的作用为方便排列') }}</div>
                        <div class="extra">{{ $t('设置规则如下：') }}</div>
                        <div class="extra">{{ $t('1、如果添加的分类为一级分类A、B、C，则抽屉一级会显示：A/B/C三个链接，二级为A、B、C下的所有二级分类') }}</div>
                        <div class="extra"> {{ $t('2、如果添加的分类为一级分类A和A下面的二级分类a1、a2，则，会显示A/a1/a2三个链接，二级为A下的所有二级分类（包含了a1、a2）') }} </div>
                        <div class="extra">{{ $t('3、如果重命名为“-”符号，则不会显示') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('热门品牌')" prop="brandList">
                        <DynamicList
                            v-model:modelValue="formState.brandList"
                            :templateList="['brandId']"
                            :templateListType="['number']"
                            buttonName="增加"
                            buttonType="primary"
                            style="width: 100%"
                        >
                            <template #dynamicSlot="{ item }">
                                <div class="dynamic-div">
                                    <SelectBrand v-model="(item as CategoryListItem).brandId" style="width: 100%"></SelectBrand>
                                </div>
                            </template>
                        </DynamicList>
                    </el-form-item>
                    <el-form-item :label="$t('ICO编码')" prop="floorIcoFont">
                        <SelectIco v-model="formState.floorIcoFont"></SelectIco>
                        <div class="extra">{{ $t('请先在本系统设置中的"接口设置"中设置"ico图标库地址"') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('是否显示')" prop="isShow">
                        <el-radio-group v-model="formState.isShow">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { CategoryListItem, PcCatFloorFormState } from "@/types/decorate/pcCatFloor.d";
import { getPcCatFloor, updatePcCatFloor } from "@/api/decorate/pcCatFloor";
import DynamicList from "@/components/list/src/DynamicList.vue";
import { SelectBrand, SelectCategory } from "@/components/select";
import SelectIco from "@/components/select/src/SelectIco.vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0 // 如果在您的应用中 0 有特殊意义，请选择一个合适的默认值
    },
    act: {
        type: String,
        default: "" // 默认为空字符串
    },
    isDialog: {
        type: Boolean,
        default: false // 默认为 false
    }
});

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<PcCatFloorFormState>({
    isShow: 1,
    sortOrder: 50
});
const fetchPcCatFloor = async () => {
    try {
        const result = await getPcCatFloor(action.value, { id: id.value });
        Object.assign(formState.value, result);
        if (result.categoryIds && result.categoryNames && result.categoryIds.length === result.categoryNames.length) {
            formState.value.categoryList = result.categoryIds.map((id, index) => {
                return {
                    categoryId: Number(id),
                    categoryName: result.categoryNames?.[index] || ""
                };
            });
            if (result.brandIds && result.brandIds.length > 0) {
                formState.value.brandList = result.brandIds.map((id, index) => {
                    return {
                        brandId: Number(id)
                    };
                });
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit('close');
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchPcCatFloor();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        processCategoryList();
        processBrandList();
        const result = await updatePcCatFloor(operation, { id: id.value, ...formState.value });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

function processCategoryList() {
    formState.value.categoryIds = formState.value.categoryIds || [];
    formState.value.categoryNames = formState.value.categoryNames || [];
    formState.value.categoryIds.length = 0;
    formState.value.categoryNames.length = 0;
    formState.value.categoryList?.forEach((item: CategoryListItem) => {
        if (typeof item.categoryId === "number" && typeof item.categoryName === "string") {
            formState.value.categoryIds?.push(item.categoryId);
            formState.value.categoryNames?.push(item.categoryName);
        }
    });
}

// 分离处理 brandList 的逻辑
function processBrandList() {
    formState.value.brandIds = formState.value.brandIds || [];
    formState.value.brandIds.length = 0;
    formState.value.brandList?.forEach((item: CategoryListItem) => {
        if (typeof item.brandId === "number") {
            formState.value.brandIds?.push(item.brandId);
        }
    });
}

// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
