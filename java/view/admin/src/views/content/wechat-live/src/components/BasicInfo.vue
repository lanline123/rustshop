<!--
 * @file 直播间基本信息组件
 * @description 显示和编辑直播间的基本信息
 * @module views/content/wechat-live/src/components/BasicInfo
-->
<template>
    <el-form ref="formRef" :model="formState" label-width="110px" class="basic-info">
        <el-form-item :rules="[{ required: true, message: '请选择直播类型' }]" :label="$t('直播类型')" prop="liveType">
            <div>
                <el-radio-group v-model="formState.liveType" :disabled="act === 'detail'">
                    <el-radio :value="0">{{ $t("手机直播") }}</el-radio>
                    <el-radio :value="1">{{ $t("推流设备直播") }}</el-radio>
                </el-radio-group>
                <div class="tip" v-if="formState.liveType === 0">{{ $t("通过小程序直播开播") }}</div>
            </div>
        </el-form-item>
        <el-form-item :rules="nameRules" :label="$t('直播间名称')" prop="name">
            <BusinessData
                width="450px"
                v-model:modelValue="formState.name"
                :dataId="id"
                :dataType="12"
                :disabled="act === 'detail'"
                :maxlength="34"
                :placeholder="$t('请输入6-17个字')"
            />
        </el-form-item>
        <el-form-item :rules="[{ required: true, message: $t('请选择开播时间') }]" :label="$t('开播时间')" prop="startTime">
            <div>
                <SelectTimeInterval
                    v-model:end-date="formState.endTime"
                    v-model:start-date="formState.startTime"
                    clearable
                    type="datetime"
                    value-format="YYYY-MM-DD HH:mm:ss"
                    :disable-before-today="true"
                    :disabled="act === 'detail'"
                />
                <div class="tip" style="margin-top: 5px">
                    {{
                        $t(
                            "开播时间仅供参考，不是实际直播间可以开播的时间。直播间在开始时间前也可以开播，并且到结束时间后不会强制结束。 若到结束时间仍未开播，则直播间无法再开播。"
                        )
                    }}
                </div>
            </div>
        </el-form-item>
        <el-form-item :rules="anchorNameRules" :label="$t('主播昵称')" prop="anchorName">
            <BusinessData
                width="450px"
                v-model:modelValue="formState.anchorName"
                :dataId="id"
                :dataType="13"
                :disabled="act === 'detail'"
                :maxlength="30"
                :placeholder="$t('请输入4-15个字')"
            />
        </el-form-item>
        <el-form-item :rules="[{ required: true, message: $t('请输入主播微信账号') }]" :label="$t('主播微信账号')" prop="anchorWechat">
            <div>
                <TigInput
                    v-model="formState.anchorWechat"
                    :disabled="act === 'detail'"
                    :maxlength="20"
                    :placeholder="$t('请输入20字以内')"
                    classType="tig-form-input"
                />
                <div class="tip" style="margin-top: 5px">
                    <div>{{ $t("每个直播间需要绑定一个用于主播身份核实，选择手机直播时将用该微信号开播。") }}</div>
                    <div>
                        {{ $t("未认证主播请使用微信扫描该") }}
                        <el-popover class="box-item" title="" placement="bottom" width="273" style="padding: 0">
                            <img :src="WECHAT_LIVE_AUTH_CODE_URL" alt="" width="250" />
                            <template #reference>
                                <span class="btn-link">{{ $t("小程序码") }}</span>
                            </template>
                        </el-popover>
                        <!--                        <a :href="WECHAT_LIVE_AUTH_CODE_URL" target="_blank" rel="noopener noreferrer">小程序码</a>-->
                        {{ $t("完成认证。") }}
                    </div>
                </div>
            </div>
        </el-form-item>
        <el-form-item :label="$t('副主播微信账号')" prop="subAnchorWechat">
            <div>
                <TigInput
                    v-model="formState.subAnchorWechat"
                    :disabled="act === 'detail'"
                    :maxlength="20"
                    :placeholder="$t('请输入20字以内')"
                    classType="tig-form-input"
                />
                <div class="tip" style="margin-top: 5px">
                    <div>{{ $t('手机直播时，副主播可以访问主播端"小程序直播"小程序管理直播评论') }}</div>
                </div>
            </div>
        </el-form-item>
        <el-form-item :rules="[{ required: true, message: '请选择官方收录' }]" :label="$t('官方收录')" prop="isFeedsPublic">
            <div>
                <el-radio-group v-model="formState.isFeedsPublic" :disabled="act === 'detail'">
                    <el-radio :value="1">{{ $t("开启") }}</el-radio>
                    <el-radio :value="0">{{ $t("关闭") }}</el-radio>
                </el-radio-group>
                <div class="tip" style="margin-top: 5px">{{ $t("开启后本场直播将有可能被官方收录") }}</div>
            </div>
        </el-form-item>
    </el-form>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { SelectTimeInterval } from "@/components/select";

/**
 * 直播间表单数据类型
 */
import { useI18n } from "vue-i18n";
const { t } = useI18n();
interface LiveRoomFormState {
    liveType?: number; // 直播类型：0-手机直播，1-推流设备直播
    name?: string; // 直播间名称
    startTime?: string; // 开播时间
    endTime?: string; // 结束时间
    anchorName?: string; // 主播昵称
    anchorWechat?: string; // 主播微信账号
    subAnchorWechat?: string; // 副主播微信账号
    isFeedsPublic?: number; // 官方收录：0-关闭，1-开启
}

// 计算字符长度：一个中文算2个英文字符或特殊字符
const getTextLength = (value: string) => {
    return Array.from(value).reduce((total, char) => {
        return total + (/[\u4e00-\u9fa5]/.test(char) ? 2 : 1);
    }, 0);
};

// 校验字数范围：一个字等于两个英文字符或特殊字符
const createTextLengthValidator = (min: number, max: number, message: string) => {
    return (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        const text = value?.trim?.() || "";
        const length = getTextLength(text);

        if (length < min || length > max) {
            callback(new Error(message));
            return;
        }

        callback();
    };
};

// 直播间名称验证规则：6-17个字
const nameRules: FormRules<LiveRoomFormState>["name"] = [
    { required: true, message: t("请输入直播间名称"), trigger: "blur" },
    {
        validator: createTextLengthValidator(12, 34, t("直播标题必须为6-17个字（一个字等于两个英文字符或特殊字符）")),
        trigger: "blur"
    }
];

// 主播昵称验证规则：4-15个字
const anchorNameRules: FormRules<LiveRoomFormState>["anchorName"] = [
    { required: true, message: t("请输入主播昵称"), trigger: "blur" },
    {
        validator: createTextLengthValidator(8, 30, t("主播昵称必须为4-15个字（一个字等于两个英文字符或特殊字符）")),
        trigger: "blur"
    }
];

const WECHAT_LIVE_AUTH_CODE_URL = "https://res.wx.qq.com/op_res/9rSix1dhHfK4rR049JL0PHJ7TpOvkuZ3mE0z7Ou_Etvjf-w1J_jVX0rZqeStLfwh";

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    act: {
        type: String,
        default: ""
    },
    loading: {
        type: Boolean,
        default: false
    },
    formState: {
        type: Object as () => LiveRoomFormState,
        default: () => ({})
    }
});

const formState = defineModel<LiveRoomFormState>("formState", { default: () => ({}) });

// 表单引用，用于验证
const formRef = ref<FormInstance>();

// 暴露 validate 方法供父组件调用
const validate = async () => {
    if (!formRef.value) return false;
    try {
        await formRef.value.validate();
        return true;
    } catch (_error) {
        return false;
    }
};

// 暴露方法
defineExpose({
    validate
});
</script>

<style lang="less" scoped>
.basic-info {
    :deep(.el-form-item) {
        margin-bottom: 18px;
    }

    :deep(.el-radio-group) {
        .el-radio {
            margin-right: 20px;
        }
    }
}

.tip {
    color: rgb(149, 152, 166);
    font-size: 12px;
    line-height: 16px;

    a {
        color: var(--tig-primary);
        text-decoration: none;
    }

    a:hover {
        text-decoration: underline;
    }
}
</style>
