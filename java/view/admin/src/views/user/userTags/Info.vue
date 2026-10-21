<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" :rules="formRules" label-width="auto">
                    <el-form-item :label="$t('标签组名称')" prop="groupName">
                        <TigInput classType="tig-form-input" v-model="formState.groupName" :placeholder="$t('请输入标签组名称')" />
                    </el-form-item>
                    <el-form-item :label="$t('标签值')" prop="tags">
                        <div class="tag-values-wrapper">
                            <div v-for="(item, index) in formState.tags" :key="item.id ?? index" class="tag-value-item">
                                <span class="tag-value-label">{{ $t('选项{index1}', { index1: index + 1 }) }}</span>
                                <TigInput classType="tig-form-input" v-model="item.tagName" :placeholder="$t('请输入')"
                                    style="flex: 1" />
                                <el-icon v-if="formState.tags!.length > 2" class="tag-value-remove"
                                    @click="removeTagValue(index)">
                                    <Close />
                                </el-icon>
                            </div>
                            <div class="tag-value-add">
                                <el-button type="primary" link @click="addTagValue">{{ $t('+ 新增选项') }}</el-button>
                            </div>
                        </div>
                        <!-- <div class="tag-value-tip">多选项至少需要添加两个选项</div> -->
                    </el-form-item>
                    <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary"
                            @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { Close } from "@element-plus/icons-vue";
import { TagItem, UserTagFormState } from "@/types/user/userTags.d";
import { getMemberTagGroupDetail, createMemberTagGroup, updateMemberTagGroup } from "@/api/user/userTags";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: { type: Number, default: 0 },
    act: { type: String, default: "" },
    isDialog: Boolean
});

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const formRef = shallowRef();
const createEmptyTag = (): TagItem => ({ tagName: "" });

const formState = ref<UserTagFormState>({
    groupName: "",
    tags: [createEmptyTag(), createEmptyTag()]
});

const validateTagNames = (_rule: any, _value: any, callback: any) => {
    const tags = formState.value.tags || [];
    if (tags.length < 2) {
        callback(new Error(t('请至少添加两个标签选项')));
        return;
    }
    const emptyIndex = tags.findIndex((tag) => !tag.tagName || !tag.tagName.trim());
    if (emptyIndex > -1) {
        callback(new Error(`选项${emptyIndex + 1}的标签值不能为空，请填写或删除该选项`));
        return;
    }
    callback();
};

const formRules = {
    groupName: [{ required: true, message: t('标签组名称不能为空!'), trigger: "blur" }],
    tags: [{ required: true, validator: validateTagNames, trigger: "blur" }]
};

const addTagValue = () => {
    formState.value.tags!.push(createEmptyTag());
};

const removeTagValue = (index: number) => {
    formState.value.tags!.splice(index, 1);
};

const fetchDetail = async () => {
    try {
        loading.value = true;
        const result = await getMemberTagGroupDetail({ id: id.value });
        formState.value.groupName = result.groupName;
        formState.value.tags = result.tags?.map((tag) => ({
            id: tag.id,
            tagName: tag.tagName || ""
        })) || [createEmptyTag(), createEmptyTag()];
        if (formState.value.tags.length < 2) {
            while (formState.value.tags.length < 2) {
                formState.value.tags.push(createEmptyTag());
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        fetchDetail();
    } else {
        loading.value = false;
    }
});

const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const tags = formState.value.tags!.map((tag) => ({
            ...(tag.id ? { id: tag.id } : {}),
            tagName: tag.tagName!.trim()
        }));
        if (action.value === "add") {
            await createMemberTagGroup({
                groupName: formState.value.groupName,
                tagNames: tags.map((tag) => tag.tagName)
            });
        } else {
            await updateMemberTagGroup({
                id: id.value,
                groupName: formState.value.groupName,
                tags
            });
        }
        emit("submitCallback");
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
    }
};

const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>

<style lang="less" scoped>
.tag-values-wrapper {
    width: 450px;
    max-width: 100%;
    box-sizing: border-box;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    padding: 16px;
    background: #fafafa;

    .tag-value-item {
        display: flex;
        align-items: center;
        margin-bottom: 12px;
        gap: 10px;

        .tag-value-label {
            flex-shrink: 0;
            width: 45px;
            color: #606266;
            font-size: 14px;
        }

        .tag-value-remove {
            flex-shrink: 0;
            font-size: 12px;
            width: 18px;
            height: 18px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            background-color: #c0c4cc;
            color: #fff;
            cursor: pointer;
            border-radius: 50%;
            transition: background-color 0.2s ease;

            &:hover {
                background-color: #a6a9ad;
            }
        }
    }

    .tag-value-add {
        margin-bottom: 0;
    }
}

.tag-value-tip {
    color: #909399;
    font-size: 12px;
    margin-top: 6px;
    line-height: 1.5;
}

@media (max-width: 768px) {
    .tag-values-wrapper {
        width: 80%;
    }
}
</style>
