<template>
    <!-- 未绑定时显示绑定按钮 -->
    <DialogForm
        v-if="!bindedH5Id"
        isDrawer
        @okCallback="onOk"
        :title="type === 'bindCustom' ? $t('绑定PC页面') : $t('绑定移动端页面')"
        width="600px"
        path="decorate/pcCustomPage/src/SelectMobilePage"
        :params="{ act: 'detail', isMultiple: false, type, selectedIds: [bindedH5Id] }"
    >
        <el-button link type="primary">{{ type === "bindCustom" ? $t('绑定PC页面') : $t('绑定移动端页面') }}</el-button>
    </DialogForm>
    <!-- 已绑定时显示取消绑定 -->
    <DeleteRecord
        v-else
        @afterDelete="onCancelBindingSuccess"
        :requestApi="cancelBinding"
        :params="{ decorateId: decorateId }"
        :title="type === 'bindCustom' ? $t('您确认要取消绑定PC页面吗？') : $t('您确认要取消绑定移动端页面吗？')"
        :message="$t('取消绑定成功')"
    > {{ $t('取消绑定') }} </DeleteRecord>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { message } from "ant-design-vue";
import { DialogForm } from "@/components/dialog";
import { DeleteRecord } from "@/components/list";
import { bindingH5Decorate, bindingCustomDecorate, cancelBinding } from "@/api/decorate/pcCustomPage";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    decorateId: {
        type: Number,
        required: true
    },
    bindedH5Id: {
        type: Number,
        default: 0
    },
    type: {
        type: String,
        default: "bindH5" // bindH5, bindCustom
    }
});

const emit = defineEmits(["okCallback"]);

// 弹窗回调
const onFormSubmit = () => {
    emit("okCallback");
};

const onOk = (selectedData: any) => {
    console.log(selectedData);
    // 判断是否有选中参数
    if (!selectedData || !selectedData.length) {
        message.error(props.type === "bindCustom" ? t('请选择要绑定的PC端页面') : t('请选择要绑定的移动端页面'));
        return;
    }
    const fetchApi = props.type === "bindCustom" ? bindingCustomDecorate : bindingH5Decorate;
    // 调用接口进行绑定
    fetchApi({
        decorateId: props.decorateId,
        bindedH5Id: selectedData[0]
    })
        .then((res: any) => {
            message.success(t('操作成功'));
            emit("okCallback");
        })
        .catch((err: any) => {
            message.error(err.message);
        });
};

// 取消绑定成功回调
const onCancelBindingSuccess = () => {
    emit("okCallback");
};

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped></style>
