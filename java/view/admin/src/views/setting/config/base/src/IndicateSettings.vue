/**
 * @file 指示设置组件
 * @description 配置商城通用指示和地区设置（子组件）
 * @features
 * - 敏感词过滤设置
 * - 搜索关键词分词开关
 * - 自营店铺名称设置
 * - 商城默认地区配置
 */
<template>
    <a-spin :spinning="loading">
        <el-form ref="formRef" :model="formState" label-width="140px" style="margin-left: 22px">
            <div class="title" id="generalSetup">{{ $t('通用设置') }}</div>
            <el-form-item :label="$t('敏感词屏蔽')" prop="msgHackWord">
                <TigInput width="100%" v-model="formState.msgHackWord" />
                <div class="extra">{{ $t('设置后留言中出现的字符会提示非法，请用“,”号隔开，') }}</div>
            </el-form-item>
            <el-form-item :label="$t('搜索关键词分词')" prop="isOpenPscws">
                <el-radio-group v-model="formState.isOpenPscws" class="itemWidth">
                    <el-radio :value="1">{{ $t('开启') }}</el-radio>
                    <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                </el-radio-group>
                <div class="extra">{{ $t('开启后将自动分词，比如：儿童机器人，分拆分为：儿童、机器人（会降低搜索结果精准度）') }}</div>
            </el-form-item>
            <el-form-item :label="$t('自营名称')" prop="selfStoreName">
                <TigInput width="100%" v-model="formState.selfStoreName" />
                <div class="extra">{{ $t('比如：官方自营、Tigshop自营，仅对B2B2C多商户版本有效') }}</div>
            </el-form-item>
            <div class="title" id="regionalSettings">{{ $t('地区设置') }}</div>
            <el-form-item :label="$t('商城默认地区')" prop="shopCountry">
                <div class="itemWidth">
                    <SelectRegion v-if="!loading" v-model:selectedIds="shopRegions"></SelectRegion>
                </div>
                <div class="extra">{{ $t('此地区会在系统未判断出用户IP所在地域时，默认定位地区（仅影响PC端右上角送货地址）') }}</div>
            </el-form-item>
            <el-form-item :label="$t('默认国家')" prop="defaultCountry">
                <el-select v-model="formState.defaultCountry" style="width: 100%">
                    <el-option value="0" :label="$t('不选择')" />
                    <el-option v-for="item in countryList" :key="item.regionId" :value="item.regionId" :label="item.regionName" />
                </el-select>
                <div class="extra"> {{ $t('如果选择了默认国家，则会员在选择地址时默认会显示该国家下的省份或地区，不再显示国家选择（修改设置对已经添加的会员地址不生效）') }} </div>
            </el-form-item>
            <div class="title" id="categoryDisplay">{{ $t('分类显示') }}</div>
            <el-form-item :label="$t('商城分类级数')" prop="showCatLevel">
                <el-radio-group v-model="formState.showCatLevel" class="itemWidth">
                    <el-radio :value="1">{{ $t('二级') }}</el-radio>
                    <el-radio :value="0">{{ $t('三级或以上') }}</el-radio>
                </el-radio-group>
                <div class="extra">{{ $t('根据商城情况，选择最多有多少分类级别，会影响到手机端分类展示方式（不推荐只设置一级分类）') }}</div>
            </el-form-item>
        </el-form>
        <div style="height: 20px"></div>
        <div class="selected-action-warp selected-warp-left">
            <div class="selected-action" style="padding-left: 80px">
                <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }}</el-button>
            </div>
        </div>
    </a-spin>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, watch } from "vue";
import { SelectRegion } from "@/components/select";
import { message } from "ant-design-vue";
import { BaseDisplayConfig, Regions } from "@/types/setting/config";
import { getConfigShow, saveConfigShow } from "@/api/setting/config";
import { useConfigStore } from "@/store/config";
import { useRoute } from "vue-router";
import { convertNullsToEmptyStrings } from "@/utils/format"
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const route = useRoute();
const configStore = useConfigStore();
const formRef = shallowRef();
const shopRegions = ref<number[]>([]);
const confirmLoading = ref<boolean>(false);
const formState = ref<BaseDisplayConfig>({
    msgHackWord: "",
    isOpenPscws: "",
    selfStoreName: "",
    shopDefaultRegions: "",
    defaultCountry: 0,
    showCatLevel: 0
});
const countryList = ref<Regions[]>([]);
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const loading = ref<boolean>(true);
const loadFilter = async () => {
    try {
        const result = await getConfigShow();
        if(result.shopDefaultRegions){
            shopRegions.value = result.shopDefaultRegions.split(",").map(Number);
        }
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        countryList.value = result.countries || [];
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    if(shopRegions.value.length > 0){
        formState.value.shopDefaultRegions = shopRegions.value.join(",");
    }
    try {
        const result = await saveConfigShow(formState.value);
        message.success(t('修改成功'));
        configStore.updateConfig();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
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
</style>
