/** 会员等级方案编辑 - 配置会员等级名称和图标（最多10级） */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <div class="title">{{ $t('等级方案') }}</div>
                <el-form ref="formRef" :rules="rules" :model="formState" label-width="auto">
                    <el-form-item prop="data" style="margin-bottom: 0">
                        <div class="flex mb10">
                            <div class="flex">
                                <el-button type="primary" :icon="Plus" link @click="addPreferential" :disabled="formState.data.length >= 10">
                                    {{ formState.data.length >= 10 ? $t('多添加10级') : `添加等级(${formState.data.length}/10)` }}
                                </el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item prop="data">
                        <el-table :data="formState.data" :loading="loading" row-key="userId">
                            <el-table-column :width="120" :label="$t('等级')" prop="level">
                                <template #default="{ row, $index }">
                                    <div class="level_txt">vip{{ row.rankLevel }}</div>
                                </template>
                            </el-table-column>
                            <el-table-column :width="130" :label="$t('会员图标')" prop="rankLogo" align="center">
                                <template #default="{ row, $index }">
                                    <el-tooltip effect="light" placement="bottom" :show-after="150">
                                        <template #content> {{ $t('点击修改') }} </template>
                                        <div>
                                            <FormAddGallery v-model:photo="row.rankLogo" :type="2">
                                                <el-image style="height: 20px" :src="row.rankLogo" />
                                            </FormAddGallery>
                                        </div>
                                    </el-tooltip>
                                </template>
                            </el-table-column>
                            <el-table-column :width="300" :label="$t('会员名称*')" prop="rankName">
                                <template #default="{ row, $index }">
                                    <el-form-item :prop="'data.' + $index + '.rankName'" :rules="rules.rankName">
                                        <div class="flex flex-align-center">
                                            <TigInput v-model="row.rankName" width="100px"></TigInput>
                                        </div>
                                    </el-form-item>
                                </template>
                            </el-table-column>
                            <el-table-column fixed="right" :width="80" :label="$t('操作')" class="setBtn">
                                <template #default="{ row, $index }">
                                    <div v-if="$index === formState.data.length - 1 && formState.data.length > 1">
                                        <el-button type="primary" link size="mini" @click="onDel($index)">{{ $t('删除') }}</el-button>
                                    </div>
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import { Plus } from "@element-plus/icons-vue";
import { onMounted, ref, shallowRef, reactive } from "vue";
import { useRouter } from "vue-router";
import { message } from "ant-design-vue";
import { getUserRank, updateUserRank } from "@/api/user/levelManage";
import type { FormRules } from "element-plus";
import { UserFormState } from "@/types/user/levelManage.d";
import { FormAddGallery } from "@/components/gallery";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
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
const formRef = shallowRef();
const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const nameList: { [key: number]: string } = {
    2: t('铂金会员'),
    3: t('黑金会员'),
    4: t('钻石会员'),
    5: t('至尊会员')
};
const formState = ref<UserFormState>({
    data: [
        {
            rankLevel: 1, // 会员等级
            rankName: t('黄金会员'), // 会员名称
            rankLogo: "https://oss.tigshop.com/img/gallery/202501/1735803176Yh9mCaE2r9ebXK3bGm.png" // 会员logo
        }
    ]
});
interface RuleForm {
    rankName: string;
}
const rules = reactive<FormRules<RuleForm>>({
    rankName: [{ required: true, message: t('请输入'), trigger: "blur" }]
});

const fetchUser = async () => {
    try {
        loading.value = true;
        const result = await getUserRank(action.value);
        formState.value.data = result.userRankList;
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};
const vipIcons = [
    "https://oss.tigshop.com/img/gallery/202501/1737524324ro1DJxNm3aQowZKnPU.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324ZUrDWMFqTPMBTCOlhg.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324ptLNB2WynLJBBwSHHj.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324hhmoz188M0ejyXEBhT.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324O1BXVeurAyDMj9QG8f.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324T7eKXTpMiCeVQOyFb6.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324bUufpiVaGcaUad1CWd.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324NhZbUn45wj1eSaIbjY.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324bv68MBsoQXa2rLUO9i.png",
    "https://oss.tigshop.com/img/gallery/202501/1737524324urqONY3QDjaTqW7d56.png"
];

const addPreferential = async () => {
    await formRef.value.validate();
    if (formState.value.data.length <= 10) {
        let data = {
            rankLevel: formState.value.data.length + 1, // 会员等级
            rankName: nameList[formState.value.data.length + 1], // 会员名称
            rankLogo: vipIcons[formState.value.data.length] // 会员logo
        };
        formState.value.data.push(data);
        await formRef.value.validate();
    }
};
const onDel = (index: number) => {
    formState.value.data.splice(index, 1);
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
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        emit("update:confirmLoading", true);
        const result = await updateUserRank(operation, { id: id.value, ...formState.value });
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

defineExpose({ onFormSubmit });
</script>
<style lang="less" scoped>
.title {
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 20px;
}
.lyecs-form-table {
    .level_txt {
        font-weight: 600;
        color: #ed6a18;
        font-size: 14px;
    }
    :deep(.el-table) {
        th.el-table__cell {
            background-color: #fff;
            padding: 8px 0;
        }
        .cell {
            overflow: visible !important;
        }
        .el-form-item__error {
            position: absolute !important;
            z-index: 99;
            top: 32px !important;
        }
        .el-form-item {
            margin-bottom: 5px;
        }
        .el-input-group__append,
        .el-input-group__prepend {
            padding: 0 10px;
        }
    }
    .table-col {
        width: 100%;
        height: 30px;
        .show {
            display: block;
        }
        .hide {
            display: none;
        }
        &:hover {
            .show {
                display: none;
            }
            .hide {
                display: block;
            }
        }
        &:focus-within .hide {
            display: block;
        }
        &:focus-within .show {
            display: none;
        }
    }
}
</style>
