<template>
    <div class="lyecs-popedit">
        <div v-if="isHover" class="flex flex-align-center pop-form-wrap">
            <slot></slot>
            <span class="lyecs-popedit-btn iconfont icon-bianji1" ref="triggerRef" @click="editBtn"></span>
        </div>
        <div v-else style="width: 100%" class="flex flex-align-center flex-justify-center pop-form-wrap">
            <slot></slot>
            <span class="lyecs-popedit-btn2 iconfont icon-bianji1" @click="editBtn"></span>
        </div>
        <div v-if="loaded" style="position: relative">
            <el-popover
                ref="popoverRef"
                trigger="manual"
                placement="bottom-start"
                :hide-after="0"
                :visible="visible"
                @confirm="onConfirm"
                @cancel="onCancel"
                :teleported="true"
                width="360px"
            >
                <template #reference>
                    <div style="position: absolute; left: -32px; width: 50px; height: 5px; visibility: hidden"></div>
                </template>
                <template #default>
                    <div ref="popoverContentRef" class="popForm-con" style="width: 100%; padding: 8px 6px 5px; box-sizing: border-box">
                        <el-form ref="formRef" :model="formState" layout="horizontal" name="form_in_modal" :wrapper-col="{ span: 38 }" :label-wrap="false">
                            <el-form-item prop="paramValue" :label="label" :rules="[{ required: props.required, message: '请选择' }]">
                                <div>
                                    <div>
                                        <el-select v-model="formState.paramValue">
                                            <el-option :label="options.label" :value="options.value" :disabled="options.label === '无上级'"></el-option>
                                        </el-select>
                                    </div>
                                    <div class="extra" v-if="extra">{{ extra }}</div>
                                </div>
                            </el-form-item>
                        </el-form>
                        <div class="flex" style="justify-content: right">
                            <el-button size="small" @click="visible = false">{{ $t('取消') }}</el-button>
                            <el-button size="small" type="primary" @click="onConfirm" :loading="loading">{{ $t('确认') }}</el-button>
                        </div>
                    </div>
                </template>
            </el-popover>
        </div>
    </div>
</template>
<script setup lang="ts">
import { ref, reactive, shallowRef, onMounted, onBeforeUnmount } from "vue";
import request from "@/utils/request";
import { message } from "ant-design-vue";
import { ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = defineProps({
    orgValue: [String, Number], //父组件使用v-model双向绑定
    requestApi: { type: Function, default: request },
    label: { type: String, default: "编辑项" },
    extra: { tyoe: String, default: "" },
    max: { type: Number, default: 0 },
    min: { type: Number, default: 0 },
    maxNum: { type: Number, default: 0 },
    minNum: { type: Number, default: 0 },
    len: { type: Number, default: 0 },
    required: { type: Boolean, default: true },
    params: {
        type: Object,
        default: { id: null, field: "" }
    },
    isHover: { type: Boolean, default: true },
    isPrice: { type: Boolean, default: false },
    options: { type: Object, default: () => {} }
});
const popoverRef = ref();
const visible = ref<any>(false);
const loaded = ref<Boolean>(false);
const formRef = shallowRef();
let formState = reactive<any>({
    //表单数据
    paramValue: props.orgValue
});
const emit = defineEmits(["update:orgValue", "callback"]);
const loading = ref<Boolean>(false);
const onConfirm = async () => {
    await formRef.value.validate();
    ElMessageBox.confirm(t('是否对该订单的状态进行修改?'), t('再次确认'), {
        confirmButtonText: t('确认'),
        cancelButtonText: t('取消'),
        type: "warning"
    })
        .then(async () => {
            try {
                loading.value = true;
                const result = await props.requestApi({
                    id: props.params.id,
                    field: props.params.field,
                    val: formState.paramValue
                });
                emit("callback");
                message.success(t('操作成功'));
                visible.value = false;
            } catch (error: any) {
                message.error(error.message);
            } finally {
                loading.value = false;
            }
        })
        .catch(() => {});
};

const editBtn = () => {
    loaded.value = true;
    visible.value = true;
    formState.paramValue = props.orgValue;
};

const show = (e: any) => {
    visible.value = true;
};
const onCancel = () => {
    // 还原表单的值
    formState.paramValue = props.orgValue;
    // 清除验证信息
    formRef.value.clearValidate();
};
const triggerRef = ref(null);
const popoverContentRef = ref(null);
// 点击外部区域关闭 Popover
const handleClickOutside = (event) => {
    if (!visible.value) return;

    const isClickInsidePopover = popoverContentRef.value?.contains(event.target);
    const isClickOnTrigger = triggerRef.value?.contains(event.target);

    if (!isClickInsidePopover && !isClickOnTrigger) {
        visible.value = false;
    }
};

// 添加全局点击事件监听
onMounted(() => {
    document.addEventListener("click", handleClickOutside);
});

// 组件卸载前移除监听
onBeforeUnmount(() => {
    document.removeEventListener("click", handleClickOutside);
});

defineExpose({
    show
});
</script>

<style lang="less" scoped>
.popForm-con {
    width: 370px;
    position: relative;
}

.lyecs-popedit {
    display: flex;
    position: relative;
    align-items: center;
}

.lyecs-popedit .lyecs-popedit-btn {
    cursor: pointer;
    color: #999;
    font-size: 16px;
    padding-left: 2px;
    line-height: 1;
}
.lyecs-popedit .lyecs-popedit-btn2 {
    cursor: pointer;
    color: #999;
    font-size: 16px;
    padding-left: 2px;
    line-height: 1;
}
</style>
