/** 会员留言回复 - 查看留言详情并回复会员 */
<template>
    <div class="container">
        <div class="lyecs-form-table">
            <h2>
                <span>{{ $t('类型:{typeName}', { typeName: formData.typeName }) }} </span>
                <span v-if="formData.productId">【 {{ formData.product.productName }} 】</span>
            </h2>
        </div>
        <el-divider />
        <div class="lyecs-form-table">
            <p>{{ $t('主题:{title}', { title: formData.title }) }}</p>
            <p>{{ $t('内容:{content}', { content: formData.content }) }}</p>
            <div class="pic-list flex" v-if="formData.feedbackPics && formData.feedbackPics.length > 0">
                <div class="item mr10" v-for="item in formData.feedbackPics">
                    <Image :src="item" fit="contain" style="height: 120px; width: 120px" />
                </div>
            </div>
            <p style="text-align: end">{{ $t("【 留言板 】{username}{'@'}{addTime}", { username: formData.username, addTime: formData.addTime }) }}</p>
        </div>
        <!-- 回复列表 -->
        <template v-if="formData.replyList && formData.replyList.length > 0">
            <template v-for="reply in formData.replyList" :key="reply.id">
                <el-divider />
                <div class="lyecs-form-table">
                    <p>{{ $t('内容:{content}', { content: reply.content }) }}</p>
                    <p style="text-align: end">{{ $t('【 回复 】{username}', { username: reply.username }) }}</p>
                </div>
            </template>
        </template>
        <el-divider />
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('回复内容')" prop="content">
                        <TigInput classType="tig-form-input" type="textarea" :rows="5" v-model="formState.content" />
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FeedbackFormState } from "@/types/user/feedback";
import { getFeedback, updateFeedback } from "@/api/user/feedback";
import { Image } from "@/components/image";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
    isDialog: Boolean
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formData = ref<FeedbackFormState>({
    product: {
        productId: 0,
        productName: ""
    }
});
const formState = ref<FeedbackFormState>({
    product: {
        productId: 0,
        productName: ""
    }
});
const fetchUserRank = async () => {
    try {
        const result = await getFeedback(action.value, { id: id.value });
        Object.assign(formData.value, result);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    // 获取详情数据
    fetchUserRank();
});
// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        let parentId: any = 0;
        if (formData.value.status == 1) {
            // parentId = formData.value.id
            parentId = 0;
        }
        const result = await updateFeedback(operation, { id: id.value, parentId: parentId, ...formState.value });
        emit("submitCallback", result);
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
<style lang="less" scoped>
.lyecs-form-table {
    h2 {
        line-height: 30px;
    }
    p {
        line-height: 20px;
    }
}
</style>
