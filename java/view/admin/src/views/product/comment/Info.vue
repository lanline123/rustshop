/** * @file 商品评论信息表单 * @description 用于添加或编辑商品评论的表单组件，支持管理员回复评论。 * @features * - 选择评论商品 * - 设置会员名称和头像 * -
评论星级选择（1-5星） * - 评论标签管理（可动态添加/删除） * - 评论内容和日期 * - 晒单图片上传 * - 推荐和置顶设置 * - 管理员回复功能 * - 审核查看模式（只读） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :label="$t('评论商品')" prop="productIds">
                        <SelectProduct :isMultiple="false" v-if="!loading" v-model:ids="formState.productIds" :disabled="examine == 1"> </SelectProduct>
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '会员名称不能为空!' }]" :label="$t('会员名称')" prop="username">
                        <TigInput classType="tig-form-input" v-model="formState.username" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item :label="$t('会员头像')" prop="avatar">
                        <FormAddGallery v-model:photo="formState.avatar" :disabled="examine == 1"></FormAddGallery>
                        <div class="extra">{{ $t('请统一上传比例为1:1的图片，宽100px，高100px（高清））') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('评论星级')" prop="commentRank">
                        <el-radio-group v-model="formState.commentRank" :disabled="examine == 1">
                            <el-radio :value="1">{{ $t('一星') }}</el-radio>
                            <el-radio :value="2">{{ $t('二星') }}</el-radio>
                            <el-radio :value="3">{{ $t('三星') }}</el-radio>
                            <el-radio :value="4">{{ $t('四星') }}</el-radio>
                            <el-radio :value="5">{{ $t('五星') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('标签')" prop="commentTag">
                        <div class="flex flex-wrap">
                            <el-tag
                                style="margin: 5px"
                                v-for="tag in formState.commentTag"
                                :key="tag"
                                :closable="examine != 1"
                                :disable-transitions="false"
                                @close="handleClose(tag)"
                            >
                                {{ tag }}
                            </el-tag>
                            <template v-if="examine != 1">
                                <TigInput
                                    classType="tig-form-input"
                                    v-if="inputVisible"
                                    ref="InputRef"
                                    v-model="inputValue"
                                    class="w-20"
                                    size="small"
                                    @keyup.enter="handleInputConfirm"
                                    @blur="handleInputConfirm"
                                />
                                <el-button style="margin: 5px" v-else class="button-new-tag" size="small" @click="showInput"> {{ $t('+ 新增标签') }} </el-button>
                            </template>
                        </div>
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '评论内容不能为空!' }]" :label="$t('评论内容')" prop="content">
                        <TigInput classType="tig-form-input" v-model="formState.content" type="textarea" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item :label="$t('评论日期')" prop="addTime">
                        <el-date-picker
                            v-model="formState.addTime"
                            format="YYYY-MM-DD HH:mm:ss"
                            :placeholder="$t('请选择日期')"
                            :disabled="examine == 1"
                            type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"
                        />
                    </el-form-item>
                    <el-form-item :label="$t('晒单')" prop="showPics">
                        <FormAddGallery v-if="!loading" v-model:photos="formState.showPics" :isMultiple="true" :disabled="examine == 1"> </FormAddGallery>
                    </el-form-item>
                    <el-form-item :label="$t('排序')" prop="sortOrder">
                        <TigInput classType="tig-form-input" type="integer" v-model="formState.sortOrder" :disabled="examine == 1" />
                    </el-form-item>
                    <el-form-item :label="$t('是否推荐')" prop="isRecommend">
                        <el-radio-group v-model="formState.isRecommend" :disabled="examine == 1">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('是否置顶')" prop="isTop">
                        <el-radio-group v-model="formState.isTop" :disabled="examine == 1">
                            <el-radio :value="1">{{ $t('是') }}</el-radio>
                            <el-radio :value="0">{{ $t('否') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">{{ $t('提交') }} </el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
            <div class="container-card" v-if="!loading && action !== 'add'">
                <div class="title"> {{ $t('管理员回复') }} <DialogForm
                        :params="{ id: id }"
                        isDrawer
                        path="product/comment/src/EditRemark"
                        :title="$t('回复评论')"
                        width="600px"
                        @okCallback="updateDataWithList"
                    >
                        <el-button bg class="buttonColor" size="small" text type="primary"> {{ $t('回复评论') }} </el-button>
                    </DialogForm>
                </div>
                <div v-if="!formState.replies || formState.replies.length === 0">{{ $t('暂无回复！') }}</div>
                <div v-else>
                    <div
                        v-for="item in formState.replies"
                        :key="item.commentId"
                        style="margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px dashed #ececec"
                    >
                        <p>{{ $t('内容：{content}', { content: item.content }) }}</p>
                        <p style="text-align: end; margin-top: 10px">{{ item.username }}@{{ item.addTime }}</p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { DialogForm } from "@/components/dialog";
import { onMounted, ref, shallowRef, nextTick } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { FormAddGallery } from "@/components/gallery";
import { CommentFormState } from "@/types/product/comment";
import { getComment, updateComment } from "@/api/product/comment";
import { SelectProduct } from "@/components/select";
import { ElInput } from "element-plus";

/**
 * @description 父组件回调事件
 * @event submitCallback - 表单提交成功后的回调
 * @event update:confirmLoading - 更新确认按钮加载状态
 * @event close - 关闭弹窗
 */
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

/**
 * @property {number} id - 评论ID
 * @property {string} act - 操作类型：'add'-新增, 'detail'-编辑
 * @property {number} examine - 是否为审核模式：1-只读查看
 * @property {boolean} isDialog - 是否在弹窗中显示
 */
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    examine: {
        type: Number,
        default: 0
    },
    isDialog: Boolean
});

/** 加载状态 */
const loading = ref<boolean>(true);
/** 当前路由查询参数 */
const query = useRouter().currentRoute.value.query;
/** 当前操作类型 */
const action = ref<string>(props.isDialog ? props.act : String(query.act));
/** 评论ID */
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
/** API操作类型 */
const operation = action.value === "add" ? "create" : "update";
/** 表单引用 */
const formRef = shallowRef();
/**
 * @description 评论表单数据
 * @property {number} commentRank - 评论星级：1-5
 * @property {number} isRecommend - 是否推荐：0-否，1-是
 * @property {number} isTop - 是否置顶：0-否，1-是
 * @property {number} sortOrder - 排序值
 * @property {number[]} productIds - 评论商品ID
 * @property {string[]} commentTag - 评论标签列表
 */
const formState = ref<CommentFormState>({
    commentRank: 5,
    isRecommend: 1,
    isTop: 0,
    sortOrder: 50,
    productIds: [],
    commentTag: []
});

/** 新标签输入值 */
const inputValue = ref("");
/** 标签输入框是否显示 */
const inputVisible = ref(false);
/** 标签输入框引用 */
const InputRef = ref<InstanceType<typeof ElInput>>();

/**
 * @description 删除标签
 * @param tag 要删除的标签内容
 */
const handleClose = (tag: string) => {
    formState.value.commentTag.splice(formState.value.commentTag.indexOf(tag), 1);
};

/**
 * @description 显示标签输入框
 */
const showInput = () => {
    inputVisible.value = true;
    nextTick(() => {
        InputRef.value!.input!.focus();
    });
};

/**
 * @description 确认添加标签
 */
const handleInputConfirm = () => {
    if (inputValue.value) {
        formState.value.commentTag.push(inputValue.value);
    }
    inputVisible.value = false;
    inputValue.value = "";
};
onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchComment();
    } else {
        loading.value = false;
    }
});
/**
 * @description 获取评论详情数据
 */
const fetchComment = async () => {
    try {
        const result = await getComment(action.value, { id: id.value });
        let productIds: number[] = [];
        if (result.productId) {
            productIds = [result.productId];
        }
        Object.assign(formState.value, result, { productIds: productIds });
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
/**
 * @description 回复评论后刷新评论数据
 */
const updateDataWithList = () => {
    // 获取详情数据
    fetchComment();
};

/**
 * @description 表单验证通过后提交评论数据
 */
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        let productId = 0;
        if (formState.value.productIds.length > 0) {
            productId = formState.value.productIds[0];
        }
        const result = await updateComment(operation, { id: id.value, ...formState.value, productId: productId });
        emit("submitCallback", result);
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};
/**
 * @description 表单提交入口，供父组件调用
 */
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style scoped lang="less">
.container-card {
    border: 1px solid #ececec;
    box-shadow: 2px 2px 5px rgba(203, 193, 193, 0.2);
    border-radius: 2px;
    padding: 20px;
    background: #fff;
    margin-bottom: 20px;
    .title {
        font-family: bold;
        font-size: 16px;
        line-height: 25px;
        color: #323233;
        margin-bottom: 15px;
    }
}
</style>
