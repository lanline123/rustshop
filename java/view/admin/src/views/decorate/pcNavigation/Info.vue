<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, message: '名称不能为空!' }]" :label="$t('名称')" prop="title">
                        <TigInput classType="tig-form-input" v-model="formState.title" />
                    </el-form-item>
                    <el-form-item :label="$t('链接地址')" prop="link">
                        <SelectLink v-model="formState.link" decorateType="pc"></SelectLink>
                    </el-form-item>
                    <el-form-item  :label="$t('ICO编码')" prop="icon">
                        <SelectIco v-model="formState.icon"></SelectIco>
                        <div class="extra">{{ $t('请先在本系统设置中的"接口设置"中设置"ico图标库地址"') }}</div>
                    </el-form-item>
                    <el-form-item v-if="type===2" :label="$t('上级导航')" prop="parentId">
                        <el-select style="width: 100%" v-model="formState.parentId">
                            <el-option :label="$t('顶级导航')" :value="0" :key="0"></el-option>
                            <el-option v-for="item in options" :label="item.title" :value="item.id" :key="item.id"></el-option>
                        </el-select>
                        <div class="extra">{{ $t('请选择相对应位置的导航，如果父导航为中间导航，那该导航必须同样设置为中间导航才会生效；') }}</div>
                        <div class="extra">{{ $t('默认不选则表示为顶级导航') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('位置')" prop="typeName">
                        <TigInput classType="tig-form-input" disabled v-model="formState.typeName" />
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" />
                    </el-form-item>
                    <el-form-item :label="$t('是否显示')" prop="isShow">
                        <el-radio-group v-model="formState.isShow">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('是否新窗口')" prop="isBlank">
                        <el-radio-group v-model="formState.isBlank">
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
import { PcNavigationFormState } from "@/types/decorate/pcNavigation";
import { getParentNav, getPcNavigation, updatePcNavigation } from "@/api/decorate/pcNavigation";
import { SelectLink } from "@/components/select";
import SelectIco from "@/components/select/src/SelectIco.vue";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    type: {
        type: Number,
        default: 0
    },
    typeName: {
        type: String,
        default: ""
    },
    act: {
        type: String,
        default: ""
    },
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<PcNavigationFormState>({
    type: props.type,
    isShow:1,
    isBlank:0
});
const fetchPcNavigation = async () => {
    try {
        const result = await getPcNavigation(action.value, { id: id.value });
        Object.assign(formState.value, result);
        if (operation === "create") {
            formState.value.type = props.type;
            formState.value.typeName = props.typeName;
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const options: any = ref([]);
const getOptions = async (value: number) => {
    const result = await getParentNav({ type: value });
    options.value.push(...result);
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchPcNavigation();
    } else {
        loading.value = false;
    }
    getOptions(formState.value.type);
});

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updatePcNavigation(operation, { id: id.value, ...formState.value });
        emit("submitCallback", {parentId:formState.value.parentId});
        message.success(t('操作成功'));
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
