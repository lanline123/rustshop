<template>
    <div style="width: 100%" v-if="type == 'input'">
        <TigInput
            classType="tig-form-input"
            v-model="modelValue"
            :disabled="disabled"
            :placeholder="placeholder"
            :width="width"
            :maxlength="maxlength"
            :showWordLimit="showWordLimit"
            :suffix-icon="suffixIcon"
            @blur="emit('blur')"
            @input="(val: string) => emit('input', val)"
        >
        </TigInput>
    </div>
    <div style="width: 100%" v-if="type == 'complete'">
        <el-autocomplete
            v-model="modelValue"
            clearable
            :fetch-suggestions="querySearch"
            :placeholder="placeholder"
            @blur="emit('blur')"
            @input="(val: string) => emit('input', val)"
            :disabled="disabled"
        >
        </el-autocomplete>
    </div>
    <div class="flex flex-align-center" style="width: 100%" v-if="type == 'select'">
        <el-select
            class="input-tag-warp"
            style="width: 390px !important"
            :teleported="false"
            v-model="tagValue"
            multiple
            filterable
            allow-create
            default-first-option
            :reserve-keyword="false"
            placeholder=""
            popper-class="input-tag-popper-warp"
            @change="onChange"
            :filter-method="onfilter"
            @keyup.enter="onEnter"
        >
        </el-select>
    </div>
    <div class="multilingual-box" v-if="type == 'text'">
        <div class="multilingual-text">
            {{ modelValue }}
        </div>
    </div>
    <div class="multilingual-editor-box" v-if="type == 'editor'">
    </div>
    <div class="multilingual-textarea-box" v-if="type == 'textarea'">
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { ref, onMounted, watch, PropType, VNode } from "vue";
import { message } from "ant-design-vue";
import { getCreateTranslation, updateCreateTranslation } from "@/api/multilingual/currencyManagement";
import { isOverseas } from "@/utils/version";

const props = defineProps({
    disabled: {
        type: Boolean,
        default: false
    },
    placeholder: {
        type: String,
        default: ""
    },
    dataType: {
        type: Number,
        default: 2
    },
    dataId: {
        type: Number,
        default: 0
    },
    maxlength: {
        type: Number,
        default: 999999
    },
    showWordLimit: {
        type: Boolean,
        default: false
    },
    type: {
        type: String,
        default: "input"
    },
    splitString: {
        type: String,
        value: ","
    },
    width: {
        type: String,
        default: "100%"
    },
    suffixIcon: {
        type: [String, Function, null, Object] as PropType<string | (() => VNode) | any>,
        default: null
    },
    completeTip: {
        type: Array,
        default: () => []
    },
    translationButtonText: {
        type: String,
        default: "多语言翻译"
    }
});
const emit = defineEmits(["update:modelValue", "blur", "input"]);
const modelValue = defineModel<string>("modelValue", { type: String, default: "" });
const translationName = ref<string>("");
const isTranslation = ref(false);
const hasInitTranslationLoaded = ref(false);
const _getCreateTranslation = async () => {
    try {
        const result = await getCreateTranslation({
            dataType: props.dataType,
            // dataId: props.dataId,
            translationName: modelValue.value
        });
        if (result.items.length <= 0) {
            isTranslation.value = true;
        } else {
            translationName.value = result.translationName;
            isTranslation.value = false;
        }
    } catch (error: any) {
        message.error(error.message);
    }
};
const tryInitTranslationFetch = (value: string) => {
    if (hasInitTranslationLoaded.value) return;
};
// 监听modelValue变化
watch(
    modelValue,
    (newVal: string) => {
        if (props.maxlength > 0 && newVal && newVal.length > props.maxlength) {
            modelValue.value = newVal.slice(0, props.maxlength);
            return;
        }
        tryInitTranslationFetch(newVal);
        if (newVal != translationName.value) {
            isTranslation.value = true;
        } else {
            isTranslation.value = false;
        }
    },
    { immediate: true }
);
const tagValue = ref();
const splitString = ref();
splitString.value = props.splitString || ",";
if (props.type == "select") {
    if (typeof modelValue.value === "string") {
        tagValue.value = modelValue.value ? modelValue.value.split(splitString.value) : [];
    } else {
        tagValue.value = modelValue.value;
    }
}
const onChange = (value: any) => {
    if (typeof modelValue.value === "string") {
        emit("update:modelValue", tagValue.value.join(splitString.value));
    } else {
        emit("update:modelValue", tagValue.value);
    }
};
const filterInput = ref();
const onfilter = (e: any) => {
    if (e) filterInput.value = e;
};
const onEnter = () => {
    if (tagValue.value.indexOf(filterInput.value) === -1) {
        if (filterInput.value) {
            tagValue.value.push(filterInput.value);
            filterInput.value = "";
        }
    }
};

interface RestaurantItem {
    value: string;
    // link: string
}

const completeTip = ref(props.completeTip);
const restaurants = ref<RestaurantItem[]>([]);

const querySearch = (queryString: string, cb: any) => {
    const results = queryString ? restaurants.value.filter(createFilter(queryString)) : restaurants.value;
    cb(results);
};
const createFilter = (queryString: string) => {
    return (restaurant: RestaurantItem) => {
        return restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0;
    };
};

watch(
    () => props.completeTip,
    (newVal) => {
        restaurants.value = [];
        if (newVal && Array.isArray(newVal)) {
            newVal.forEach((item: any) => {
                if (item) {
                    restaurants.value.push({ value: typeof item === "string" ? item : String(item) });
                }
            });
        }
    },
    { immediate: true, deep: true }
);

onMounted(() => {
    tryInitTranslationFetch(modelValue.value);
});
</script>

<style lang="less" scoped>
:deep(.el-button) {
    padding: 3px 15px;
}
.multilingual-box {
    position: relative;
    width: 100%;
    .multilingual-text {
        width: 100%;
        white-space: nowrap; /* 防止文字换行 */
        overflow: hidden; /* 隐藏溢出的文字 */
        text-overflow: ellipsis; /* 显示省略号来代替溢出的文字 */
    }
    .multilingual-icon {
        position: absolute;
        top: -8px;
        right: -8px;
        z-index: 100;
    }
}
.multilingual-editor-box {
    position: relative;
    width: 100%;
    .multilingual-editor-icon {
        position: absolute;
        z-index: 10;
        right: 10px;
        bottom: 40px;
    }
}
.multilingual-textarea-box {
    position: absolute;
    right: 20px;
    bottom: var(--multilingual-textarea-bottom, 10px);
    z-index: 10;
    .multilingual-textarea-icon {
        position: static;
    }
}
</style>
