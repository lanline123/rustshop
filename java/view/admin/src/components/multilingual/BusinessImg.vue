<template>
    <div class="multilingual-item-content" :style="style" v-if="modelValue">
        <slot></slot>
        <DialogForm
            isDrawer
            @okCallback="_getCreateTranslation"
            :title="$t('内容翻译')"
            width="800px"
            path="setting/multilingual/translationContent/BusinessImgDialog"
            :params="{ act: isTranslation ? 'add' : 'detail', id: dataId, dataType: dataType, translationName: effectiveTranslationName }"
        >
        </DialogForm>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { ref, onMounted, watch, computed } from "vue";
import { message } from "ant-design-vue";
import { getCreateTranslation, updateCreateTranslation } from "@/api/multilingual/currencyManagement";
import { isOverseas } from "@/utils/version";
import { imageFormat } from "@/utils/format";
import { CircleCloseFilled } from "@element-plus/icons-vue";
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
    data: {
        //额外传参，添加相册返回的同时会回传
        type: Object,
        default: {}
    },
    style: {
        type: Object,
        default: {}
    },
    maxlength: {
        type: Number,
        default: 999999
    },
    type: {
        type: String,
        default: "image"
    },
    picThumb: {
        type: String,
        default: ""
    },
    splitString: {
        type: String,
        value: ","
    },
    del: {
        type: Boolean,
        default: true
    },
    edit: {
        type: Boolean,
        default: true
    },
    translationName: {
        type: String,
        default: undefined
    }
});
const emit = defineEmits(["update:modelValue", "Edit", "Delete"]);
const modelValue = defineModel<string>("modelValue", { type: String, default: "" });
const latestTranslationName = ref<string>("");
const isTranslation = ref(false);
/** 优先使用外部传入的 translationName，未传时回退到图片地址 */
const effectiveTranslationName = computed(() => (props.translationName !== undefined ? props.translationName : modelValue.value));
const _getCreateTranslation = async () => {
    const name = effectiveTranslationName.value;
    if (!name) {
        latestTranslationName.value = "";
        isTranslation.value = true;
        return;
    }
    try {
        const result = await getCreateTranslation({
            dataType: props.dataType,
            // dataId: 0,
            translationName: name
        });
        if (result.items.length <= 0) {
            isTranslation.value = true;
        } else {
            latestTranslationName.value = result.translationName;
            isTranslation.value = false;
        }
    } catch (error: any) {
        message.error(error.message);
    }
};

const onEdit = (result: any, data: any) => {
    emit("Edit", { result, data });
};
const onAdd = (result: any, data: any) => {
    emit("Edit", { result, data });
};
const onDelete = (index: number) => {
    emit("Delete", index);
};
// 监听图片地址或翻译标识变化
watch([modelValue, effectiveTranslationName], ([newVal]) => {
    if (effectiveTranslationName.value != latestTranslationName.value) {
        isTranslation.value = true;
    } else {
        isTranslation.value = false;
    }
});
</script>

<style lang="less" scoped>
.multilingual-item-content {
    width: 100%;
    height: 100%;
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .multilingual-icon {
        position: absolute;
        top: -10px;
        right: -10px;
        width: 25px;
        height: 25px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 100px;
        background: #fff;
        border-radius: 3px;
        border: 1px solid #cbcbcb;
        z-index: 222;

        &__image {
            width: 24px !important;
            height: 24px !important;
            object-fit: contain;
            flex: none;
        }
    }
    .change-image {
        position: absolute;
        bottom: 0;
        left: 0;
        width: 100%;
        height: 20px;
        line-height: 20px;
        font-size: 12px;
        color: #fff;
        background: rgba(0, 0, 0, 0.5);
        cursor: pointer;
    }
    .close {
        position: absolute;
        top: -7px;
        left: -7px;
        cursor: pointer;
        display: none;
    }
    &:hover {
        .close {
            display: block;
        }
    }
}
</style>
