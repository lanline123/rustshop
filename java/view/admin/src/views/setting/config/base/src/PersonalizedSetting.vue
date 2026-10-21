/** * @file 个性化设置页 * @description 配置商城个性化显示选项 * @features * - 设置"猜你喜欢"功能和名称 * - 配置会员默认头像 * - 设置店内搜索功能 * -
配置分享海报样式 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="120px" style="margin-left: 22px">
                <div class="content_wrapper">
                    <div class="title" id="weChatofficialaccount">{{ $t("猜你喜欢") }}</div>
                    <el-form-item :label="$t('自定义名称')" prop="guestLikeGoodsName">
                        <div>
                            <TigInput width="300px" v-model="formState.guestLikeGoodsName" :maxlength="10" showWordLimit />
                            <div class="extra">{{ $t("该名称会显示在移动端买家个人中心及购物车下方，默认名称“猜你喜欢”") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('猜你喜欢')" prop="isGuestLikeGoods">
                        <div>
                            <el-checkbox v-model="formState.isGuestLikeGoods" :label="$t('启用猜你喜欢')" :true-label="1" :false-label="0" />
                            <div class="extra">{{ $t("启用后，移动端买家商城个人中心及购物车将会显示此模块") }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('会员默认头像')" prop="defaultAvatar">
                        <FormAddGallery v-model:photo="formState.defaultAvatar" style="width: 100%" />
                        <div class="extra">{{ $t("请上传1:1的200*200像素以内的图片") }}</div>
                    </el-form-item>
                </div>
                <div v-if="!isB2CPro()" class="content_wrapper" style="margin-top: 15px">
                    <div class="title">{{ searchScopeTitle }}</div>
                    <template v-if="!isB2CPro() && !isStore()">
                        <el-form-item :label="searchScopeTitle" prop="searchSelfStore">
                            <div>
                                <el-checkbox
                                    v-model="formState.searchSelfStore"
                                    :label="$t('开启{searchScopeTitle}功能', { searchScopeTitle })"
                                    :true-label="1"
                                    :false-label="0"
                                />
                                <div class="extra">
                                    {{
                                        $t("开启后，移动端、PC端买家在{currentSearchEntity}中可搜索当前{currentSearchEntity1}商品。", {
                                            currentSearchEntity,
                                            currentSearchEntity1: currentSearchEntity
                                        })
                                    }}<el-popover :width="800" trigger="hover">
                                        <template #reference>
                                            <a class="btn-link">{{ $t("示例") }}</a>
                                        </template>
                                        <template #default>
                                            <img src="@/style/images/searchShop.png" style="width: 100%; object-fit: contain" />
                                        </template>
                                    </el-popover>
                                </div>
                            </div>
                        </el-form-item>
                    </template>
                    <el-form-item :label="$t('首页搜索')" prop="indexSearchShop">
                        <div>
                            <div style="display: flex; flex-direction: column; align-items: flex-start; gap: 8px">
                                <el-checkbox :model-value="1" :label="$t('搜商品')" :true-label="1" :false-label="0" disabled />
                                <el-checkbox
                                    v-model="formState.indexSearchShop"
                                    :label="!isMerchant() && isStore() ? $t('搜门店') : $t('搜店铺')"
                                    :true-label="1"
                                    :false-label="0"
                                />
                            </div>
                            <div class="extra">
                                {{
                                    $t("开启后，PC端买家在搜索框中可选择搜索商品或{isMerchantisStore}。", {
                                        isMerchantisStore: !isMerchant() && isStore() ? "门店" : "店铺"
                                    })
                                }}<el-popover :width="800" trigger="hover">
                                    <template #reference>
                                        <a class="btn-link">{{ $t("示例") }}</a>
                                    </template>
                                    <template #default>
                                        <img :src="pcIndexSearchImg" style="width: 100%; object-fit: contain" />
                                    </template>
                                </el-popover>
                            </div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
            <div style="height: 30px"></div>
            <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
                <div class="selected-action">
                    <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t("提 交") }} </el-button>
                </div>
            </div>
        </a-spin>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef } from "vue";
import { message } from "ant-design-vue";
import { PersonalizedFormState } from "@/types/setting/config";
import { getPersonalizedSetting, savePersonalizedSetting } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { useRoute } from "vue-router";
import { useThemeStore } from "@/store/theme";
import { FormAddGallery } from "@/components/gallery";
import { isOverseas, isB2CPro, isStore, isMerchant } from "@/utils/version";
import pcIndexSearchDefaultImg from "@/assets/PersonalizedSetting/pcIndexSearch.png";
import pcIndexSearchO2OImg from "@/assets/PersonalizedSetting/pcIndexSearch_o2o.png";
import pcIndexSearchOverseasImg from "@/assets/PersonalizedSetting/pcIndexSearch_overseas.png";

import { useI18n } from "vue-i18n";
const { t } = useI18n();
const { themeInfo } = useThemeStore();
const configStore = useConfigStore();
const formRef = shallowRef();
const confirmLoading = ref<boolean>(false);
const pcIndexSearchImg = isOverseas() ? pcIndexSearchOverseasImg : !isMerchant() && isStore() ? pcIndexSearchO2OImg : pcIndexSearchDefaultImg;
const currentSearchEntity = !isMerchant() && isStore() ? t("门店") : t("店铺");
const searchScopeTitle = !isMerchant() && isStore() ? t("搜本门店") : t("搜本店");
const formState = ref<PersonalizedFormState>({
    isGuestLikeGoods: 0,
    guestLikeGoodsName: "",
    defaultAvatar: "",
    searchSelfStore: 0,
    indexSearchShop: 0
});

// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getPersonalizedSetting();
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    try {
        const result = await savePersonalizedSetting(formState.value);
        message.success(t("修改成功"));
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.content {
    padding: 10px;

    .title {
        font-weight: bold;
        padding-top: 20px;
        padding-bottom: 20px;
        font-size: 14px;
    }

    .subtitle {
        color: #999;
        font-weight: normal;
        font-size: 12px;
    }
}

.mr8 {
    margin-right: 8px;
}

.width60 {
    width: 60px;
}

.ml8 {
    margin-left: 8px;
}

.itemWidth {
    width: 100%;
}

.error {
    color: red;
}

.example-link {
    color: #1890ff;
    cursor: pointer;
    &:hover {
        text-decoration: underline;
    }
}
</style>
