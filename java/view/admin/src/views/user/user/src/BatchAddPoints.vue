/** 批量增加客户积分弹窗组件 */
<template>
    <div class="btn" @click="handleShow">
        <slot></slot>
    </div>
    <template v-if="show">
        <el-dialog
            class="batch-points-dialog"
            :append-to-body="true"
            :show-close="false"
            v-model="showDialog"
            width="500"
            top="35vh"
            v-bind="$attrs"
            @close="handleClose"
            @closed="show = false"
        >
            <template #header>
                <div class="header">
                    <div class="header-title">{{ $t("批量增加客户积分") }}</div>
                    <div class="header-close" @click="showDialog = false">
                        <i class="iconfont-admin icon-cha1"></i>
                    </div>
                </div>
            </template>
            <a-spin :spinning="loading" style="width: 100%; height: 100%">
                <el-form v-if="!loading" ref="formRef" :model="form" :rules="rules" label-width="auto">
                    <el-form-item :label="$t('增加积分数')" prop="points">
                        <TigInput classType="tig-form-input" v-model="form.points" type="integer" :placeholder="$t('请输入增加积分数')" :min="1" />
                    </el-form-item>
                    <el-form-item>
                        <div class="button-box">
                            <el-button @click="handleClose">{{ $t("取消") }}</el-button>
                            <el-button type="primary" @click="onSubmit()">{{ $t("提交") }}</el-button>
                        </div>
                    </el-form-item>
                </el-form>
            </a-spin>
        </el-dialog>
    </template>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
const { t } = useI18n();
defineOptions({
    inheritAttrs: false
});

import { PropType, ref } from "vue";
import { message } from "ant-design-vue";
import { useResetReactive } from "@/hooks/useResetState";
import { batchAddPoints } from "@/api/user/user";

const props = defineProps({
    ids: {
        type: Array as PropType<(string | number)[]>,
        default: () => []
    }
});

const emit = defineEmits(["callBack"]);

const formRef = ref();

const show = ref(false);
const showDialog = ref(false);
const loading = ref<boolean>(false);

const [form, reset] = useResetReactive({
    points: ""
});

/** 验证正整数 */
const validatePoints = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t("请输入增加积分数")));
    } else if (!/^[1-9]\d*$/.test(value)) {
        callback(new Error(t("增加积分数必须为大于0的整数")));
    } else {
        callback();
    }
};

const rules = {
    points: [{ required: true, validator: validatePoints, trigger: "blur" }]
};

/** 显示弹窗 */
const handleShow = () => {
    reset();
    show.value = true;
    showDialog.value = true;
};

/** 关闭弹窗 */
const handleClose = () => {
    showDialog.value = false;
    reset();
};

/** 提交批量增加积分 */
const onSubmit = async () => {
    try {
        await formRef.value.validate();
        loading.value = true;

        const data = {
            ids: props.ids,
            value: Number(form.points)
        };

        await batchAddPoints(data);
        message.success(t("操作成功"));
        showDialog.value = false;
        emit("callBack");
    } catch (error: any) {
        if (error.message) {
            message.error(error.message);
        }
    } finally {
        loading.value = false;
    }
};
</script>

<style lang="less">
body {
    .el-overlay-dialog {
        .batch-points-dialog {
            padding: 20px 24px;
            border-radius: 4px;
            .el-dialog__header {
                padding: 0;
                border: none;
            }
            .el-form-item:last-child {
                margin-bottom: 0;
            }
        }

        .header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 24px;

            .header-title {
                color: #1e2226;
                font-weight: 600;
                font-size: 16px;
                line-height: 24px;
            }

            .icon-cha1 {
                cursor: pointer;
                font-size: 12px;
                padding: 6px 0px 6px 8px;
                font-weight: normal;
                color: #8a9099;
                transition: color 0.2s;

                &:hover {
                    color: #1e2226;
                }
            }
        }

        .button-box {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: flex-end;
            column-gap: 6px;
        }

        .form-tip {
            color: #9598a6;
            font-size: 12px;
            margin-top: 4px;
        }
    }
}
</style>
