<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item
                        :rules="[
                            { required: true, message: t('地区名称不能为空!') },
                            { max: 200, message: t('地区名称最多200个字符'), trigger: 'blur' }
                        ]"
                        :label="$t('地区名称')"
                        prop="regionName"
                    >
                        <TigInput classType="tig-form-input" v-model="formState.regionName" :maxlength="200" />
                    </el-form-item>
                    <el-form-item :label="$t('上级地区')" prop="parentName">
                        <TigInput classType="tig-form-input" v-model="formState.parentName" disabled :placeholder="$t('顶级地区')" />
                    </el-form-item>
                    <el-form-item :label="$t('是否热门')" prop="isHot">
                        <el-radio-group v-model="formState.isHot" style="width: 100%">
                            <el-radio :value="1">{{ $t("是") }}</el-radio>
                            <el-radio :value="0">{{ $t("否") }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" :min="0" v-model="formState.sortOrder" />
                        <div class="extra">{{ $t("默认值为50，数值越小，排序越靠前") }}</div>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import type { RegionFormState } from "@/types/setting/region";
import { getRegion, getRegionList, updateRegion } from "@/api/setting/region";
import { useRegionStore } from "@/store/region";
import { isOverseas } from "@/utils/version";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const region: any = useRegionStore();
// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean,
    regionId: {
        type: Number,
        default: 0
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));

const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<RegionFormState>({
    isHot: 0,
    firstWord: "",
    standardCode: "",
    sortOrder: 50
});
const fetchRegion = async () => {
    try {
        const result = await getRegion(action.value, { id: id.value });
        Object.assign(formState.value, result);
        formState.value.sortOrder = Number(formState.value.sortOrder ?? 50);
        await fetchParentRegionName(formState.value.parentId);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const fetchParentRegionName = async (parentId?: number) => {
    if (!parentId) {
        formState.value.parentName = t("顶级地区");
        return;
    }
    try {
        const result = await getRegionList({ regionId: parentId });
        formState.value.parentName = result.records?.[0]?.regionName || t("顶级地区");
    } catch (error: any) {
        message.error(error.message);
    }
};
const fetchRegionList = async () => {
    try {
        const result = await getRegionList({ regionId: props.regionId });
        if (result.records && result.records.length > 0) {
            formState.value.parentId = result.records[0].regionId;
            formState.value.parentName = result.records[0].regionName;
        } else {
            formState.value.parentId = 0;
            formState.value.parentName = t("顶级地区");
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchRegion();
    } else {
        loading.value = false;
        fetchRegionList();
    }
});
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateRegion(operation, {
            ...(operation === "update" ? { id: id.value } : {}),
            regionName: formState.value.regionName,
            parentId: operation === "create" ? props.regionId : formState.value.parentId,
            firstWord: formState.value.firstWord,
            isHot: formState.value.isHot,
            sortOrder: formState.value.sortOrder,
        });
        region.clearRegion();
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};
defineExpose({ onFormSubmit });
</script>
