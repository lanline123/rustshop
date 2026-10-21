<template>
    <div class="container">
        <div class="content_wrapper">
            <div class="lyecs-table-list-warp">
                <div class="list-table-tool lyecs-search-warp">
                    <div class="notice-warp">
                        <p> {{ $t('提示：') }}</p>
                        <p>{{ $t('1、请先通过“') }}<a href="goods_batch_export.html">{{ $t('商品批量导出') }}</a>{{ $t('”功能导出商品，然后修改该文件并上传，完成对商品的修改！') }}</p>
                        <p>{{ $t('2、字段格式请参考批量上传！') }}</p>
                        <p>{{ $t('3、商品编号是唯一用来匹配所要修改商品的字段！') }}</p>
                        <p class="red">{{ $t('提示：提交后请勿刷新或关闭页面！') }}</p>
                    </div>
                    <div class="lyecs-form-table">
                        <el-form :model="formState" label-width="auto">
                            <el-form-item :rules="[{ required: true, message: '请选择文件!' }]" :label="$t('上传文件')">
                                <el-upload
                                  ref="uploadRef"
                                  :auto-upload="false"
                                  class="upload-demo">
                                    <template #trigger>
                                        <el-button type="primary">{{ $t('选择文件') }}</el-button>
                                    </template>
                                    <el-button v-show="false" class="ml-3" type="success">
                                        upload to server
                                    </el-button>
                                </el-upload>
                            </el-form-item>
                            <el-form-item :label="$t('是否自动添加分类')">
                                <el-radio-group v-model="formState.brandIsHot"  class="width100">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                                <div class="extra">{{ $t('如果选指否，则分类不存在时会输出错误；如果选指是，则会自动新建分类！') }}</div>
                            </el-form-item>
                            <el-form-item :label="$t('是否自动添加品牌')">
                                <el-radio-group v-model="formState.brandIsHot">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                            </el-form-item>
                            <el-form-item :label="$t('是否修改商品相册')">
                                <el-radio-group v-model="formState.brandIsHot" class="width100">
                                    <el-radio :value="1">{{ $t('是') }}</el-radio>
                                    <el-radio :value="0">{{ $t('否') }}</el-radio>
                                </el-radio-group>
                                <div class="extra">{{ $t('为防止误操作，默认不会对文件中的商品相册字段进行修改，如果需要修改，请钩选此项为“是”') }} </div>
                            </el-form-item>
                            <el-form-item label=" ">
                                <el-button ref="submitBtn" class="form-submit-btn" type="primary">{{ $t('提交') }} </el-button>
                                <el-button ref="submitBtn" class="form-submit-btn">{{ $t('重置') }}</el-button>
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import '@/style/css/list.less'
import {onMounted, reactive, ref} from 'vue';
import {useConfigStore} from "@/store/config";

const config = useConfigStore();
// 基本参数定义
const filterResult = reactive([]);
const filter = ref<any>({});        //列表查询参数
const loading = ref(true);          //列表加载中
const page = ref(1);        //页码
const size = ref(config.get('pageSize'));       //分页大小
const total = ref(0);       //总数
const sortfield = ref();    //排序字段
const sortOrder = ref();    //排序升降序
const selectedIds = ref([]);    //钩选项
const advancedSearch = ref(false);
const formState = reactive<any>({   //初使化用于查询的参数
    keyword: '',
    isShow: -1,
    brandIsHot: -1,
});
// 获取列表的查询结果
const loadFilter = async () => {

}
onMounted(async () => {
    loadFilter();
});

</script>
<style lang="less" scoped>
.notice-warp {
    background-color: #eef2ff;
    border-radius: 9px;
    padding: 15px;
    margin-bottom: 20px;
    line-height: 24px;
}

.lyecs-form-table {
    padding: 14px 0;
    background: #fff;
    border-radius: 6px;
    display: flex;
    justify-content: center;
    align-content: center
}
.width100{
    width: 100%;
}
</style>
