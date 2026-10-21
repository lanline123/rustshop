<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form ref="formRef" :model="formState" label-width="auto">
                    <el-form-item name="name" :label="$t('相册名称')" :rules="[{ required: true, message: '相册名称不能为空!' }]">
                        <TigInput v-model="formState.name" maxlength="20" />
                    </el-form-item>
                    <el-form-item name="sort" :label="$t('排序')">
                        <TigInput type="integer" v-model="formState.sort" :min="0" />
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { message, Progress, Upload, Modal } from "ant-design-vue";
import { ref, reactive, toRefs, onMounted } from "vue"
import {getGalleryVideoDetail,updateGalleryVideoInfo} from "@/api/setting/gallery";
import type {GalleryDetail} from "@/types/setting/gallery.d";

//获取来自父组件的参数
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    parentId: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ''
    },
    isDialog: Boolean
})
const { parentId, id, act } = toRefs(props);
// 判断是处理更新还是添加
const operation = act.value == 'add' ? 'create' : 'update';

// 表单参数初使化
const formRef = ref();  //表单Ref
let formState = reactive<GalleryDetail>({
    name:"",
    sort: 50
});  //表单数据
onMounted(() => {
    if (act.value === "detail") {
        // 获取详情数据
        fetchGalleryInfo()
    }
});
const fetchGalleryInfo = async () => {
    try {
        const result = await getGalleryVideoDetail({ id: id.value });
        Object.assign(formState, result);
    } catch (error: any) {
        message.error(error.message);
    }
};
// 父组件回调
const emit = defineEmits(["submitCallback", "okType"])

// 表单通过验证后提交
const formSubmit = async () => {
    await formRef.value.validate();
    try {
        const result = await updateGalleryVideoInfo(operation,{
                id: id.value,
                parentId: parentId.value,
                ...formState
            });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    }
};

// 处理
const onFormSubmit = () => {
    formSubmit()
};

defineExpose({
    onFormSubmit
});

</script>
