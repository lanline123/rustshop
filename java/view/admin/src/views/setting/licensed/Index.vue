<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px">
                    <el-form ref="formRef" :model="licensedData" label-width="auto">
                        <div class="title">{{ $t('授权信息') }}</div>
                        <el-form-item :label="$t('授权码：')" prop="license">
                            <div>
                                <div class="textarea-box">
                                    <span class="content">
                                        {{ licensedData.license }}
                                    </span>
                                    <div class="mask-box">
                                        <DialogForm
                                            :maskClose="false"
                                            :isDrawer="false"
                                            path="setting/licensed/Info"
                                            :title="$t('授权码')"
                                            width="600px"
                                            @okCallback="reenter"
                                        >
                                            <el-button class="form-submit-btn" size="large" type="primary">{{ isAuthorized ? $t('重新输入授权码') : $t('输入授权码') }}</el-button>
                                        </DialogForm>
                                    </div>
                                </div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('授权类型：')" prop="licensedTypeName">
                            <TigInput classType="tig-form-input" v-model="licensedData.licensedTypeName" disabled></TigInput>
                        </el-form-item>
                        <el-form-item :label="$t('授权版本：')" prop="isEnterprise">
                            <div class="auth-img">
                                <img :src="isShowAvatar()" style="width: 80px" />
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('是否去版权：')" prop="deCopyright">
                            <span class="green" v-if="licensedData.deCopyright == 1">{{ $t('已去版权') }}</span>
                            <span v-else>{{ $t('否') }}</span>
                        </el-form-item>
                        <el-form-item :label="$t('授权域名：')" prop="authorizedDomain">
                            <div>
                                <div>
                                    <span class="green" v-if="licensedData.authorizedDomain">{{ licensedData.authorizedDomain }}</span>
                                    <span v-else>{{ $t('无') }}</span>
                                </div>
                            </div>
                        </el-form-item>
                        <!-- <div class="title">版权信息</div>
                        <el-form-item label="是否隐藏标题栏版权：" prop="poweredByStatus">
                            <template #label>
                                <el-popover placement="top-end" :width="350" trigger="hover">
                                    <template #reference>
                                        <div class="flex flex-align-center">
                                            <el-icon color="#c8c8c8" size="15"><QuestionFilled /></el-icon>
                                            <p class="ml5">是否隐藏标题栏版权：</p>
                                        </div>
                                    </template>
                                    <template #default>
                                        <img style="width: 300px" src="@/style/images/powered_guide.png" />
                                    </template>
                                </el-popover>
                            </template>
                            <div>
                                <div>
                                    <el-radio-group v-model="licensedData.poweredByStatus" :disabled="licensedStore.licensedData.deCopyright == 0">
                                        <el-radio :value="1">是</el-radio>
                                        <el-radio :value="0">否</el-radio>
                                    </el-radio-group>
                                </div>
                                <div v-if="licensedData.poweredByStatus == 0">
                                    <TigInput
                                        classType="tig-form-input"
                                        v-model="licensedData.poweredBy"
                                        :disabled="licensedStore.licensedData.deCopyright == 0"
                                        placeholder="请输入隐藏标题栏版权文字"
                                    />
                                </div>
                                <div class="extra" v-if="licensedStore.licensedData.deCopyright == 0">购买去版权服务后可修改</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="是否隐藏版本号：" prop="versionInfoHidden">
                            <template #label>
                                <el-popover placement="top-end" :width="450" trigger="hover">
                                    <template #reference>
                                        <div class="flex flex-align-center">
                                            <el-icon color="#c8c8c8" size="15"><QuestionFilled /></el-icon>
                                            <p class="ml5">是否隐藏版本号：</p>
                                        </div>
                                    </template>
                                    <template #default>
                                        <img style="width: 400px" src="@/style/images/version_no.png" />
                                    </template>
                                </el-popover>
                            </template>
                            <div>
                                <div>
                                    <el-radio-group v-model="licensedData.versionInfoHidden" :disabled="licensedStore.licensedData.deCopyright == 0">
                                        <el-radio :value="1">是</el-radio>
                                        <el-radio :value="0">否</el-radio>
                                    </el-radio-group>
                                </div>
                                <div class="extra" v-if="licensedStore.licensedData.deCopyright == 0">购买去版权服务后可修改</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="版权所有" prop="shopCompany">
                            <template #label>
                                <el-popover placement="top-end" :width="650" trigger="hover">
                                    <template #reference>
                                        <div class="flex flex-align-center">
                                            <el-icon color="#c8c8c8" size="15"><QuestionFilled /></el-icon>
                                            <p class="ml5">版权所有：</p>
                                        </div>
                                    </template>
                                    <template #default>
                                        <img style="width: 600px" src="@/style/images/shop_company_guide.png" />
                                    </template>
                                </el-popover>
                            </template>
                            <div>
                                <TigInput
                                    classType="tig-form-input"
                                    v-model="licensedData.shopCompany"
                                    :disabled="licensedStore.licensedData.deCopyright == 0"
                                />
                                <div class="extra" v-if="licensedStore.licensedData.deCopyright == 0">购买去版权服务后可修改</div>
                            </div>
                        </el-form-item>
                         -->
<!--                        <el-form-item label="深色背景LOGO" prop="adminDarkLogo" v-if="isMerchant()">-->
<!--                            <template #label>-->
<!--                                <div class="flex flex-align-center">-->
<!--                                    <el-popover placement="top-end" :width="350" trigger="hover">-->
<!--                                        <template #reference>-->
<!--                                            <div class="flex flex-align-center cursor-pointer">-->
<!--                                                <el-icon color="#c8c8c8" size="15"><QuestionFilled /></el-icon>-->
<!--                                            </div>-->
<!--                                        </template>-->
<!--                                        <template #default>-->
<!--                                            <img style="width: 300px" src="@/style/images/dark_logo_guide.png" />-->
<!--                                        </template>-->
<!--                                    </el-popover>-->
<!--                                    <div class="ml5">深色背景LOGO：</div>-->
<!--                                </div>-->
<!--                            </template>-->
<!--                            <div>-->
<!--                                <div class="img-box">-->
<!--                                    <FormAddGallery v-model:photo="licensedData.adminDarkLogo" style="width: 100%" />-->
<!--                                    <div class="mask-box" v-if="licensedStore.licensedData.deCopyright == 0"></div>-->
<!--                                </div>-->
<!--                                <div class="extra" v-if="licensedStore.licensedData.deCopyright == 0">购买去版权服务后可修改</div>-->
<!--                                <div class="extra" v-else>建议宽度200px，高度40px，格式为png、jpg、jpeg</div>-->
<!--                            </div>-->
<!--                        </el-form-item>-->
<!--                        <el-form-item label="管理后台LOGO" prop="adminLightLogo">-->
<!--                            <template #label>-->
<!--                                <el-popover placement="top-end" :width="350" trigger="hover">-->
<!--                                    <template #reference>-->
<!--                                        <div class="flex flex-align-center">-->
<!--                                            <el-icon color="#c8c8c8" size="15"><QuestionFilled /></el-icon>-->
<!--                                            <p class="ml5">管理后台后台LOGO</p>-->
<!--                                        </div>-->
<!--                                    </template>-->
<!--                                    <template #default>-->
<!--                                        <img style="width: 300px" src="@/style/images/light_logo_guide.png" />-->
<!--                                    </template>-->
<!--                                </el-popover>-->
<!--                            </template>-->
<!--                            <div>-->
<!--                                <div class="img-box">-->
<!--                                    <FormAddGallery v-model:photo="licensedData.adminLightLogo" style="width: 100%" />-->
<!--                                    <div class="mask-box" v-if="licensedStore.licensedData.deCopyright == 0"></div>-->
<!--                                </div>-->
<!--                                <div class="extra" v-if="licensedStore.licensedData.deCopyright == 0">购买去版权服务后可修改</div>-->
<!--                                <div class="extra" v-else>建议宽度200px，高度40px，格式为png、jpg、jpeg</div>-->
<!--                            </div>-->
<!--                        </el-form-item>-->
                        <!-- <el-form-item label=" ">
                            <el-radio-group v-model="licensedData.deCopyright" disabled>
                                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">保 存</el-button>
                            </el-radio-group>
                        </el-form-item> -->
                    </el-form>
                </a-spin>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref } from "vue";
import { DialogForm } from "@/components/dialog";
import { message } from "ant-design-vue";
import { licensedSave } from "@/api/setting/licensed";
import { useLicensedStore } from "@/store/licensed";
import { isShowAvatar } from "@/utils/authorize";
import { isMerchant } from "@/utils/version";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const licensedStore = useLicensedStore();
const licensedData = licensedStore.licensedData;
// 父组件回调
const emit = defineEmits(["submitCallback", "update:confirmLoading", "close"]);
const confirmLoading = ref(false);
const loading = ref<boolean>(false);
const license = ref(licensedData.license || "");
//是否授权
const isAuthorized = ref(license.value ? true : false);
const reenter = () => {
    licensedStore._getLicensed();
    setTimeout(() => {
        location.reload();
    }, 500);
};
onMounted(() => {
    licensedStore._getLicensed();
});
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await licensedSave({
            adminDarkLogo: licensedData.adminDarkLogo,
            adminLightLogo: licensedData.adminLightLogo,
            shopCompany: licensedData.shopCompany,
            poweredBy: licensedData.poweredBy,
            poweredByStatus: licensedData.deCopyright == 0 ? 0 : licensedData.poweredByStatus,
            versionInfoHidden: licensedData.deCopyright == 0 ? 0 : licensedData.versionInfoHidden
        });
        message.success(t('操作成功'));
        reenter();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
// 表单提交
const onFormSubmit = () => {
    onSubmit();
};

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.textarea-box {
    background-color: #f5f7fa;
    width: 430px;
    color: #999;
    height: 120px;
    overflow: hidden;
    border-radius: 5px;
    padding: 10px;
    overflow-wrap: break-word; /* 当单词过长时换行 */
    white-space: normal; /* 允许文本换行 */
    line-height: 16px;
    position: relative;
    .content {
        color: var(--el-disabled-text-color);
    }
    .mask-box {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        border-radius: 5px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 0 0 1px var(--el-disabled-border-color) inset;
    }
}
@media (max-width: 768px) {
    .textarea-box{
        width: 97%;
    }
}
.img-box {
    position: relative;
    width: 80px;
    height: 80px;
    .mask-box {
        position: absolute;
        width: 100%;
        height: 100%;
        top: 0;
        left: 0;
        cursor: not-allowed;
    }
}
.title {
    font-weight: bold;
    padding-top: 20px;
    padding-bottom: 20px;
    font-size: 14px;
}
.auth-img {
    display: flex;
    align-items: center;
}
</style>
