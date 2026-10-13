/** 客户标记编辑弹窗 - 为客户添加颜色标记和备注 */
<template>
    <div class="btn" @click="handleShow">
        <slot> </slot>
    </div>
    <template v-if="show">
        <el-dialog
            class="sign-dialog"
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
                    <div class="header-title">{{ $attrs.title || $t("添加标记") }}</div>
                    <div class="header-close" @click="showDialog = false">
                        <i class="iconfont-admin icon-cha1"></i>
                    </div>
                </div>
            </template>
            <a-spin :spinning="loading" style="width: 100%; height: 100%">
                <el-form v-if="!loading" ref="formRef" :model="form" label-width="auto">
                    <el-form-item :label="$t('标记')" prop="mark ">
                        <el-radio-group v-model="form.mark">
                            <template v-for="item in signList" :key="item.value">
                                <el-radio :value="item.value"><SignTag :value="item.value"></SignTag></el-radio>
                            </template>
                        </el-radio-group>
                    </el-form-item>
                    <el-form-item :label="$t('备注')" prop="adminNote">
                        <TigInput
                            classType="tig-form-input"
                            v-model="form.adminNote"
                            :rows="6"
                            :placeholder="$t('请输入备注')"
                            type="textarea"
                            show-word-limit
                            :maxlength="200"
                        />
                    </el-form-item>
                    <el-form-item>
                        <div class="button-box">
                            <a class="btn-link" @click="handleClear">{{ $t("清除") }}</a>
                            <el-button @click="handleClose">{{ $t("取消") }}</el-button>
                            <el-button type="primary" @click="onSubmit()">{{ $t("提交") }} </el-button>
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
import SignTag from "@/views/user/user/src/SignTag.vue";
import { message } from "ant-design-vue";
import { useResetReactive } from "@/hooks/useResetState";
import { setAdminNote } from "@/api/user/user";
import { UserFormState } from "@/types/user/user.d";

const props = defineProps({
    item: {
        type: Object as PropType<UserFormState>,
        default: () => ({})
    },
    ids: {
        type: Array as PropType<number[] | any[]>,
        default: () => []
    }
});

const emit = defineEmits(["callBack"]);

const formRef = ref();

/** 标记颜色列表 */
const signList = [
    {
        value: 1,
        color: "#ff5050"
    },
    {
        value: 2,
        color: "#ffba1f"
    },
    {
        value: 3,
        color: "#2ecf92"
    },
    {
        value: 4,
        color: "#16afff"
    },
    {
        value: 5,
        color: "#792dff"
    }
];

const show = ref(false);

const showDialog = ref(false);

const loading = ref<boolean>(false);

const [form, reset] = useResetReactive({
    adminNote: "",
    mark: 0
});

/** 显示弹窗 */
const handleShow = () => {
    const { adminNote = "", mark = 0 } = props.item;
    form.adminNote = adminNote;
    form.mark = mark;
    show.value = true;
    showDialog.value = true;
};

/** 关闭弹窗 */
const handleClose = () => {
    showDialog.value = false;
    reset();
};

/** 清除标记 */
const handleClear = () => {
    reset();
    setAdminNoteData();
};

/** 提交标记 */
const onSubmit = async () => {
    setAdminNoteData();
};

/** 保存标记数据 */
const setAdminNoteData = async () => {
    try {
        const data: any = {
            ...form
        };

        if (props.item.userId) {
            data.userId = props.item.userId;
        } else if (props.ids && props.ids.length > 0) {
            data.userIds = props.ids.join(",");
        }

        await setAdminNote(data);

        message.success(t("操作成功"));

        showDialog.value = false;

        emit("callBack");
    } catch (error: any) {
        message.error(error.message);
    }
};
</script>

<style lang="less">
body {
    .el-overlay-dialog {
        .sign-dialog {
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

            .btn-link {
                margin-right: 12px;
                font-size: 14px;
            }
        }
    }
}
</style>
