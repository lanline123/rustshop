<template>
    <div class="container">
        <div class="card-row" v-loading="loading">
            <div class="card-box-row flex flex-wrap">
                <div class="card-box" v-for="item in cardList" :class="cardType[item.accountType - 1]">
                    <img class="bg" src="@/assets/logo/bg.png" alt="" />
                    <div class="logo">
                        <img v-if="item.accountType == 3" src="@/assets/logo/wechat_logo.png" alt="" />
                        <img v-if="item.accountType == 2" src="@/assets/logo/alipay_logo.png" alt="" />
                        <img v-if="item.accountType == 1" src="@/assets/logo/bank_card_logo.png" alt="" />
                    </div>
                    <div class="info">
                        <div class="tit">
                            <span v-if="item.accountType == 3">{{ $t('微信昵称') }}</span>
                            <span v-if="item.accountType == 2">{{ $t('支付宝昵称') }}</span>
                            <span v-if="item.accountType == 1">{{ item.bankName }}</span>
                        </div>
                        <div class="txt">{{ item.accountNo }}</div>
                    </div>
                    <div class="set-box">
                        <div class="name-box">
                            <div class="tit">{{ $t('持卡人') }}</div>
                            <div class="name">
                                {{ item.accountName }}
                            </div>
                        </div>
                        <div class="btn-box">
                            <DeleteRecord :params="{ id: item.accountId }" :requestApi="delAccount" @afterDelete="loadFilter()">
                                <div class="btn">{{ $t('删除') }}</div>
                            </DeleteRecord>
                            <DialogForm
                                :params="{ act: 'detail', id: item.accountId }"
                                isDrawer
                                path="merchant/capitalfund/account/Info"
                                :title="$t('编辑账户')"
                                width="600px"
                                @okCallback="loadFilter"
                            >
                                <div class="btn">{{ $t('编辑') }}</div>
                            </DialogForm>
                        </div>
                    </div>
                </div>

                <DialogForm :params="{ act: 'add' }" isDrawer path="merchant/capitalfund/account/Info" :title="$t('添加账户')" width="600px" @okCallback="loadFilter">
                    <div class="card-box add">
                        <div style="text-align: center">
                            <div class="icon-box">
                                <el-icon><Plus /></el-icon>
                            </div>
                            <div class="txt">{{ $t('添加账户') }}</div>
                        </div>
                    </div>
                </DialogForm>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
import { onMounted, reactive, ref } from "vue";
import { message } from "ant-design-vue";
import { DeleteRecord } from "@/components/list";
import { DialogForm } from "@/components/dialog";
import { Plus } from "@element-plus/icons-vue";
import { useConfigStore } from "@/store/config";
import { getAccountList, delAccount } from "@/api/merchant/capitalfund/account";
import { AccountFilterState, AccountFilterParams } from "@/types/merchant/capitalfund/account.d";
const cardType = ref(["bank", "alipay", "wechat"]);
const cardList = ref<AccountFilterState[]>([]);
const config: any = useConfigStore();
const loading = ref<boolean>(false);
const filterParams = reactive<AccountFilterParams>({
    //初使化用于查询的参数
    page: 1,
    size: config.get("pageSize"),
    sortField: "",
    sortOrder: ""
});
// 获取列表的查询结果
const loadFilter = async () => {
    loading.value = true;
    try {
        const result = await getAccountList({ ...filterParams });
        cardList.value = result.records;
        console.log(cardList.value);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
onMounted(() => {
    loadFilter();
});
</script>
<style lang="less" scoped>
.card-row {
    min-height: 65vh;
    background-color: #fff;
    padding: 20px;
    .card-box {
        margin-bottom: 15px;
        margin-right: 15px;
        width: 300px;
        min-width: 300px;
        height: 180px;
        border-radius: 5px;
        cursor: pointer;
        position: relative;
        .bg {
            position: absolute;
            width: 100%;
            height: 100%;
        }
        .logo {
            text-align: end;
            padding: 15px;
            img {
                height: 30px;
            }
        }
        .info {
            color: #fff;
            padding: 0 20px;
            width: 100%;
            .txt {
                margin-top: 5px;
                font-size: 18px;
                font-weight: 600;
            }
        }
        .set-box {
            position: absolute;
            bottom: 20px;
            width: 100%;
            display: flex;
            justify-content: space-between;
            .name-box {
                margin-left: 20px;
                color: #fff;
                .name {
                    margin-top: 5px;
                    font-size: 14px;
                    font-weight: 600;
                }
            }
            .btn-box {
                margin-right: 20px;
                display: flex;
                .btn {
                    min-width: 25px;
                    margin-top: 12px;
                    color: #fff;
                    background-color: rgba(255, 255, 255, 0.4);
                    height: 25px;
                    line-height: 25px;
                    padding: 0 10px;
                    border-radius: 20px;
                    margin-left: 10px;
                }
            }
        }
    }
    .wechat {
        background-color: #09d288;
    }
    .alipay {
        background-color: #1a8bfd;
    }
    .bank {
        background-color: #f95266;
    }
    .add {
        border: 1px dashed #155bd4;
        background-color: #f7f8fa;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        .icon-box {
            display: inline-block;
            padding: 6px;
            background-color: #fff;
            border-radius: 100px;
            margin-bottom: 10px;
            :deep(.el-icon) {
                color: #155bd4;
                font-size: 20px;
                font-weight: 600;
            }
        }
    }
}
</style>
