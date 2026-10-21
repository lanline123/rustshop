/** 客户认证详情 - 查看认证申请详情并进行审核（通过/不通过） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-tabs v-model="activeKey" class="lyecs-tabs" tab-position="top" @tab-change="onTabChange">
                    <el-tab-pane :key="1" :label="$t('基础信息')" name="sm"></el-tab-pane>
                    <el-tab-pane v-if="formState.type == 2" :key="2" :label="$t('企业信息')" name="company"></el-tab-pane>
                </el-tabs>
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <div v-if="activeKey == 'sm'">
                        <el-form-item :label="$t('认证资质')">
                            <template v-if="formState.type == 1">
                                <Tag :transparent="true" :text="$t('个人')"></Tag>
                            </template>
                            <template v-else>
                                <Tag color="#409EFF" :transparent="false" :text="$t('企业')"></Tag>
                            </template>
                        </el-form-item>
                        <el-form-item :label="$t('审核结果')">
                            <div class="width100">
                                <div>
                                    <template v-if="formState.status == 1">
                                        <StatusDot :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="formState.status == 2">
                                        <StatusDot color="#52c41a" :flicker="true"></StatusDot>
                                    </template>
                                    <template v-if="formState.status == 3">
                                        <StatusDot color="#f5222d" :flicker="false"></StatusDot>
                                    </template>
                                    {{ formState.statusText }}
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('审核备注')" v-if="formState.auditRemark && formState.status != 1">
                            <div class="width100">
                                <div>
                                    {{ formState.auditRemark }}
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('姓名')">
                            <div class="width100">{{ formState.companyData.corporateName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('联系人手机')">
                            <div class="width100">
                                <MobileCard :mobile="formState.companyData.contactPhone"></MobileCard>
                            </div>
                        </el-form-item>
                        <el-form-item class="form-item" :label="$t('证件类型')">
                            <div class="width100">
                                {{ documentTypeOptions.find((option) => option.value === formState.companyData.documentType)?.label || "" }}
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件号码')">
                            <div class="width100">{{ formState.companyData.documentNumber }}</div>
                        </el-form-item>
                        <!-- <el-form-item label="性别">
                            <div class="width100">{{ formState.companyData.sex == 1 ? "男" : "女" }}</div>
                        </el-form-item> -->
                        <el-form-item :label="$t('居住地址')">
                            <div class="width100">{{ formState.companyData.residentialAddress }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('出生日期')">
                            <div class="width100">{{ formState.companyData.birthday }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('证件有效期')">
                            <div class="width100">
                                <template v-if="formState.companyData.certificateValidityPeriod == 1">
                                    {{
                                        formState.companyData.certificateValidityPeriodDate[0] + " ～ " + formState.companyData.certificateValidityPeriodDate[1]
                                    }}
                                </template>
                                <template v-else>
                                    {{
                                        $t("{certificateValidityPeriodEnd}～ 长期", {
                                            certificateValidityPeriodEnd: formState.companyData.certificateValidityPeriodEnd
                                        })
                                    }}</template
                                >
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件照正面')">
                            <div class="width100">
                                <el-image
                                    :src="imageFormat(formState.companyData.frontOfPhoto[0].picUrl)"
                                    class="image-size"
                                    fit="cover"
                                    @click="showImage(imageFormat(formState.companyData.frontOfPhoto[0].picUrl))"
                                ></el-image>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('证件照反面')">
                            <div class="width100">
                                <el-image
                                    :src="imageFormat(formState.companyData.backOfPhoto[0].picUrl)"
                                    class="image-size"
                                    fit="cover"
                                    @click="showImage(imageFormat(formState.companyData.backOfPhoto[0].picUrl))"
                                ></el-image>
                            </div>
                        </el-form-item>
                        <template v-if="formState.status == 1">
                            <el-form-item :wrapper-col="{ offset: 4, span: 16 }">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit(2)" :loading="auditLoading">{{
                                    $t("审核通过")
                                }}</el-button>
                                <el-button ref="submitBtn" class="form-submit-btn" @click="onSubmit(3)" :loading="auditLoading">{{
                                    $t("审核不通过")
                                }}</el-button>
                            </el-form-item>
                            <TigInput
                                classType="tig-form-input"
                                :rows="4"
                                v-model="formState.auditRemark"
                                :placeholder="$t('请输入审核备注（审核不通过将必填）')"
                                type="textarea"
                            ></TigInput>
                        </template>
                        <el-form-item v-show="!props.isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                            <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t("提交") }}</el-button>
                        </el-form-item>
                    </div>
                    <div v-if="activeKey == 'company'">
                        <el-form-item :label="$t('企业名称')">
                            <div class="width100">{{ formState.companyData.companyName }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('注册地址')">
                            <div class="width100">{{ formState.companyData.licenseAddrProvinceName }} {{ formState.companyData.businessLicenseAddress }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('经营范围')">
                            <div class="width100">{{ formState.companyData.businessScope }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('统一社会信用代码')">
                            <div class="width100">{{ formState.companyData.businessLicenseId }}</div>
                        </el-form-item>
                        <el-form-item :label="$t('营业期限')">
                            <div class="width100">
                                <template v-if="formState.companyData.operatingTermType == 1">
                                    {{ formState.companyData.operatingTermTypeDate[0] + " ～  " + formState.companyData.operatingTermTypeDate[1] }}
                                </template>
                                <template v-else>
                                    {{ $t("{operatingTermTypeEnd}～长期", { operatingTermTypeEnd: formState.companyData.operatingTermTypeEnd }) }}</template
                                >
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('营业执照')">
                            <div class="width100">
                                <template v-if="formState.companyData.businessLicenseImg?.length > 0">
                                    <el-image
                                        :src="imageFormat(formState.companyData.businessLicenseImg[0].picUrl)"
                                        class="image-size"
                                        fit="cover"
                                        @click="showImage(imageFormat(formState.companyData.businessLicenseImg[0].picUrl))"
                                    ></el-image>
                                </template>
                            </div>
                        </el-form-item>
                    </div>
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
        <el-dialog v-model="dialogVisible">
            <div class="image">
                <el-image :src="dialogImageUrl" alt="" fit="cover" />
            </div>
        </el-dialog>
    </div>
</template>
<script lang="ts" setup>
import { nextTick, onMounted, ref, shallowRef } from "vue";
import { SelectRankList } from "@/components/select";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { UserCompanyFormState } from "@/types/user/userCertification.d";
import { getUserCompany, AuditUserCompany } from "@/api/user/userCertification";
import { ElInput } from "element-plus";
import StatusDot from "@/components/form/src/StatusDot.vue";
import MobileCard from "@/components/list/src/MobileCard.vue";
import { Tag } from "@/components/form";
import { imageFormat } from "@/utils/format";
// 父组件回调
import { useI18n } from "vue-i18n";
const { t } = useI18n();
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
const documentTypeOptions = ref([
    {
        value: 1,
        label: t("大陆身份证")
    },
    {
        value: 2,
        label: t("护照（限境外人士）")
    },
    {
        value: 3,
        label: t("来往内地通行证（中国香港居民）")
    },
    {
        value: 4,
        label: t("来往内地通行证（中国澳门居民）")
    },
    {
        value: 5,
        label: t("来往大陆通行证（中国台湾居民）")
    }
]);
const activeKey = ref<string>("sm");
const onTabChange = (val: string) => {
    activeKey.value = val;
};
const loading = ref<boolean>(true);
const auditLoading = ref<boolean>(false); // 添加审核加载状态
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<UserCompanyFormState>({
    companyData: {
        certificateValidityPeriodDate: [],
        frontOfPhoto: [],
        backOfPhoto: [],
        businessLicenseImg: [],
        operatingTermTypeDate: []
    }
});

const fetchUser = async () => {
    try {
        loading.value = true;
        const result = await getUserCompany(action.value, { id: id.value });
        console.log(result);
        Object.assign(formState.value, result);
        formState.value.contactMobile = formState.value.contactMobile?.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchUser();
    } else {
        loading.value = false;
    }
});

// 表单通过验证后提交
const onSubmit = async (type?: number) => {
    // 如果是审核操作，检查重复点击
    if (type && (type === 2 || type === 3)) {
        if (auditLoading.value) {
            return; // 防止重复点击
        }
        auditLoading.value = true;
    }

    try {
        await formRef.value.validate();

        if (type == 3 && !formState.value.auditRemark) {
            message.error(t("请输入审核备注"));
            return;
        }

        emit("update:confirmLoading", true);
        const result = await AuditUserCompany({ id: id.value, status: type, auditRemark: formState.value.auditRemark });
        emit("submitCallback", result);
        message.success(t("操作成功"));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        emit("update:confirmLoading", false);
        if (type && (type === 2 || type === 3)) {
            auditLoading.value = false; // 重置审核加载状态
        }
    }
};
const dialogImageUrl = ref("");
const dialogVisible = ref(false);
const showImage = (url: string) => {
    dialogImageUrl.value = url;
    dialogVisible.value = true;
};
</script>
<style lang="less" scoped>
.width100 {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    word-break: break-word;
}

:deep(.el-tabs__content) {
    display: none;
}

.image-size {
    width: 80px;
    height: 80px;
    border: 1px solid #e0e0e0;
    cursor: pointer;
}

.image {
    width: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}
</style>
