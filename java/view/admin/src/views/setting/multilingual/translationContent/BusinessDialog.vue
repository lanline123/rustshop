<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="translationName" :label="$t('翻译原文：')" :rules="[{ required: true, message: $t('翻译原文不能为空!') }]">
                        <TigInput type="text" :maxlength="60" v-model="formState.translationName" width="300px;" class="mr10" />
                        <el-button type="primary" @click="translation(formState.translationName)">{{ $t('一键生成翻译') }} </el-button>
                    </el-form-item>
                    <el-form-item prop="language" :label="$t('翻译列表：')">
                        <el-table :data="localesList" style="width: 100%">
                            <el-table-column prop="language" :label="$t('语言')" width="180" />
                            <el-table-column prop="translationValue" :label="$t('翻译内容')">
                                <template #default="{ row }">
                                    <TigInput type="text" v-model="row.translationValue" />
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { useI18n } from "vue-i18n";
import type { FormRules } from "element-plus";
import type { LocalesFormState } from "@/types/multilingual/languagesList.d";
import { getLocales, updateLocales } from "@/api/multilingual/languagesList";

const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
import type { LocalesFilterState } from "@/types/multilingual/languagesList.d";
import { getLocalesList } from "@/api/multilingual/languagesList";
import type { TranslationsFilterState } from "@/types/multilingual/translationContent.d";
import { updateTranslations, getTranslations, updateTranslation } from "@/api/multilingual/translationContent";
import { getCreateTranslation, updateCreateTranslation } from "@/api/multilingual/currencyManagement";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    dataType: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    translationName: {
        type: String,
        default: ""
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<TranslationsFilterState>({
    translationName: props.translationName,
    dataType: props.dataType,
    items: []
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        _getLocales();
    } else {
        loading.value = false;
        _getLocalesList();
    }
});
const _getLocales = async () => {
    try {
        const result = await getCreateTranslation({
            dataType: props.dataType,
            // dataId: props.id,
            translationName: props.translationName
        });
        formState.value.items = result.items;
        _getLocalesList();
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const localesList = ref<LocalesFilterState[]>([]);

const _getLocalesList = async () => {
    try {
        const result = await getLocalesList({
            isEnabled: 1,
            size: -1
        });
        result.records.forEach((item) => {
            item.translationValue = "";
        });
        Object.assign(localesList.value, result.records);
        if (action.value === "detail" && formState.value.items.length > 0) {
            formState.value.items.forEach((translation) => {
                console.log(translation);
                localesList.value.forEach((locale) => {
                    if (translation.localeId === locale.id) {
                        locale.translationValue = translation.translationValue;
                    }
                });
            });
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const translation = async (text: any) => {
    await formRef.value.validate();
    localesList.value.forEach((locale) => {
        let data = {
            code: locale.localeCode,
            text
        };
        _updateTranslation(data, locale);
    });
};
const _updateTranslation = async (data: any, locale: any) => {
    try {
        const result = await updateTranslation(data);
        locale.translationValue = result.translation;
    } catch (error: any) {
        if (error.code == 1001) {
            message.error(error.message);
        } else {
            message.error(error.message);
        }
    }
};
const filterateLocales = (locales: LocalesFilterState[]) => {
    let arr: any = [];
    locales.forEach((item) => {
        let obj = {
            localeId: item.id,
            translationValue: item.translationValue
        };
        arr.push(obj);
    });
    return arr;
};
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateCreateTranslation({
            ...formState.value,
            items: filterateLocales(localesList.value)
        });
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
<style lang="less" scoped></style>
