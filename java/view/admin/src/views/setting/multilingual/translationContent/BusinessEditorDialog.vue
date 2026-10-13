<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-tabs v-model="activeName" type="card" class="demo-tabs" @tab-click="handleClick">
                    <el-tab-pane :label="$t('原文')" :name="-1">
                        <Editor v-model:html="formState.translationName" height="400px"></Editor>
                    </el-tab-pane>
                    <el-tab-pane v-for="item in localesList" :label="item.language" :name="item.id">
                        <Editor v-model:html="item.translationValue" height="400px"></Editor>
                    </el-tab-pane>
                </el-tabs>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { ref, shallowRef, onMounted } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { Editor } from "@/components/editor";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
import type { LocalesFilterState } from "@/types/multilingual/languagesList.d";
import { getLocalesList } from "@/api/multilingual/languagesList";
import type { TranslationsFilterState } from "@/types/multilingual/translationContent.d";
import { getCreateTranslation, updateCreateTranslation } from "@/api/multilingual/currencyManagement";
const activeName = ref(-1);
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

const handleClick = (tab: number) => {
    activeName.value = tab;
};

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
            // dataId: 0,
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
    try {
        emit("update:confirmLoading", true);
        const result = await updateCreateTranslation({
            // dataId: 0,
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
<style lang="less" scoped>
.item-image-src {
    max-width: 600px;
    max-height: 250px;
    overflow: auto;
    .item-image {
        border: 1px double #ddd;
    }
}
.lyecs-form-table {
    :deep(.el-tabs__content) {
        padding-top: 0px !important;
    }
}
</style>
