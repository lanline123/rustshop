/** * @file 批发商设置 * @description 批发功能总开关 + 自动转批发 + 默认批发等级 + 批发商品赠送积分 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form
                v-if="!loading"
                ref="formRef"
                :model="formState"
                :rules="rules"
                label-width="160px"
                class="wholesale-setting"
                style="display: flex; gap: 12px; flex-direction: column"
            >
                <div class="content_wrapper">
                    <div class="title">{{ $t('批发商功能') }}</div>
                    <el-form-item :label="$t('批发商功能')">
                        <div>
                            <el-switch
                                v-model="formState.wholesaleEnabled"
                                :active-value="1"
                                :inactive-value="0"
                                :active-text="$t('已开启')"
                            />
                            <div class="extra">{{ $t('开启后，可根据配置的批发商品定价规则，已认证批发商登录可查看专属批发价并批量下单；关闭后批发配置失效，所有用户仅显示零售价，无法进行批发采购。') }}</div>
                        </div>
                    </el-form-item>
                </div>

                <div v-if="formState.wholesaleEnabled === 1" class="content_wrapper">
                    <div class="title">{{ $t('批发商配置') }}</div>
                    <el-form-item :label="$t('添加批发商')">
                        <div>
                            <el-checkbox v-model="autoRegisterChecked"> {{ $t('新增会员默认成为批发商') }} </el-checkbox>
                            <div class="extra"> {{ $t('开启此选项后，所有新注册 / 新增的会员，系统将自动赋予其「批发商」身份，默认享有批发价、批量下单等批发权益。如需手动添加批发商，请关闭此选项。') }} </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('默认批发等级')" prop="wholesaleDefaultGradeId">
                        <div>
                            <el-select v-model="selectedGradeId" :placeholder="$t('请选择默认批发等级')" style="width: 300px">
                                <el-option v-for="item in gradeOptions" :key="item.gradeId" :label="item.gradeName" :value="item.gradeId" />
                            </el-select>
                            <div class="extra">{{ $t('新增会员自动转批发商时绑定的等级；默认等级在此页设置（唯一入口），等级管理只读反映') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('批发商品赠送积分')">
                        <div>
                            <el-radio-group v-model="formState.wholesaleGivePoints">
                                <el-radio :value="1">{{ $t('赠送') }}</el-radio>
                                <el-radio :value="0">{{ $t('不赠送') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">{{ $t('选择「赠送」时，用户购买批发商品可按平台规则获得对应积分；选择「不赠送」时，批发商品订单将不产生任何积分。') }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 30px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="saving" class="form-submit-btn" size="large" type="primary" @click="onSubmit"> {{ $t('提 交') }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { ref, computed, onMounted } from "vue";
import type { FormInstance, FormRules } from "element-plus";
import { message } from "ant-design-vue";
import { getWholesaleSetting, saveWholesaleSetting } from "@/api/wholesale/setting";
import { getGradeList } from "@/api/wholesale/grade";
import type { WholesaleSettingVO } from "@/types/wholesale/setting";
import type { WholesalerGradeVO } from "@/types/wholesale/grade";
import { useThemeStore } from "@/store/theme";

import { useI18n } from "vue-i18n"
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const loading = ref<boolean>(true);
const saving = ref<boolean>(false);
const formState = ref<WholesaleSettingVO>({
    wholesaleEnabled: 0,
    wholesaleAutoRegister: 0,
    wholesaleDefaultGradeId: 0,
    wholesaleGivePoints: 0,
    isMultiShop: false
});
const gradeOptions = ref<WholesalerGradeVO[]>([]);
const formRef = ref<FormInstance>();

// 仅当所选等级在启用列表中才回显，匹配不上则显示空（不展示裸数字）
const selectedGradeId = computed({
    get: () => {
        const id = formState.value.wholesaleDefaultGradeId;
        return id && gradeOptions.value.some((g) => g.gradeId === id) ? id : undefined;
    },
    set: (val) => {
        formState.value.wholesaleDefaultGradeId = val ?? 0;
    }
});

const rules: FormRules = {
    wholesaleDefaultGradeId: [
        {
            required: true,
            message: t('请选择默认批发等级'),
            validator: (_rule, value, callback) => {
                if (formState.value.wholesaleEnabled === 1 && formState.value.wholesaleAutoRegister === 1 && !(value && value > 0)) {
                    callback(new Error(t('请选择默认批发等级')));
                } else {
                    callback();
                }
            },
            trigger: "change"
        }
    ]
};

const autoRegisterChecked = computed({
    get: () => formState.value.wholesaleAutoRegister === 1,
    set: (val: boolean) => {
        formState.value.wholesaleAutoRegister = val ? 1 : 0;
    }
});

const loadSetting = async () => {
    loading.value = true;
    try {
        const result = await getWholesaleSetting();
        formState.value = { ...formState.value, ...result };
    } catch (error: any) {
        message.error(error.message || t('加载失败'));
    } finally {
        loading.value = false;
    }
};

const loadGradeOptions = async () => {
    try {
        gradeOptions.value = (await getGradeList({ status: 1, withUserCount: false })) || [];
    } catch (error: any) {
        message.error(error.message || t('等级加载失败'));
    }
};

/** 组装保存入参 */
const buildPayload = () => ({
    wholesaleEnabled: formState.value.wholesaleEnabled,
    wholesaleAutoRegister: formState.value.wholesaleAutoRegister,
    wholesaleDefaultGradeId: formState.value.wholesaleDefaultGradeId ?? 0,
    wholesaleGivePoints: formState.value.wholesaleGivePoints
});

/** 提交：默认批发等级必填（开启批发功能时） */
const onSubmit = async () => {
    const valid = await formRef.value?.validate().catch(() => false);
    if (!valid) return;
    saving.value = true;
    try {
        await saveWholesaleSetting(buildPayload());
        message.success(t('保存成功'));
    } catch (error: any) {
        message.error(error.message || t('保存失败'));
    } finally {
        saving.value = false;
    }
};

onMounted(() => {
    loadSetting();
    loadGradeOptions();
});
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}
</style>
