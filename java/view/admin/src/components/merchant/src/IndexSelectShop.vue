<template>
    <div class="select-shop-warp">
        <div class="head-box flex flex-justify-between flex-align-center">
            <div class="title">{{ dialogTitle }}</div>
            <div v-if="createMode === ''" class="organization-search-box">
                <TigInput v-model="searchKeyword" width="100%" :prefix-icon="Search" :placeholder="$t('请输入组织名称')" clearable />
            </div>
            <div class="close" @click="close">
                <i class="iconfont icon-cha1"></i>
            </div>
        </div>
        <div v-if="createMode === ''">
            <a-spin :spinning="loading" style="width: 100%; margin-top: 100px; height: 200px">
                <div class="warp-info" v-if="!loading">
                    <el-scrollbar style="height: 100%">
                        <div class="list-box">
                            <div v-if="showSearchEmpty" class="search-empty">
                                <img src="@/assets/search-empty.png" alt="" />
                                <div>{{ $t('暂无搜索结果') }}</div>
                            </div>
                            <div class="info-item admin" @click="toIndex(0, 'admin')"
                                v-if="showAdmin">
                                <div class="logo">
                                    <img src="@/assets/logo/admin_logo.png" alt="" />
                                </div>
                                <div> {{ $t('管理后台') }} <span v-if="userinfo && userinfo.username && userinfo.username !== ''">（{{
                                        userinfo?.username }}）</span>
                                </div>
                            </div>
                            <!-- <div class="info-item supplier" @click="toIndex(0)">
                            <div class="logo">
                                <img src="@/assets/logo/supplier_logo.png" alt="" />
                            </div>
                            <div>供应商后台</div>
                        </div> -->
                            <template v-if="filteredShopList.length > 0">
                                <div class="info-item" v-for="item in filteredShopList" :key="item.shopId"
                                    @click="toIndex(item.shopId, 'shop')">
                                    <div class="logo">
                                        <el-image v-if="item.shopLogo" class="shop-img"
                                            :src="imageFormat(item.shopLogo)" fit="cover" />
                                        <img v-else src="@/assets/logo/merchant_logo.png" alt="" />
                                    </div>
                                    <div class="shop-name">
                                        <p class="label shop" v-if="item.shopType === 1">{{ $t('店铺') }}</p>
                                        <p class="label store" v-if="item.shopType === 2">{{ $t('门店') }}</p>
                                        <p class="label pickup" v-if="item.shopType === 3">{{ $t('自提点') }}</p>
                                        <p class="line1">{{ item.shopTitle }}</p>
                                    </div>
                                </div>
                            </template>
                            <template v-if="filteredVendorList.length > 0">
                                <div class="info-item" v-for="item in filteredVendorList" :key="item.vendorId"
                                    @click="toIndex(item.vendorId, 'vendor')">
                                    <div class="logo">
                                        <img v-if="item.gysLogo" :src="imageFormat(item.gysLogo)" alt="" />
                                        <img v-else src="@/assets/logo/supplier_logo.png" alt="" />
                                    </div>
                                    <div class="shop-name line1">
                                        <p class="label vendor">{{ $t('供应商') }}</p>
                                        <p class="line1">{{ item.vendorName }}</p>
                                    </div>
                                </div>
                            </template>
                        </div>
                    </el-scrollbar>
                </div>
            </a-spin>
            <div class="btn-box" v-if="canShowCreateShopBtn || canShowCreateStoreBtn">
                <el-button v-if="canShowCreateShopBtn" type="primary" @click="openCreateForm('shop')" style="width: 130px">{{ $t('新建店铺') }}</el-button>
                <el-button v-if="canShowCreateStoreBtn" type="primary" @click="openCreateForm('store')" style="width: 130px">{{ $t('新建门店') }}</el-button>
            </div>
        </div>
        <div v-else>
            <div style="padding: 40px 0">
                <el-form label-width="100" ref="formRef" :model="formState" class="demo-ruleForm" status-icon>
                    <el-form-item v-if="createMode === 'vendor'" :rules="[{ required: true, message: '供应商名称不能为空!' }]" :label="$t('供应商名称')" prop="vendorName">
                        <TigInput classType="tig-form-input" v-model="formState.vendorName" />
                    </el-form-item>
                    <el-form-item v-else-if="createMode === 'store'" :rules="[{ required: true, message: '门店名称不能为空!' }]" :label="$t('门店名称')" prop="shopTitle">
                        <TigInput classType="tig-form-input" v-model="formState.shopTitle" />
                    </el-form-item>
                    <el-form-item v-else :rules="[{ required: true, message: '店铺名称不能为空!' }]" :label="$t('店铺名称')" prop="shopTitle">
                        <TigInput classType="tig-form-input" v-model="formState.shopTitle" />
                    </el-form-item>
                    <el-form-item label="">
                        <el-button type="primary" :loading="formLoading" @click="onSubmit()"> {{ $t('创建') }} </el-button>
                        <el-button @click="cancelCreateForm">{{ $t('取消') }}</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from "vue";
import { createShop, checkIsAdmin } from "@/api/merchant/shop";
import { createVendor } from "@/api/vendor/suppliers";
import { urlFormat, imageFormat } from "@/utils/format";
import { message } from "ant-design-vue";
import { getAdminType, getShopType } from "@/utils/storage";
import gysLogo from "@/assets/logo/gys.png";
import { useI18n } from "vue-i18n"
import { Search } from "@element-plus/icons-vue";
const { t } = useI18n()
const props = defineProps({
    selectShopFlag: {
        type: Boolean,
        default: false
    },
    myShopList: {
        type: Array as () => any[],
        default: () => []
    },
    vendorList: {
        type: Array as () => any[],
        default: () => []
    },
    posList: {
        type: Array as () => any[],
        default: () => []
    },
    loading: {
        type: Boolean,
        default: false
    },
    userinfo: {
        type: Object,
        default: () => { }
    },
    adminType: {
        type: String,
        default: ""
    }
});

const searchKeyword = ref("");
const normalizedSearchKeyword = computed(() => searchKeyword.value.trim().toLocaleLowerCase());
const includesSearchKeyword = (name?: string) => !normalizedSearchKeyword.value || String(name ?? "").toLocaleLowerCase().includes(normalizedSearchKeyword.value);
const filteredPosList = computed(() => props.posList.filter((item) => includesSearchKeyword(item.shopTitle)));
const filteredShopList = computed(() => props.myShopList.filter((item) => includesSearchKeyword(item.shopTitle)));
const filteredVendorList = computed(() => props.vendorList.filter((item) => includesSearchKeyword(item.vendorName)));
const adminSearchText = computed(() => `${t('管理后台')} ${String(props.userinfo?.username ?? '')}`);
const showAdmin = computed(() => props.userinfo.adminType === 'admin' && includesSearchKeyword(adminSearchText.value));
const hasSearchResults = computed(
    () => showAdmin.value || filteredPosList.value.length > 0 || filteredShopList.value.length > 0 || filteredVendorList.value.length > 0
);
const showSearchEmpty = computed(() => normalizedSearchKeyword.value.length > 0 && !hasSearchResults.value);

const isAdmin = ref(false);
onMounted(async () => {
    try {
        const result = await checkIsAdmin();
        isAdmin.value = result?.isAdmin === true;
    } catch {}
});

const canShowCreateShopBtn = computed(
    () => getAdminType() === "shop" && getShopType() === 1 && isAdmin.value
);
const canShowCreateStoreBtn = computed(
    () => getAdminType() === "shop" && getShopType() === 2 && isAdmin.value
);

// 创建态状态机：'' 列表态，'shop'/'vendor'/'store' 对应表单态
type CreateMode = "" | "shop" | "vendor" | "store";
const createMode = ref<CreateMode>("");
const dialogTitle = computed(() => {
    if (createMode.value === "shop") return t('新增店铺');
    if (createMode.value === "store") return t('新增门店');
    if (createMode.value === "vendor") return t('新增供应商');
    return t('选择组织');
});

const formState = ref({
    shopTitle: "",
    vendorName: ""
});
const emit = defineEmits(["closePopup", "update:selectShopFlag", "callBack", "changeShopList", "close"]);
const close = () => {
    emit("close");
};
const formLoading = ref(false);

const openCreateForm = (mode: Exclude<CreateMode, "">) => {
    formState.value.shopTitle = "";
    formState.value.vendorName = "";
    createMode.value = mode;
};
const cancelCreateForm = () => {
    createMode.value = "";
};

// 表单通过验证后按 createMode 走三分支提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        formLoading.value = true;
        if (createMode.value === "vendor") {
            await createVendor({ vendorName: formState.value.vendorName });
        } else if (createMode.value === "store") {
            await createShop({ shopTitle: formState.value.shopTitle, shopType: 2 });
        } else {
            await createShop({ shopTitle: formState.value.shopTitle, shopType: 1 });
        }
        message.success(t('创建成功'));
        createMode.value = "";
        emit("changeShopList", true);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        formLoading.value = false;
    }
};
const formRef = shallowRef();
const toIndex = async (id: number, type: string) => {
    emit("callBack", { id, type });
};
</script>
<style scoped lang="less">
.select-shop-warp {
    padding: 0px 20px;

    .head-box {
        padding: 10px 0 10px 0;
        font-size: 16px;
        gap: 24px;

        .title {
            flex: none;
            white-space: nowrap;
            color: #1f2026;
            font-weight: 500;
        }

        .organization-search-box {
            flex: 0 1 300px;
            width: 300px;
            min-width: 220px;
            margin-right: auto;

            :deep(.el-input__wrapper) {
                border-radius: 4px;
                background: #fff;
                box-shadow: 0 0 0 1px #ebeef5 inset;
                transition: all 0.2s ease;
            }

            :deep(.el-input__wrapper:hover) {
                box-shadow: 0 0 0 1px #dcdfe6 inset;
            }

            :deep(.el-input__wrapper.is-focus) {
                box-shadow: 0 0 0 1px var(--tig-primary) inset;
            }

            :deep(.el-input__prefix) {
                color: #c0c4cc;
                font-size: 16px;
            }
        }
    }

    .btn-box {
        margin-top: 20px;
        text-align: center;
    }

    .warp-info {
        height: 470px;
        overflow-y: auto;

        .list-box {
            padding: 20px 10px;
            display: flex;
            flex-wrap: wrap;
            gap: 25px;

            .search-empty {
                width: 100%;
                min-height: 410px;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                color: #969ba6;
                font-size: 14px;

                img {
                    width: 240px;
                    height: 240px;
                    margin-bottom: -30px;
                    object-fit: contain;
                }
            }

            .info-item {
                width: 200px;
                height: 200px;
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                background: linear-gradient(135deg, #edf0f5 0%, #fafbfc 100%);
                border: 2px solid #ffffff;
                border-radius: 6px;
                box-shadow: 0px 2px 16px 0px rgba(29, 54, 83, 0.08);
                color: #1f2026;
                font-size: 16px;
                transition: all 0.2s;

                &:hover {
                    margin-top: -5px;
                    box-shadow: 0 20px 40px 0 rgba(2, 31, 65, 0.1);
                    cursor: pointer;
                    border-color: var(--tig-primary);
                    background: #fff;
                }

                .logo {
                    img {
                        width: 90px;
                        height: 90px;
                        border-radius: 50px;
                        margin-bottom: 20px;
                    }

                    .shop-img {
                        width: 85px;
                        height: 85px;
                        border-radius: 50px;
                        margin-bottom: 20px;
                    }
                }

                .shop-name {
                    max-width: 80%;
                    display: flex;
                    align-items: center;
                    line-height: 22px;

                    .label {
                        height: 15px;
                        line-height: 15px;
                        border: 1px solid red;
                        padding: 0 2px;
                        font-size: 10px;
                        margin-right: 5px;
                        border-radius: 2px;
                        margin-top: 3px;
                        text-align: center;
                    }

                    .shop {
                        min-width: 25px;
                        color: #1456F0;
                        border-color: #1456F0;
                    }

                    .vendor {
                        min-width: 35px;
                        color: #A203FC;
                        border-color: #A203FC;
                    }

                    .pickup {
                        min-width: 45px;
                        color: #ff6104;
                        border-color: #ff6104;
                    }

                    .store {
                        min-width: 25px;
                        color: #52C41A;
                        border-color: #52C41A;
                    }

                    .pos {
                        min-width: 40px;
                        color: #fd2645;
                        border-color: #fd2645;
                    }
                }
            }
        }
    }
}

@media only screen and (max-width: 767px) {
    .select-shop-warp {
        padding: 0;
    }
}
</style>
