<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item prop="translationName" :label="$t('原图：')">
                        <div>
                            <div class="item-image-src">
                                <Image :src="imageFormat(translationName)" fit="contain" style="height: 300px; width: 300px" />
                            </div>
                            <div class="extra">{{ $t('如果未设置单独语言图片，则默认使用原图。') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="language" :label="$t('翻译图片：')">
                        <el-table :data="localesList" style="width: 100%">
                            <el-table-column prop="language" :label="$t('语言')" width="180" />
                            <el-table-column prop="translationValue" :label="$t('翻译图片')">
                                <template #default="{ row }">
                                    <div style="padding: 10px 0">
                                        <FormAddGallery v-model:photo="row.translationValue"></FormAddGallery>
                                    </div>
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
import { ref, shallowRef, onMounted, computed } from "vue";
import { useI18n } from "vue-i18n";
import { FormAddGallery } from "@/components/gallery";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { imageFormat } from "@/utils/format";
import { Image } from "@/components/image";

const { t } = useI18n();
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
import type { LocalesFilterState } from "@/types/multilingual/languagesList.d";
import { getLocalesList } from "@/api/multilingual/languagesList";
import type { TranslationsFilterState } from "@/types/multilingual/translationContent.d";
import { getCreateTranslation, updateCreateTranslation } from "@/api/multilingual/currencyManagement";

/** 翻译图片行：在语言项上附加翻译来源（1=AI翻译，2=AI消除） */
interface BusinessLocaleItem extends LocalesFilterState {
    translationSource?: number;
}

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
const localesList = ref<BusinessLocaleItem[]>([]);
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
            formState.value.items.forEach((translation: any) => {
                localesList.value.forEach((locale) => {
                    if (translation.localeId === locale.id) {
                        locale.translationValue = translation.translationValue;
                        locale.translationSource = translation.translationSource;
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
const filterateLocales = (locales: BusinessLocaleItem[]) => {
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
    width: 300px;
    height: 300px;
    border: 1px double #ddd;
}

.ai-actions {
    display: flex;
    gap: 8px;
    margin-top: 12px;
}

.ai-table-actions {
    display: inline-flex;
    align-items: center;
    white-space: nowrap;
}
</style>
