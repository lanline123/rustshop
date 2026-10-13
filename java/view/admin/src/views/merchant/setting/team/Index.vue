<template>
    <div class="container">
        <div class="content_wrapper">
            <el-form ref="formRef" :model="formState" label-width="160px">
                <div v-show="activeKey === '店铺设置'" class="content">
                    <div class="title">
                        <span> {{ $t('经营设置') }} </span>
                        <span class="tips"> {{ $t('设置你的网点经营模式；') }} </span>
                    </div>
                    <el-form-item :label="$t('经营状态')" prop="status">
                        <div>
                            <el-radio-group v-model="formState.status" class="itemWidth">
                                <el-radio :value="1" v-if="shopInfo.status != 4">{{ $t('开业') }}</el-radio>
                                <el-radio :value="4">{{ $t('停业') }}</el-radio>
                                <el-radio :value="10" v-if="shopInfo.status != 4">{{ $t('打烊') }}</el-radio>
                            </el-radio-group>
                            <div class="extra">
                                <text v-if="shopInfo.status == 4" class="red">{{ $t('暂停营业中，请联系平台处理。') }}</text>
                                <text v-else>{{ $t('设置休息后，买家将无法在店内消费，请谨慎操作。') }}</text>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('客服电话')" prop="kefuPhone">
                        <TigInput v-model="formState.kefuPhone" width="300px;" />
                    </el-form-item>
                    <el-form-item :label="$t('客服入口页面')" prop="kefuInlet">
                        <el-checkbox-group v-model="formState.kefuInlet">
                            <el-checkbox :value="1" :label="$t('商品详情页')" />
                            <el-checkbox :value="2" :label="$t('订单详情页')" />
                        </el-checkbox-group>
                    </el-form-item>
                    <template v-if="getShopType() == 2">
                        <el-form-item :label="$t('显示分类')" prop="shopShowCategory">
                            <div>
                                <el-radio-group v-model="formState.shopShowCategory.type" class="itemWidth">
                                    <el-radio :value="1">{{ $t('全部平台分类') }}</el-radio>
                                    <el-radio :value="2">{{ $t('自定义平台分类') }}</el-radio>
                                    <!-- <el-radio :value="3">门店分类</el-radio> -->
                                </el-radio-group>
                                <div class="wechat-config" v-if="formState.shopShowCategory.type == 2">
                                    <el-checkbox-group v-model="shopShowCategoryIds">
                                        <el-checkbox v-for="item in options" :label="item.categoryName" :value="item.categoryId" />
                                    </el-checkbox-group>
                                </div>
                            </div>
                        </el-form-item>
                    </template>
                    <el-form-item :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button :loading="confirmLoading" ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit">{{ $t('提交') }}</el-button>
                    </el-form-item>
                </div>
            </el-form>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, ref, shallowRef, computed } from "vue";
import { message } from "ant-design-vue";
import { useUserStore } from "@/store/user";
import type { ShopInfoFormState } from "@/types/merchant/setting/team.d";
import { saveShopSetting } from "@/api/merchant/setting/team";
import { getShopInfo } from "@/api/authority/accountEditing";
import { getShopType } from "@/utils/storage";
import { getAllCategoryList } from "@/api/product/category";
import { CategoryFilterState } from "@/types/product/category";
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const formRef = shallowRef();
// 基本参数定义
const activeKey = ref<string>(t('店铺设置'));
const confirmLoading = ref<boolean>(false);
const formState = ref<ShopInfoFormState>({
    kefuInlet: [],
    shopShowCategory: {
        ids: "",
        type: null
    }
});
const shopShowCategoryIds = ref<number[]>([]);
// 表单通过验证后提交
const onSubmit = async () => {
    confirmLoading.value = true;
    if (getShopType() == 2) {
        if (formState.value.shopShowCategory.type == 2 && shopShowCategoryIds.value.length == 0) {
            message.error(t('请选择显示的分类'));
            confirmLoading.value = false;
            return;
        }
        if (shopShowCategoryIds.value.length > 0) {
            formState.value.shopShowCategory.ids = shopShowCategoryIds.value.join(",");
        }
    }
    try {
        const result = await saveShopSetting({
            ...formState.value
        });
        message.success(t('操作成功'));
        // 更新后台设置项
        const userStore = useUserStore() as any;
        userStore.updateUserInfo();
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
const shopInfo = computed(() => useUserStore().shopInfo);
const loading = ref<boolean>(true);
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getShopInfo();
        if (getShopType() == 2) {
            if (result.shopShowCategory.type == 2 && result.shopShowCategory.ids) {
                shopShowCategoryIds.value = result.shopShowCategory.ids.split(",").map(Number);
            }
        }
        Object.assign(formState.value, result);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
const options = ref<CategoryFilterState[]>([]);
const loadCategory = async () => {
    try {
        const result = await getAllCategoryList();
        options.value = result;
    } catch (error: any) {
        message.error(error.message);
    }
};

if (getShopType() == 2) {
    loadCategory();
}
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.container {
    background-color: #fff;
    margin: 20px;
    .content {
        .title {
            margin: 20px 0 20px 100px;
            line-height: 30px;
            font-size: 14px;
            font-weight: 700;
            .tips {
                display: inline-block;
                margin-left: 10px;
                font-weight: 500;
                color: #999;
            }
        }
    }
}

.wechat-config {
    background: #f5f7fa;
    width: 600px;
    padding: 20px;
    margin-top: 10px;
    .extra {
        margin: 0px 0 0px 0;
    }
}
@media (max-width: 768px) {
    .wechat-config {
        width: 90% !important;
    }
}
</style>
