<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <div class="group activity-base">
                        <div class="group-header">
                            <div class="group__block"></div>
                            <div class="group__title">{{ $t('基础设置') }}</div>
                        </div>
                        <div class="group-content">
                            <el-form-item required :label="$t('活动名称')" prop="name">
                                <BusinessData v-model:modelValue="formState.name" :placeholder="$t('请输入活动名称')" :dataType="4" :dataId="id"></BusinessData>
                            </el-form-item>
                            <el-form-item required :label="$t('活动时间')" prop="activityTime">
                                <div class="flex flex-align-center">
                                    <SelectTimeInterval
                                        v-model:end-date="formState.endTime"
                                        v-model:start-date="formState.startTime"
                                        clearable
                                        type="datetime"
                                        value-format="YYYY-MM-DD HH:mm:ss"
                                        @callback="changeDateType(-1)"
                                    >
                                    </SelectTimeInterval>
                                    <el-radio-group class="itemWidth" v-model="dateType" @change="changeDateType">
                                        <el-radio-button :value="0">{{ $t('1天') }}</el-radio-button>
                                        <el-radio-button :value="1">{{ $t('7天') }}</el-radio-button>
                                        <el-radio-button :value="2">{{ $t('15天') }}</el-radio-button>
                                    </el-radio-group>
                                </div>
                            </el-form-item>
                            <!-- 会员标签二期：参与人群圈选（全部会员 / 部分会员，部分会员下指定会员、会员标签、会员等级三者互斥） -->
                            <el-form-item prop="sendUserTarget" label="参与人群">
                                <SendUserSelector ref="sendUserRef" v-model="formState.sendUserTarget" />
                            </el-form-item>
                        </div>
                    </div>
                    <div class="group activity-rule">
                        <div class="group-header">
                            <div class="group__block"></div>
                            <div class="group__title">{{ $t('活动规则') }}</div>
                        </div>
                        <div class="group-content">
                            <el-form-item prop="expirationTime" :label="$t('成团有效时间')" class="expiration-time-item">
                                <div>
                                    <div class="flex expiration-time-inputs">
                                        <TigInput class="mr10" type="integer" v-model="days" width="120px">
                                            <template #append>{{ $t('日') }}</template>
                                        </TigInput>
                                        <TigInput class="mr10" type="integer" v-model="hours" width="120px">
                                            <template #append>{{ $t('时') }}</template>
                                        </TigInput>
                                        <TigInput type="integer" v-model="minutes" width="120px">
                                            <template #append>{{ $t('分') }}</template>
                                        </TigInput>
                                    </div>
                                    <div class="extra">{{ $t('拼团有效期需在15分钟至30天之间，若设置1日，用户开团后，需要在1日内成团，超时则拼团失败') }}</div>
                                </div>
                            </el-form-item>
                            <!-- <el-form-item v-if="orgRequired" prop="shopIds" label="适用组织" class="org-select-form-item"
                                :rules="[{ required: orgRequired, message: '请至少选择一个组织' }]">
                                <div class="org-select">
                                    <el-radio-group class="org-radio-group" v-model="useOrgType">
                                        <el-radio :value="0">全部组织</el-radio>
                                        <el-radio :value="1">指定组织(店铺/门店）适用</el-radio>
                                    </el-radio-group>
                                    <div v-if="getAdminType() === 'admin' && useOrgType === 1">
                                        <div v-if="orgSelecteds.length" style="padding-left: 24px;">
                                            <el-tag :disable-transitions="false">
                                                {{ orgSelecteDisplay }}
                                            </el-tag>
                                        </div>
                                        <DialogForm dialogClass="noPadding" :maskClose="false"
                                            :params="{ isSelect: true, selectedIds: orgSelecteds }" isDrawer
                                            path="promotion/groupon/src/OrganizePop" title="选择组织" width="1100px"
                                            @okCallback="handleOrgSelectChange">
                                            <div class="org-select-btn">
                                                <el-button type="primary" link>选择组织</el-button>
                                                <el-icon>
                                                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1024 1024">
                                                        <path fill="currentColor"
                                                            d="M340.864 149.312a30.592 30.592 0 0 0 0 42.752L652.736 512 340.864 831.872a30.592 30.592 0 0 0 0 42.752 29.12 29.12 0 0 0 41.728 0L714.24 534.336a32 32 0 0 0 0-44.672L382.592 149.376a29.12 29.12 0 0 0-41.728 0z">
                                                        </path>
                                                    </svg>
                                                </el-icon>
                                            </div>
                                        </DialogForm>
                                    </div>
                                </div>
                            </el-form-item> -->
                        </div>
                    </div>
                    <div class="group activity-guide">
                        <div class="group-header">
                            <div class="group__block"></div>
                            <div class="group__title">{{ $t('活动引导') }}</div>
                        </div>
                        <div class="group-content">
                            <el-form-item :label="$t('PC活动图片')" prop="pcGrouponImage">
                                <FormAddGallery
                                    v-model:photo="formState.pcGrouponImage"
                                    pageType="overseas"
                                    :dataType="7"
                                    :dataId="formState.pcGrouponImage"
                                ></FormAddGallery>
                                <div class="extra"> {{ $t('建议尺寸：1190*264或等比图片') }} <el-popover :width="400" placement="right-end" trigger="click">
                                        <template #reference>
                                            <a>{{ $t('示例') }}</a>
                                        </template>
                                        <template #default>
                                            <img src="@/style/images/promotion/groupon/pc_example.png" style="width: 380px" />
                                        </template>
                                    </el-popover>
                                </div>
                            </el-form-item>
                            <el-form-item :label="$t('H5活动图片')" prop="grouponImage">
                                <FormAddGallery
                                    v-model:photo="formState.grouponImage"
                                    pageType="overseas"
                                    :dataType="7"
                                    :dataId="formState.grouponImage"
                                ></FormAddGallery>
                                <div class="extra">{{ $t('建议尺寸：1125*660或等比图片，将会在默认活动页面顶部展示') }}</div>
                            </el-form-item>

                            <el-form-item :label="$t('专题页分享图')" prop="shareImage">
                                <FormAddGallery
                                    v-model:photo="formState.shareImage"
                                    pageType="overseas"
                                    :dataType="7"
                                    :dataId="formState.shareImage"
                                ></FormAddGallery>
                                <div class="extra">{{ $t('建议尺寸：1000*800或宽高比为5:4的图片，将会分享活动专题页的时候作为小程序分享图') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('活动规则')" prop="grouponExplain">
                                <div class="multilingual-textarea-overlay" style="position: relative; display: inline-block">
                                    <TigInput classType="tig-form-input" v-model="formState.grouponExplain" :row="3" type="textarea" />
                                    <BusinessData
                                        v-model:modelValue="formState.grouponExplain"
                                        :placeholder="$t('请输入活动名称')"
                                        :type="'textarea'"
                                        :dataType="1"
                                    ></BusinessData>
                                </div>
                            </el-form-item>
                        </div>
                    </div>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref, shallowRef, watchEffect, watch } from "vue";
import { useRouter } from "vue-router";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { SelectTimeInterval } from "@/components/select";
import { DialogForm } from "@/components/dialog";
import { GrouponFormState } from "@/types/promotion/groupon";
import { formattedDate } from "@/utils/time";
import { getDays } from "@/utils/util";
import type { FormRules } from "element-plus";
import { message } from "ant-design-vue";
import { getGroupon, updateGroupon } from "@/api/promotion/groupon";
import { getAdminType } from "@/utils/storage";
import { fromPairs } from "lodash-es";
import { ShopFilterState } from "@/types/shop/shop";
import { FormAddGallery } from "@/components/gallery";
import SendUserSelector from "@/components/SendUserSelector/index.vue";
import type { SendUserTarget } from "@/types/user/memberAudience";
import { emptySendUserTarget, normalizeSendUserTarget } from "@/utils/sendUserTarget";

// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);

const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    groupType: {
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
const groupType = ref<number>(props.isDialog ? props.groupType : Number(query.groupType));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const sendUserRef = ref();
const days = ref(0);
const hours = ref(24);
const minutes = ref(0);
const orgSelecteds = ref<ShopFilterState[]>([]);
const useOrgType = ref(0);
const adminType = computed(() => getAdminType());
const orgRequired = computed(() => {
    return adminType.value === "admin" && useOrgType.value === 1;
});
const orgSelecteDisplay = computed(() => {
    if (orgSelecteds.value.length === 0) {
        return "";
    }
    if (orgSelecteds.value.length === 1) {
        return orgSelecteds.value[0].shopTitle ?? "";
    } else {
        return t('{shopTitle}等{length}个组织(店铺/门店）', { shopTitle: orgSelecteds.value[0].shopTitle, length: orgSelecteds.value.length });
    }
});
const orgSelectIds = computed(() => orgSelecteds.value.map((v) => v.shopId));

const getImageUrl = (type: "groupon" | "share", gType: number): string => {
    const imageUrlMap = {
        groupon: new Map<number, string>([
            [1, "https://oss.tigshop.com/img/gallery/202512/17669920938pWamU12MRNnVx5Hr7.jpeg"],
            [2, "https://oss.tigshop.com/img/gallery/202512/1766992210RAJwZi1YjU8RZrOYq3.jpeg"]
        ]),
        share: new Map<number, string>([
            [1, "https://oss.tigshop.com/img/gallery/202512/1766992159YGOvzGsX9KddfypOCM.jpeg"],
            [2, "https://oss.tigshop.com/img/gallery/202512/1766992232DblDOCr5EtynIhN2jv.jpeg"]
        ])
    };

    return imageUrlMap[type].get(gType) || "";
};
const formState = ref<GrouponFormState>({
    groupType: groupType.value || 0,
    startTime: formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss"),
    endTime: formattedDate(getDays(1, "add"), "YYYY-MM-DD HH:mm:ss"),
    expirationTime: 0,
    grouponImage: getImageUrl("groupon", groupType.value),
    pcGrouponImage: "https://oss.tigshop.com/img/gallery/202512/17669918734NtdSTIe0qSga5fRgy.jpeg",
    shareImage: getImageUrl("share", groupType.value),
    grouponExplain: "",
    memberRange: 0,
    memberMarks: [],
    sendUserTarget: emptySendUserTarget()
});

/**
 * 计算拼团有效期
 */
const expTime = () => {
    // 将天、小时、分钟都转换为毫秒
    const daysInMs = days.value * 24 * 60 * 60;
    const hoursInMs = hours.value * 60 * 60;
    const minutesInMs = minutes.value * 60;
    return daysInMs + hoursInMs + minutesInMs;
};

/**
 * 根据毫秒算出具体的天数和小时和分钟
 */
const getTime = (time: number) => {
    const days = Math.floor(time / (24 * 60 * 60));
    const hours = Math.floor((time % (24 * 60 * 60)) / (60 * 60));
    const minutes = Math.floor((time % (60 * 60)) / 60);
    return { days, hours, minutes };
};

watchEffect(() => {
    formState.value.expirationTime = expTime();
});

watch(orgSelectIds, (value) => {
    formState.value.shopIds = orgSelectIds.value;
});

const dateType = ref(0);
const changeDateType = (event: number) => {
    if (event === -1) {
        dateType.value = event;
        return;
    }
    formState.value.startTime = formattedDate(new Date(), "YYYY-MM-DD HH:mm:ss");
    if (event === 0) {
        //1
        formState.value.endTime = formattedDate(getDays(1, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 1) {
        //7
        formState.value.endTime = formattedDate(getDays(7, "add"), "YYYY-MM-DD HH:mm:ss");
    }
    if (event === 2) {
        //15
        formState.value.endTime = formattedDate(getDays(15, "add"), "YYYY-MM-DD HH:mm:ss");
    }
};

/**
 * 选择组织
 */
const handleOrgSelectChange = (value: ShopFilterState[]) => {
    console.log(value);
    orgSelecteds.value = value;
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchGroupon();
    } else {
        loading.value = false;
    }
});

const fetchGroupon = async () => {
    try {
        const result = await getGroupon(action.value, {
            grouponPromotionId: id.value
        });
        // console.log(result);
        Object.assign(formState.value, result);
        // 回显容错
        (formState.value as any).memberRange = Number((formState.value as any).memberRange ?? 0);
        (formState.value as any).memberMarks = Array.isArray((formState.value as any).memberMarks) ? (formState.value as any).memberMarks : [];
        (formState.value as any).sendUserTarget = normalizeSendUserTarget(
            (formState.value as any).sendUserTarget ?? (formState.value as any).memberAudience
        );
        delete (formState.value as any).memberAudience;
        const timespan = getTime(formState.value.expirationTime || 0);
        {
            days.value = timespan.days;
            hours.value = timespan.hours;
            minutes.value = timespan.minutes;
        }
        if (result.shops && result.shops.length > 0) {
            orgSelecteds.value = result.shops;
            if (orgSelectIds.value.length > 0) {
                useOrgType.value = 1;
            }
        }
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    const audienceResult = sendUserRef.value?.validate?.();
    if (audienceResult && !audienceResult.valid) {
        message.error(audienceResult.message);
        return;
    }
    try {
        emit("update:confirmLoading", true);
        // 旧会员标记功能保持禁用（统一传 0/[]）；参与人群以 sendUserTarget 为准
        (formState.value as any).memberRange = 0;
        (formState.value as any).memberMarks = [];
        // 如果选择的是全部组织
        if (useOrgType.value === 0) {
            formState.value.shopIds = [-1];
        }
        const result = await updateGroupon(operation, { id: id.value, ...formState.value });
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

/**
 * 验证活动时间
 * @param rule
 * @param value
 * @param callback
 */
const validActivityTime = (rule: any, value: any, callback: any) => {
    if (!formState.value.startTime && !formState.value.endTime) {
        callback(new Error(t('请输入活动时间')));
        return;
    }
    callback();
};
/**
 * 验证成团有效时间 只能在15分钟到30天之间
 * @param rule
 * @param value
 * @param callback
 */
const validExpirationTime = (rule: any, value: any, callback: any) => {
    if (!formState.value.expirationTime) {
        callback(new Error(t('成团有效时间不能为空!')));
        return;
    }
    if (formState.value.expirationTime < 15 * 60 || formState.value.expirationTime > 30 * 24 * 60 * 60) {
        callback(new Error(t('成团有效时间需在15分钟到30天之间')));
        return;
    }
    callback();
};

interface RuleForm {
    name: string;
    activityTime: number;
    groupType: number;
    expirationTime: number;
}
const rules = reactive<FormRules<RuleForm>>({
    name: [{ required: true, message: t('活动名称不能为空!'), trigger: "blur" }],
    activityTime: [{ required: true, validator: validActivityTime, trigger: "blur" }],
    groupType: [{ required: true, message: t('请至少选择一个组织'), trigger: "blur" }],
    expirationTime: [{ required: true, validator: validExpirationTime, trigger: "blur" }]
});

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.container {
    --color-title: #101010;

    .itemWidth {
        margin-left: 10px;
    }

    .radio-group {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
    }
}

:deep(.el-form-item__label) {
    color: var(--color-title);
}

.group {
    .group-header {
        display: flex;
        align-items: center;
        border-radius: 3px;
        background-color: rgba(245, 247, 250, 1);
        padding: 6px 0 6px 14px;
    }

    .group__block {
        width: 4px;
        height: 16px;
        border-radius: 22px;
        background-color: rgba(20, 86, 240, 1);
    }

    .group__title {
        color: var(--color-title);
        padding-left: 6px;
    }

    .extra {
        padding-top: 11px;
    }

    &.activity-base .group-content {
        padding: 26px 0 18px 0;
    }

    &.activity-rule .group-content {
        padding-top: 22px;

        .el-form-item {
            margin-bottom: 19px;
        }
    }

    &.activity-guide .group-content {
        padding: 24px 0;
    }
}

.org-select-form-item {
    :deep(.el-form-item__error) {
        padding-left: 24px;
    }
}

.org-select {
    display: flex;
    flex-direction: column;
}

.org-radio-group {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
}

.org-select-btn {
    padding-left: 24px;
    display: flex;
    align-items: center;
    color: #1456f0;
}

:deep(.gallery-pic-select) {
    .item-img {
        background: #fff;
    }
}

/* 给右下角翻译按钮预留空间，避免遮挡输入文字 */
:deep(.multilingual-textarea-overlay .el-textarea__inner) {
    padding-bottom: 44px;
}

@media only screen and (max-width: 767px) {
    .expiration-time-item {
        display: flex;
        flex-direction: column;
        align-items: flex-start;

        :deep(.el-form-item__label) {
            width: 100% !important;
            max-width: none !important;
            justify-content: flex-start;
            text-align: left !important;
            white-space: nowrap !important;
        }

        :deep(.el-form-item__content) {
            width: 100%;
            margin-left: 0 !important;
        }
    }

    .expiration-time-inputs {
        display: flex !important;
        flex-direction: row !important;
        flex-wrap: nowrap !important;
        gap: 8px !important;
        width: 100% !important;

        :deep(.tig-input-wrapper) {
            flex: 1 !important;
            width: auto !important;
            min-width: 0 !important;
            margin-right: 0 !important;
        }
    }
}
</style>
