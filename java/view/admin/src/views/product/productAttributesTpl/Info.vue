/**
 * @file 商品属性模板表单
 * @description 添加/编辑商品属性模板，配置属性、规格、附加服务
 * @features
 * - 属性项配置（如颜色、材质等展示属性）
 * - 规格项配置（如尺码，影响SKU和价格）
 * - 附加服务配置（如延保、安装服务）
 * - 支持拖拽排序属性值
 * - 支持多语言
 */
<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-form-table">
                <el-form v-if="!loading" ref="formRef" :model="formState" label-width="auto">
                    <el-form-item :rules="[{ required: true, validator: validateCategory, trigger: 'change' }]" :label="$t('商品类目')" prop="categoryId">
                        <SelectCategory v-model:categoryId="formState.categoryId" :multiple="false" style="width: 350px"></SelectCategory>
                    </el-form-item>
                    <el-form-item :rules="[{ required: true, message: '属性模板名称不能为空!' }]" :label="$t('属性模板名称')" prop="tplName">
                        <BusinessData v-model:modelValue="formState.tplName" :placeholder="$t('请输入属性模板名称')" width="350px" :dataType="8"></BusinessData>
                    </el-form-item>
                    <el-form-item :label="$t('属性')" prop="brandLogo">
                        <!-- <div class="form-item-title">属性</div> -->
                        <div class="attr-list">
                            <div v-if="formState?.tplData?.normal?.length != 0" class="attr-warp" v-for="(item, index1) in formState?.tplData?.normal">
                                <div class="btn-remove" @click="removeAttr('normal', index1)">
                                    <i class="iconfont icon-cha1"></i>
                                </div>
                                <div class="attr-warp-title">
                                    <div class="flex align-center">
                                        <div class="label">{{ $t('属性名：') }}</div>

                                        <div style="width: 300px">
                                            <el-form-item
                                                :prop="`tplData.normal.${index1}.attrName`"
                                                :rules="[{ required: true, message: '属性名不能为空', trigger: 'blur' }]"
                                            >
                                                <BusinessData
                                                    v-model:modelValue="item.attrName"
                                                    :dataType="8"
                                                    :placeholder="$t('请输入属性项，如颜色、尺码、大小')"
                                                ></BusinessData>
                                            </el-form-item>
                                        </div>
                                    </div>
                                </div>
                                <div class="attr-warp-box">
                                    <draggable
                                        class="attr-row-box"
                                        item-key="id"
                                        :list="item.attrValues"
                                        ghost-class="sortable-ghost"
                                        chosen-class="toolChosenClass"
                                        animation="300"
                                        @start=""
                                        @end=""
                                        :group="{ name: 'advanced', pull: false, put: true }"
                                    >
                                        <template #item="{ element, index }">
                                            <div class="draggable-item">
                                                <div class="attr-row">
                                                    <div style="width: 200px">
                                                        <BusinessData
                                                            v-model:modelValue="element.value"
                                                            :dataType="8"
                                                            :placeholder="$t('请输入属性值')"
                                                            @blur=""
                                                            :suffix-icon="Rank"
                                                        ></BusinessData>
                                                    </div>
                                                    <div v-if="item.attrValues.length != 1" class="btn-remove2" @click="removeAttrVal('normal', index1, index)">
                                                        <i class="iconfont icon-cha1"></i>
                                                    </div>
                                                </div>
                                                <el-button
                                                    v-if="index === item.attrValues.length - 1"
                                                    @click="addAttrValue('normal', index1)"
                                                    link
                                                    type="primary"
                                                    class="ml10"
                                                    style="margin-top: 8px"
                                                    ><i class="btn-ico">+</i>{{ $t('增加属性值') }}</el-button
                                                >
                                            </div>
                                        </template>
                                    </draggable>
                                </div>
                            </div>
                            <div class="attr-warp-title">
                                <el-button @click="addAttrList('normal')"><i class="btn-ico">+</i>{{ $t('添加属性项') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('规格')" prop="brandLogo">
                        <!-- <div class="form-item-title">规格</div> -->
                        <div class="attr-list">
                            <div v-if="formState?.tplData?.spe?.length != 0" class="attr-warp" v-for="(item, index1) in formState?.tplData?.spe">
                                <div class="btn-remove" @click="removeAttr('spe', index1)">
                                    <i class="iconfont icon-cha1"></i>
                                </div>
                                <div class="attr-warp-title">
                                    <div class="flex align-center">
                                        <div class="label">{{ $t('规格名：') }}</div>

                                        <div style="width: 300px">
                                            <el-form-item
                                                :prop="`tplData.spe.${index1}.attrName`"
                                                :rules="[{ required: true, message: '规格名不能为空', trigger: 'blur' }]"
                                            >
                                                <BusinessData
                                                    v-model:modelValue="item.attrName"
                                                    :dataType="8"
                                                    :placeholder="$t('请输入规格项，如颜色、尺码、大小')"
                                                ></BusinessData>
                                            </el-form-item>
                                        </div>
                                    </div>
                                </div>
                                <div class="attr-warp-box">
                                    <draggable
                                        class="attr-row-box"
                                        item-key="id"
                                        :list="item.attrValues"
                                        ghost-class="sortable-ghost"
                                        chosen-class="toolChosenClass"
                                        animation="300"
                                        @start=""
                                        @end=""
                                        :group="{ name: 'advanced', pull: false, put: true }"
                                    >
                                        <template #item="{ element, index }">
                                            <div class="draggable-item">
                                                <div class="attr-row">
                                                    <div style="width: 200px">
                                                        <BusinessData
                                                            v-model:modelValue="element.value"
                                                            :dataType="8"
                                                            :placeholder="$t('请输入规格值')"
                                                            @blur=""
                                                            :suffix-icon="Rank"
                                                        ></BusinessData>
                                                    </div>
                                                    <div v-if="item.attrValues.length != 1" class="btn-remove2" @click="removeAttrVal('spe', index1, index)">
                                                        <i class="iconfont icon-cha1"></i>
                                                    </div>
                                                </div>
                                                <el-button
                                                    v-if="index === item.attrValues.length - 1"
                                                    @click="addAttrValue('spe', index1)"
                                                    link
                                                    type="primary"
                                                    class="ml10"
                                                    style="margin-top: 8px"
                                                    ><i class="btn-ico">+</i>{{ $t('增加规格值') }}</el-button
                                                >
                                            </div>
                                        </template>
                                    </draggable>
                                </div>
                            </div>
                            <div class="attr-warp-title">
                                <el-button @click="addAttrList('spe')"><i class="btn-ico">+</i>{{ $t('添加规格项') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('附加服务')" prop="brandLogo">
                        <!-- <div class="form-item-title">附加服务</div> -->
                        <div class="attr-list">
                            <div v-if="formState?.tplData?.extra?.length != 0" class="attr-warp" v-for="(item, index1) in formState?.tplData?.extra">
                                <div class="btn-remove" @click="removeAttr('extra', index1)">
                                    <i class="iconfont icon-cha1"></i>
                                </div>
                                <div class="attr-warp-title">
                                    <div class="flex align-center">
                                        <div class="label">{{ $t('附加服务名：') }}</div>

                                        <div style="width: 300px">
                                            <el-form-item
                                                :prop="`tplData.extra.${index1}.attrName`"
                                                :rules="[{ required: true, message: '附加服务名不能为空', trigger: 'blur' }]"
                                            >
                                                <BusinessData v-model:modelValue="item.attrName" :dataType="8" :placeholder="$t('请输入附加服务名')"></BusinessData>
                                            </el-form-item>
                                        </div>
                                    </div>
                                </div>
                                <div class="attr-warp-box">
                                    <draggable
                                        class="attr-row-box"
                                        item-key="id"
                                        :list="item.attrValues"
                                        ghost-class="sortable-ghost"
                                        chosen-class="toolChosenClass"
                                        animation="300"
                                        @start=""
                                        @end=""
                                        :group="{ name: 'advanced', pull: false, put: true }"
                                    >
                                        <template #item="{ element, index }">
                                            <div class="draggable-item">
                                                <div class="attr-row">
                                                    <div style="width: 205px">
                                                        <BusinessData
                                                            v-model:modelValue="element.value"
                                                            :dataType="8"
                                                            :placeholder="$t('请输入附加服务值')"
                                                            @blur=""
                                                            :suffix-icon="Rank"
                                                        ></BusinessData>
                                                    </div>
                                                    <div v-if="item.attrValues.length != 1" class="btn-remove2" @click="removeAttrVal('extra', index1, index)">
                                                        <i class="iconfont icon-cha1"></i>
                                                    </div>
                                                </div>
                                                <el-button
                                                    v-if="index === item.attrValues.length - 1"
                                                    @click="addAttrValue('extra', index1)"
                                                    link
                                                    type="primary"
                                                    class="ml10"
                                                    style="margin-top: 8px"
                                                    ><i class="btn-ico">+</i>{{ $t('增加附加服务值') }}</el-button
                                                >
                                            </div>
                                        </template>
                                    </draggable>
                                </div>
                            </div>
                            <div class="attr-warp-title">
                                <el-button @click="addAttrList('extra')"><i class="btn-ico">+</i>{{ $t('添加附加服务项') }}</el-button>
                            </div>
                        </div>
                    </el-form-item>
                    <!-- <el-form-item label="附加规格">
                        <div class="attr-warp-box">
                            <el-space v-for="(attr, index) in formState?.tplData?.extra" class="attr-row">
                                <BusinessData v-model:modelValue="attr['attrName']" width="260px" :dataType="8"></BusinessData>
                                <div class="iconfont icon-cha1 btn-remove" @click="removeAttr('extra', Number(index))"></div>
                            </el-space>
                            <el-space class="attr-row">
                                <el-button @click="addAttr('extra')"><i class="btn-ico">+</i>增加附加规格</el-button>
                            </el-space>
                        </div>
                    </el-form-item>
                    <el-form-item v-show="!isDialog" :wrapper-col="{ offset: 4, span: 16 }">
                        <el-button ref="submitBtn" class="form-submit-btn" type="primary" @click="onSubmit()">提交 </el-button>
                    </el-form-item> -->
                </el-form>
                <a-spin :spinning="loading" style="width: 100%; margin-top: 100px" />
            </div>
        </div>
    </div>
</template>
<script lang="ts" setup>
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { SelectCategory } from "@/components/select";
import { onMounted, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import draggable from "vuedraggable";
import { message } from "ant-design-vue";
import { Rank, QuestionFilled } from "@element-plus/icons-vue";
import { ProductAttributesTplFormState } from "@/types/product/productAttributesTpl";
import { getProductAttributesTpl, updateProductAttributesTpl } from "@/api/product/productAttributesTpl";
// 父组件回调
import { useI18n } from "vue-i18n"
const { t } = useI18n()
const emit = defineEmits([
    "submitCallback", //确认后回调
    "update:confirmLoading", //确认按钮的loading状态
    "close" //关闭弹窗
]);
//获取来自父组件的参数
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

const loading = ref<boolean>(true);
const query = useRouter().currentRoute.value.query;
const action = ref<string>(props.isDialog ? props.act : String(query.act));
const id = ref<number>(props.isDialog ? props.id : Number(query.id));
const operation = action.value === "add" ? "create" : "update";
const formRef = shallowRef();
const formState = ref<any>({
    categoryId: "",
    tplName: "",
    tplData: {
        normal: [{ attrName: "", attrValues: [{ value: "", id: Date.now() }] }],
        spe: [{ attrName: "", attrValues: [{ value: "", id: Date.now() }] }],
        extra: [{ attrName: "", attrValues: [{ value: "", id: Date.now() }] }]
    }
});

onMounted(() => {
    if (action.value === "detail") {
        // 获取详情数据
        fetchTpl();
    } else {
        loading.value = false;
    }
});

// 分类选择器清空时会回填 0，required 规则无法拦截，这里单独校验
const validateCategory = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback(new Error(t('请选择商品类目')));
        return;
    }
    callback();
};
// 将字符串数组转换为对象数组（兼容后端数据）
const convertToObjectArray = (data: any) => {
    if (data && data.tplData) {
        ["normal", "spe", "extra"].forEach((type) => {
            if (Array.isArray(data.tplData[type])) {
                data.tplData[type].forEach((item: any) => {
                    // 兼容老数据：当 attrValues 为 null 或 undefined 时，默认为空数组
                    if (item.attrValues == null) {
                        item.attrValues = [{ value: "", id: Date.now() }];
                    } else if (Array.isArray(item.attrValues)) {
                        item.attrValues = item.attrValues.map((val: any, index: number) => {
                            // 如果已经是对象格式，直接返回；否则转换
                            if (typeof val === "object" && val !== null && "value" in val) {
                                return val.id ? val : { ...val, id: Date.now() + index };
                            }
                            return { value: val, id: Date.now() + index };
                        });
                    }
                });
            }
        });
    }
    return data;
};

// 将对象数组转换回字符串数组（用于提交）
const convertToStringArray = (data: any) => {
    const clonedData = JSON.parse(JSON.stringify(data));
    if (clonedData && clonedData.tplData) {
        ["normal", "spe", "extra"].forEach((type) => {
            if (Array.isArray(clonedData.tplData[type])) {
                clonedData.tplData[type].forEach((item: any) => {
                    if (Array.isArray(item.attrValues)) {
                        item.attrValues = item.attrValues.map((val: any) => {
                            return typeof val === "object" && val !== null && "value" in val ? val.value : val;
                        });
                    }
                });
            }
        });
    }
    return clonedData;
};

const fetchTpl = async () => {
    try {
        const result: any = await getProductAttributesTpl(action.value, { id: id.value });
        // let temp:FilterState = result;
        // temp.tplData = temp.tplData?JSON.parse(temp.tplData):{}
        const convertedData = convertToObjectArray(result);
        Object.assign(formState.value, convertedData);
    } catch (error: any) {
        message.error(error.message);
        emit("close");
    } finally {
        loading.value = false;
    }
};

// 移除属性
const removeAttr = (type: string, attrIndex: number) => {
    formState.value.tplData[type].splice(attrIndex, 1);
};

// 移除属性项值
const removeAttrVal = (type: string, attrIndex: number, attrValIndex: number) => {
    formState.value.tplData[type][attrIndex]?.attrValues?.splice(attrValIndex, 1);
};

// 新增属性列表
const addAttrList = (type: string) => {
    // 首先确保 tplData 存在
    if (formState.value.tplData) {
        // 然后检查 tplData 中对应 type 的值是否为数组，如果不是或不存在，则赋值为空数组
        if (!Array.isArray(formState.value.tplData[type])) {
            (formState.value as any).tplData[type] = [{ attrName: "", attrValues: [{ value: "", id: Date.now() }] }];
        } else {
            (formState.value as any).tplData[type].push({ attrName: "", attrValues: [{ value: "", id: Date.now() }] });
        }
    } else {
        let obj: any = {};
        obj[type] = [{ attrName: "", attrValues: [{ value: "", id: Date.now() }] }];
        formState.value.tplData[type].push(obj);
    }
};

const addAttrValue = (type: string, attrIndex: number) => {
    let attrVal = { value: "", id: Date.now() };
    formState.value.tplData[type][attrIndex]?.attrValues.push(attrVal);
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    try {
        // 转换对象数组为字符串数组提交
        const submitData = convertToStringArray(formState.value);
        console.log(submitData);
        emit("update:confirmLoading", true);
        const result = await updateProductAttributesTpl(operation, { id: id.value, ...submitData });
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

defineExpose({
    onFormSubmit
});
</script>
<style lang="less" scoped>
.product-batch {
    margin-bottom: 10px;
}

.attr-list {
    width: 100%;
    border: 1px solid #eeeeee;
    padding: 10px;
    .attr-warp-title {
        background-color: #f7f8fa;
        padding: 10px;
        cursor: pointer;
        position: relative;
        .title {
            margin-bottom: 10px;
            // width: 100px;
        }
        .label {
            width: 84px;
            text-align: right;
        }
        .attr-extra-value {
            margin-left: 85px;
        }
        .btn-remove5 {
            position: absolute;
            top: 10px;
            right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 18px;
            height: 18px;
            line-height: 14px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            i {
                font-size: 10px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove5 {
                opacity: 1;
            }
        }
    }
    .attr-warp {
        margin-bottom: 20px;
        position: relative;

        .attr-warp-box {
            display: flex;
            margin-left: 94px;
            .attr-row-box {
                flex: 1;
                margin-top: 20px;
                display: flex;
                flex-wrap: wrap;
                gap: 10px;
                .attr-row {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .draggable-item {
                    display: flex;
                    align-items: flex-start;
                    .attr-row {
                        position: relative;
                        .btn-remove2 {
                            position: absolute;
                            top: -5px;
                            right: -5px;
                            color: #fff;
                            background-color: #7b7b7b;
                            width: 16px;
                            height: 16px;
                            line-height: 11px;
                            text-align: center;
                            border-radius: 50%;
                            cursor: pointer;
                            opacity: 0;
                            i {
                                font-size: 8px;
                                font-weight: bold;
                            }
                        }
                        &:hover {
                            .btn-remove2 {
                                opacity: 1;
                            }
                        }
                    }
                }
            }
            .attr-btn {
                margin-top: 15px;
                margin-left: 10px;
            }
        }
        .btn-remove {
            position: absolute;
            top: 10px;
            right: 10px;
            color: #fff;
            background-color: #7b7b7b;
            width: 18px;
            height: 18px;
            line-height: 14px;
            text-align: center;
            border-radius: 50%;
            cursor: pointer;
            opacity: 0;
            z-index: 10;
            i {
                font-size: 10px;
                font-weight: bold;
            }
        }
        &:hover {
            .btn-remove {
                opacity: 1;
            }
        }
    }

    .attr-normal-box {
        position: relative;
        .title-box {
            background-color: #f7f8fa;
            padding: 10px;
            margin-bottom: 10px;
            .tit-value {
                display: flex;
                align-items: center;
                .btn-remove3 {
                    margin-left: 10px;
                    color: #fff;
                    background-color: #7b7b7b;
                    width: 16px;
                    height: 16px;
                    line-height: 11px;
                    text-align: center;
                    border-radius: 50%;
                    cursor: pointer;
                    opacity: 0;
                    i {
                        font-size: 8px;
                        font-weight: bold;
                    }
                }
            }
        }
        .attr-normal-value {
            margin-left: 65px;
        }
        &:hover {
            .btn-remove3 {
                opacity: 1 !important;
            }
        }
    }
}
</style>
