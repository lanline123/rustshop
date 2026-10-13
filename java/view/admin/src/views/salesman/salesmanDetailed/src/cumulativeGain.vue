<template>
    <div class="container">
        <div class="salesman-info">
            <div class="salesman-tit flex">
                <div class="logo-info flex">
                    <img class="logo" :src="avatarFormat(formState.baseUserInfo?.avatar)" alt="" />
                    <div class="name">{{ formState.baseUserInfo?.nickname || '--' }}</div>
                </div>
                <div class="logo-info flex">{{ $t('手机号：{mobile}', { mobile: formState.baseUserInfo?.mobile || '--' }) }}
                </div>
                <div class="logo-info flex">{{ $t('等级：{levelText}', { levelText: formState.levelText || '--' }) }}
                </div>
                <div class="logo-info flex">{{ $t('所属分组：{groupName}', { groupName: formState.groupInfo?.groupName || '--' }) }}
                </div>
            </div>
        </div>
        <div class="tit-row">
            <div class="txt"> {{ $t('商品佣金') }} </div>
        </div>
        <div class="num-list flex">
            <div class="item">
                <div class="txt">
                    <p>{{ $t('商品佣金') }}({{ config.config.dollarSignCn }})</p>
                </div>
                <div class="num">
                    {{ priceFormat(commission.productTransactionAmount) }}
                </div>
            </div>
            <div class="item">
                <div class="txt">
                    <span>{{ $t('自动结算佣金') }}({{ config.config.dollarSignCn }})</span>
                </div>
                <div class="num">
                    {{ priceFormat(commission.autoSettlementAmount) }}
                </div>
                <div class="tips">{{ $t('含待结算{priceFormat}', { priceFormat: priceFormat(commission.autoWaitSettlementAmount) }) }}
                </div>
            </div>
            <div class="item">
                <div class="txt">
                    <span>{{ $t('人工结算佣金') }}({{ config.config.dollarSignCn }})</span>
                </div>
                <div class="num">
                    {{ priceFormat(commission.artificialSettlementAmount) }}
                </div>
                <div class="tips">{{ $t('含待结算{priceFormat}', { priceFormat: priceFormat(commission.artificialWaitSettlementAmount) }) }}
                </div>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { QuestionFilled } from "@element-plus/icons-vue";
import { ref, shallowRef, onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { commissionDetails } from "@/api/salesman/promoter";
import { avatarFormat } from "@/utils/format";
import { priceFormat } from "@/utils/format";
import { useConfigStore } from "@/store/config";
const config: any = useConfigStore();
const props = defineProps({
    id: {
        type: Number,
        default: 0
    },
    isDialog: {
        type: Boolean,
        default: false
    }
});
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const formState = ref<any>({});
const commission = ref<any>({});
onMounted(() => {
    _getsalesman();
});
const _getsalesman = async () => {
    try {
        const result = await commissionDetails({
            salesmanId: props.id
        });
        Object.assign(formState.value, result.item);
        Object.assign(commission.value, result.commission);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        loading.value = false;
    }
};
</script>
<style lang="less" scoped>
.container{
    .salesman-info{
        margin-bottom: 20px;
        .salesman-tit{
            justify-content: space-between;
            margin-bottom: 15px;
            .logo-info{
                align-items: center;
                .logo{
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    margin-right: 10px;
                }
                .lable{
                    color: #155bd4;
                    border: #356fd4 1px solid;
                    padding: 3px;
                    margin-left: 10px;
                }
            }
        }
        .row{
            justify-content: space-between;
            padding: 0 40px;
            .item{
                margin-bottom: 15px;
            }
        }
    }
    .tit-row{
        background-color: #f7f7f7;
        padding: 10px;
        margin-bottom: 20px;
        .txt{
            border-left: 3px solid #155bd4;
            padding-left: 10px;
        }
    }
    .num-list{
        .item{
            width: 200px;
            .txt{
                display: flex;
                align-items: center;
                p{
                    margin-right: 5px;
                }
            }
            .num{
                font-size: 20px;
                font-weight: 600;
                margin: 15px 0 10px 0;
            }
            .tips{
                color: #999;
            }
        }
    }
}
</style>
