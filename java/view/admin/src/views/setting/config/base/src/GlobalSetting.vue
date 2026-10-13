/** * @file 全局设置页 * @description 配置商城全局参数和SEO设置 * @features * - 后台主题风格切换 * - SEO标题/关键词/描述设置 * - URL重写规则配置 * -
缓存和性能设置 * - 海外版多语言SEO支持 */
<template>
    <div class="container">
        <a-spin :spinning="loading">
            <el-form ref="formRef" :model="formState" label-width="200px" style="display: flex; gap: 12px; flex-direction: column">
                <div class="content_wrapper" v-if="adminType === 'admin'">
                    <div class="title">{{ $t('后台风格') }}</div>
                    <ThemeTogglePopup v-model:formState="formState"></ThemeTogglePopup>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('SEO设置') }}</div>
                    <el-form-item :label="$t('SEO标题后缀')" prop="shopTitleSuffix">
                        <BusinessData v-model:modelValue="formState.shopTitleSuffix" :dataType="5" :dataId="2"> </BusinessData>
                        <div class="extra">{{ $t('该后缀将显示在浏览器的标题栏，与SEO有关，建议长度不要超过20字符，效果如：“购物车 - Tig商城”') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('首页SEO标题')" prop="shopTitle">
                        <BusinessData v-model:modelValue="formState.shopTitle" :dataType="5" :dataId="3"></BusinessData>
                        <div class="extra">{{ $t('商城首页的完整标题（不带后缀）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('首页SEO关键词')" prop="shopKeywords">
                        <BusinessData type="select" v-if="!loading" v-model:modelValue="formState.shopKeywords" :dataType="5" :dataId="4"></BusinessData>
                        <div class="extra">{{ $t('首页关键词，该设置与SEO有关，建议不要超过10个关键词（输入完请按回车键确认）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('首页SEO描述')" prop="shopDesc">
                        <BusinessData v-model:modelValue="formState.shopDesc" :dataType="5" :dataId="5"></BusinessData>
                        <div class="extra">{{ $t('首页描述，该设置与SEO有关，建议长度不要超过50字符') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('ico图标上传')" prop="icoImg">
                        <FormAddGallery v-model:photo="formState.icoImg" style="width: 100%" />
                        <div class="extra">{{ $t('推荐使用高清128*128像素，格式为png或ico') }}</div>
                    </el-form-item>
                </div>
                <!-- <div class="content_wrapper">
                    <div class="title">会员设置</div>
                    <el-form-item label="会员默认头像" prop="defaultAvatar">
                        <FormAddGallery v-model:photo="formState.defaultAvatar" style="width: 100%" />
                        <div class="extra">请上传1:1的200*200像素以内的图片</div>
                    </el-form-item>

                </div> -->

                <div class="content_wrapper" id="domain-setting">
                    <div class="title">{{ $t('域名设置') }}</div>
                    <el-form-item :label="$t('PC客户端域名')" prop="pcDomain" :rules="[{ required: false, validator: validateDomain }]">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.pcDomain" :maxlength="100" placeholder="" />
                            <div class="extra">{{ $t('格式参考：https://www.***.com，填写后查看商城或查看商品默认跳PC端，不填则默认跳转环境解析的域名') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('H5客户端域名')" prop="h5Domain" :rules="[{ required: false, validator: validateDomain }]">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.h5Domain" :maxlength="100" placeholder="" />
                            <div class="extra">{{ $t('格式参考：https://m.***.com，填写后查看商城或查看商品默认跳H5端，不填则默认跳转到PC端') }}</div>
                        </div>
                    </el-form-item>
                    <!-- <el-form-item label="客服系统域名" prop="imDomain">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.imDomain" :maxlength="40"
                                placeholder="" />
                            <div class="extra">格式参考：wss://***.com/ws，填写后在线客服功能将使用该域名</div>
                        </div>
                    </el-form-item> -->
                    <el-form-item :label="$t('自动跳转H5')" prop="autoRedirect">
                        <el-radio-group v-model="formState.autoRedirect" class="itemWidth">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('如果开启，且配置了H5端域名，移动设备打开PC客户端则会自动跳转到H5端域名') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('管理员后台域名')" prop="adminDomain" :rules="[{ required: false, validator: validateDomain }]">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.adminDomain" :maxlength="100" placeholder="" />
                            <div class="extra">{{ $t('格式参考：https://admin.***.com，填写后默认跳后台管理端，不填则默认跳转环境解析的域名') }}</div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('上传设置') }}</div>
                    <el-form-item :label="$t('视频上传大小设置')" prop="uploadMaxSize">
                        <div>
                            <TigInput type="integer" width="150px" v-model="formState.uploadMaxSize" placeholder="">
                                <template #append>MB</template>
                            </TigInput>
                            <div class="extra">{{ $t('单位：MB，默认100MB，上传视频大小限制。') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item
                        :label="$t('图片上传大小设置')"
                        prop="imageUploadMaxSize"
                        :rules="[{ required: true, validator: validateImageUploadMaxSize }]"
                    >
                        <div>
                            <TigInput type="integer" width="150px" v-model="formState.imageUploadMaxSize" placeholder="">
                                <template #append>MB</template>
                            </TigInput>
                            <div class="extra">{{ $t('单位：MB，默认20MB，上传图片大小限制。') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('素材上传设置')" prop="materialUploadNameType">
                        <el-radio-group v-model="formState.materialUploadNameType" class="itemWidth">
                            <el-radio :value="0">{{ $t('系统随机命名') }}</el-radio>
                            <el-radio :value="1">{{ $t('保留原素材名') }}</el-radio>
                        </el-radio-group>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('搜索设置') }}</div>
                    <!-- <el-form-item label="搜索热门关键字" prop="searchKeywords">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.searchKeywords" />
                            <div class="extra">头部搜索框下方显示的搜索关键字,请用半角逗号(,)分隔多个关键字；颜色加亮请用b标签包含</div>
                        </div>
                    </el-form-item> -->
                    <el-form-item :label="$t('搜索敏感词屏蔽')" prop="msgHackWord">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.msgHackWord" />
                            <div class="extra">{{ $t('设置后留言中出现的字符会提示非法，请用“,”号隔开，') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('搜索分词')" prop="isOpenPscws">
                        <el-radio-group v-model="formState.isOpenPscws" class="itemWidth">
                            <el-radio :value="1">{{ $t('开启') }}</el-radio>
                            <el-radio :value="0">{{ $t('关闭') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('开启后将自动分词，比如：儿童机器人，分拆分为：儿童、机器人（会降低搜索结果精准度）') }}</div>
                    </el-form-item>
                    <el-form-item :label="$t('默认搜索引擎')" prop="selectSearchType">
                        <div>
                            <el-radio-group v-model="formState.selectSearchType" class="itemWidth">
                                <el-radio value="es">{{ $t('Elasticsearch（推荐）') }}</el-radio>
                                <el-radio value="mysql">{{ $t('MySQL（降级模式）') }}</el-radio>
                            </el-radio-group>
                            <el-space class="mt20" v-if="formState.selectSearchType === 'es'">
                                <el-button type="primary" plain :loading="InitAllToEsLoadding" @click="_InitAllToEsProduct()">
                                    <i class="iconfont-admin icon-elasticsearchElasticsearch" style="font-size: 18px; margin-right: 5px"></i> {{ $t('同步到搜索引擎') }} </el-button>
                            </el-space>
                            <div class="extra"> {{ $t('选择Elasticsearch后，点击按钮将一次性将所有商品数据同步到搜索引擎中。此操作为异步执行，预计需要几分钟完成，请勿重复点击。') }} </div>
                        </div>
                    </el-form-item>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('地区设置') }}</div>
                    <el-form-item :label="$t('商城默认地区')" prop="shopCountry">
                        <div>
                            <div style="width: 300px">
                                <SelectRegion v-if="!loading" v-model="shopRegions"></SelectRegion>
                            </div>
                            <div class="extra">{{ $t('此地区会在系统未判断出用户IP所在地域时，默认定位地区（仅影响PC端右上角送货地址）') }}</div>
                        </div>
                    </el-form-item>

                    <el-form-item :label="$t('默认国家')" prop="defaultCountry">
                        <div>
                            <el-select v-model="formState.defaultCountry" style="width: 300px">
                                <el-option :value="0" :label="$t('不选择')" />
                                <el-option v-for="item in countryList" :key="item.regionId" :value="item.regionId" :label="item.regionName" />
                            </el-select>
                            <div class="extra"> {{ $t('如果选择了默认国家，则会员在选择地址时默认会显示该国家下的省份或地区，不再显示国家选择（修改设置对已经添加的会员地址不生效）') }} </div>
                        </div>
                    </el-form-item>
                </div>
                <!-- <template v-if="isStore()">
                    <MapSetting></MapSetting>
                </template> -->
                <div class="content_wrapper">
                    <div class="title">{{ $t('ICO图标库') }}</div>
                    <el-form-item :label="$t('自定义ico图标库')" prop="icoDefinedCss">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.icoDefinedCss" />
                            <div>
                                <div class="extra"> {{ $t('自定义ico图标库，前往http://iconfont.cn添加，按本系统教程操作，这里只需填写如：') }}<br />https//at.alicdn.com/t/font_400590_y7t88bzlwhskbj4i.css
                                </div>
                                <div class="extra">{{ $t('您也可以下载至本地服务器，只需填写能访问到iconfont的完整域名和存放路径') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                </div>

                <div class="content_wrapper">
                    <div class="title" id="storageSettings">{{ $t('存储设置') }}</div>
                    <el-form-item :label="$t('存储类型')" prop="storageType">
                        <el-radio-group v-model="formState.storageType" class="itemWidth">
                            <el-radio :value="0">{{ $t('本地存储') }}</el-radio>
                            <el-radio :value="1">{{ $t('阿里云OSS存储') }}</el-radio>
                            <el-radio :value="2">{{ $t('腾讯云COS存储') }}</el-radio>
                            <el-radio :value="3">{{ $t('七牛云KODO存储') }}</el-radio>
                            <el-radio :value="4" v-if="isOverseas()">{{ $t('AWS S3 存储') }}</el-radio>
                        </el-radio-group>
                        <div class="extra">{{ $t('开启云存储后，图片会直接通过接口上传至指定云存储，不会再保存在本地。') }}</div>
                    </el-form-item>
                    <!-- <el-form-item label="图片是否保存全路径" prop="storageSaveFullPath" v-if="formState.storageType > 0">
                        <el-radio-group v-model="formState.storageSaveFullPath" class="itemWidth">
                            <el-radio :value="1">是</el-radio>
                            <el-radio :value="0">否</el-radio>
                        </el-radio-group>
                        <div class="extra">选择是后上传文件会保存全路径进数据库内。(如https://www.****.com/upload/2022/01/01/1.jpg)</div>
                    </el-form-item> -->
                    <el-form-item :label="$t('图片访问域名')" prop="storageLocalUrl" v-if="formState.storageType == 0">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.storageLocalUrl" @blur="validateUrl(formState.storageLocalUrl)" />
                            <div>
                                <div class="error" v-if="isErrorText">{{ $t('图片访问域名结尾必须带斜杠 /') }}</div>
                                <div class="extra">{{ $t('参考格式： https://oss.***.com/') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('图片访问域名')" prop="storageOssUrl" v-if="formState.storageType == 1">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.storageOssUrl" @blur="validateUrl(formState.storageOssUrl)" />
                            <div>
                                <div class="error" v-if="isErrorText">{{ $t('图片访问域名结尾必须带斜杠 /') }}</div>
                                <div class="extra">{{ $t('参考格式： https://oss.***.com/') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('图片访问域名')" prop="storageCosUrl" v-if="formState.storageType == 2">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.storageCosUrl" @blur="validateUrl(formState.storageCosUrl)" />
                            <div>
                                <div class="error" v-if="isErrorText">{{ $t('图片访问域名结尾必须带斜杠 /') }}</div>
                                <div class="extra">{{ $t('参考格式： https://oss.***.com/') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('CDN域名')" prop="storageQiniuUrl" v-if="formState.storageType == 3">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.storageQiniuUrl" @blur="validateUrl(formState.storageQiniuUrl)" />
                            <div>
                                <div class="error" v-if="isErrorText">{{ $t('图片访问域名结尾必须带斜杠 /') }}</div>
                                <div class="extra">{{ $t('参考格式： https://oss.***.com/') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <el-form-item label="Bucket URL" prop="storageS3Url" v-if="formState.storageType == 4">
                        <div>
                            <TigInput classType="tig-form-input" v-model="formState.storageS3Url" @blur="validateUrl(formState.storageS3Url)" />
                            <div>
                                <div class="error" v-if="isErrorText">{{ $t('Bucket URL结尾必须带斜杠 /') }}</div>
                                <div class="extra">{{ $t('参考格式： https://oss.***.com/') }}</div>
                            </div>
                        </div>
                    </el-form-item>
                    <template v-if="formState.storageType != 0">
                        <el-form-item label="AccessKeyId" prop="storageOssAccessKeyId" v-if="formState.storageType == 1">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.storageOssAccessKeyId" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageOssAccessKeyId }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AccessKeyId', content: formState.storageOssAccessKeyId }"
                                        path="setting/config/src/EditSecret"
                                        title="AccessKeyId"
                                        width="600px"
                                        @okCallback="formState.storageOssAccessKeyId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的AccessKeyId') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="SecretId" prop="storageCosSecretId" v-if="formState.storageType == 2">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.storageCosSecretId" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageCosSecretId }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretId', content: formState.storageCosSecretId }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretId"
                                        width="600px"
                                        @okCallback="formState.storageCosSecretId = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的SecretId') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="AccessKey(AK)" prop="storageQiniuAccessKey" v-if="formState.storageType == 3">
                            <!-- <TigInput classType="tig-form-input" v-model="formState.storageQiniuSecretKey" /> -->
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageQiniuAccessKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AccessKey(AK)', content: formState.storageQiniuAccessKey }"
                                        path="setting/config/src/EditSecret"
                                        title="AccessKey(AK)"
                                        width="600px"
                                        @okCallback="formState.storageQiniuAccessKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的AccessKey(AK)') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="Access key ID" prop="storageS3AccessKey" v-if="formState.storageType == 4">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageS3AccessKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AccessKey', content: formState.storageS3AccessKey }"
                                        path="setting/config/src/EditSecret"
                                        title="AccessKey"
                                        width="600px"
                                        @okCallback="formState.storageS3AccessKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的Access key ID') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="AccessKeySecret" prop="storageOssAccessKeySecret" v-if="formState.storageType == 1">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageOssAccessKeySecret }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'AccessKeySecret', content: formState.storageOssAccessKeySecret }"
                                        path="setting/config/src/EditSecret"
                                        title="AccessKeySecret"
                                        width="600px"
                                        @okCallback="formState.storageOssAccessKeySecret = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的AccessKeySecret') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="KeySecret" prop="storageCosSecretKey" v-if="formState.storageType == 2">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageCosSecretKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'KeySecret', content: formState.storageCosSecretKey }"
                                        path="setting/config/src/EditSecret"
                                        title="KeySecret"
                                        width="600px"
                                        @okCallback="formState.storageCosSecretKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的KeySecret') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="SecretKey(SK)" prop="storageQiniuSecretKey" v-if="formState.storageType == 3">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageQiniuSecretKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretKey(SK)', content: formState.storageQiniuSecretKey }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretKey(SK)"
                                        width="600px"
                                        @okCallback="formState.storageQiniuSecretKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的SecretKey(SK)') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item label="Secret access key" prop="storageS3SecretKey" v-if="formState.storageType == 4">
                            <div>
                                <div class="flex flex-align-center" style="gap: 10px">
                                    <div class="secret-txt line1" style="max-width: 380px">
                                        {{ formState.storageS3SecretKey }}
                                    </div>
                                    <DialogForm
                                        :maskClose="false"
                                        :isDrawer="false"
                                        :params="{ title: 'SecretKey', content: formState.storageS3SecretKey }"
                                        path="setting/config/src/EditSecret"
                                        title="SecretKey"
                                        width="600px"
                                        @okCallback="formState.storageS3SecretKey = $event"
                                    >
                                        <el-button style="margin-top: 3px" link type="primary"> {{ $t('编辑') }} </el-button>
                                    </DialogForm>
                                </div>
                                <div class="extra">{{ $t('编辑完成后，点击下方提交按钮确认修改，请确保填写正确的Secret access key') }}</div>
                            </div>
                        </el-form-item>
                        <el-form-item :label="$t('空间名称')" prop="storageOssBucket" v-if="formState.storageType == 1">
                            <TigInput classType="tig-form-input" v-model="formState.storageOssBucket" />
                        </el-form-item>
                        <el-form-item :label="$t('空间名称')" prop="storageCosBucket" v-if="formState.storageType == 2">
                            <TigInput classType="tig-form-input" v-model="formState.storageCosBucket" />
                        </el-form-item>
                        <el-form-item :label="$t('空间名称')" prop="storageQiniuBucket" v-if="formState.storageType == 3">
                            <TigInput classType="tig-form-input" v-model="formState.storageQiniuBucket" />
                        </el-form-item>
                        <el-form-item label="Bucket name" prop="storageS3Bucket" v-if="formState.storageType == 4">
                            <TigInput classType="tig-form-input" v-model="formState.storageS3Bucket" />
                        </el-form-item>
                        <el-form-item :label="$t('空间地域节点')" prop="storageOssRegion" v-if="formState.storageType == 1">
                            <TigInput classType="tig-form-input" v-model="formState.storageOssRegion" />
                            <div class="extra"></div>
                        </el-form-item>
                        <el-form-item :label="$t('空间地域节点')" prop="storageCosRegion" v-if="formState.storageType == 2">
                            <TigInput classType="tig-form-input" v-model="formState.storageCosRegion" />
                            <div class="extra"></div>
                        </el-form-item>
                        <el-form-item :label="$t('存储区域')" prop="storageQiniuRegion" v-if="formState.storageType == 3">
                            <TigInput classType="tig-form-input" v-model="formState.storageQiniuRegion" />
                            <div class="extra"></div>
                        </el-form-item>
                        <el-form-item label="Region" prop="storageS3Region" v-if="formState.storageType == 4">
                            <TigInput classType="tig-form-input" v-model="formState.storageS3Region" />
                        </el-form-item>
                        <!--                        <el-form-item label="Endpoint URL" prop="storageS3Endpoint" v-if="formState.storageType == 4">-->
                        <!--                            <TigInput classType="tig-form-input" v-model="formState.storageS3Endpoint" />-->
                        <!--                        </el-form-item>-->
                    </template>
                </div>
                <div class="content_wrapper">
                    <div class="title">{{ $t('MCP 设置') }}</div>
                    <el-form-item :label="$t('MCP 密钥')">
                        <div>
                            <div class="flex flex-align-center" style="gap: 10px">
                                <TigInput classType="tig-form-input" :modelValue="workBuddySettings.maskedKey || $t('尚未生成')" readonly />
                                <el-button type="primary" plain :loading="workBuddyLoading" :disabled="workBuddyLoadFailed" @click="onGenerateWorkBuddyKey">{{ $t('生成密钥') }}</el-button>
                                <el-button v-if="isPlatformSuperAdmin" type="primary" link :disabled="!workBuddySettings.configured" :loading="copyingWorkBuddyKey" @click="copyWorkBuddyKey">{{ $t('复制密钥') }}</el-button>
                            </div>
                            <div class="error" v-if="workBuddyLoadFailed">{{ $t('MCP 设置加载失败，本次提交不会修改 MCP 密钥') }}</div>
                            <div class="extra">{{ $t('密钥始终掩码显示，可随时复制；生成新密钥后请点击页面底部提交保存。') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('密钥有效期')" prop="workbuddyApiKeyExpiresAt" :rules="workBuddyExpiresAtRules">
                        <div>
                            <el-date-picker
                                v-model="workBuddyExpiresAt"
                                type="datetime"
                                value-format="YYYY-MM-DDTHH:mm:ssZ"
                                :placeholder="$t('请选择到期时间')"
                                :disabled="workBuddyLoading || workBuddyLoadFailed"
                            />
                            <div class="extra">{{ $t('到期后 MCP 请求会被拒绝，请在到期前重新生成密钥。') }}</div>
                        </div>
                    </el-form-item>
                    <el-form-item :label="$t('MCP 连接配置')">
                        <div class="mcp-template-wrapper">
                            <pre id="mcp-connection-template" class="mcp-connection-template">{{ mcpConnectionTemplate }}</pre>
                            <el-button type="primary" link :loading="copyingMcpConfig" @click="copyMcpConnectionTemplate">
                                {{ $t('复制配置模板') }}
                            </el-button>
                            <div class="extra">{{ $t('复制后请将域名和 MCP 密钥替换为当前环境的真实值。') }}</div>
                        </div>
                    </el-form-item>
                </div>
            </el-form>
        </a-spin>
    </div>
    <div style="height: 20px"></div>
    <div class="selected-action-warp selected-warp-left" :style="{ left: themeInfo.layout !== 'topMenu' ? '369px' : '270px' }">
        <div class="selected-action">
            <el-button :loading="confirmLoading" class="form-submit-btn" size="large" type="primary" @click="onSubmit">{{ $t('提 交') }} </el-button>
        </div>
    </div>
</template>

<script lang="ts" setup>
import "@/style/css/list.less";
// import MapSetting from "@/views/setting/config/base/src/MapSetting.vue";
import { DialogForm } from "@/components/dialog";
import { SelectRegion } from "@/components/select";
import { computed, onMounted, ref, shallowRef } from "vue";
import { FormAddGallery } from "@/components/gallery";
import { message } from "ant-design-vue";
import { GlobalConfig } from "@/types/setting/config";
import { getConfigGlobal, saveConfigGlobal, InitAllToEsProduct, getWorkBuddyKeySettings, generateWorkBuddyKey, revealWorkBuddyKey } from "@/api/setting/config";
import { batchUpdateCategoryLink, batchUpdateProductLink } from "@/api/setting/seo";
import { useConfigStore } from "@/store/config";
import BusinessData from "@/components/multilingual/BusinessData.vue";
import { convertNullsToEmptyStrings } from "@/utils/format";
import { ThemeTogglePopup } from "@/components/themetogglepopup";
import { BaseDisplayConfig, Regions } from "@/types/setting/config";
import { isOverseas, isStore } from "@/utils/version";
import { isPositiveIntegerImageUploadMaxSize, normalizeImageUploadMaxSize } from "@/utils/imageUploadLimit";
import { useThemeStore } from "@/store/theme";
import { useUserStore } from "@/store/user";
import { useI18n } from "vue-i18n"
import { copyWorkBuddyText, fromWorkBuddyExpiresAt, getMcpConnectionTemplate, maskWorkBuddyKey, toWorkBuddyExpiresAt, validateWorkBuddyExpiresAt } from "./workbuddySettings";
const { t } = useI18n()
const { themeInfo } = useThemeStore();
const adminType = localStorage.getItem("adminType");
const userStore = useUserStore();
const isPlatformSuperAdmin = computed(() => adminType === "admin" && userStore.userInfo?.authList?.includes("all"));
const configStore = useConfigStore();
const themeStore = useThemeStore();
const formRef = shallowRef();
const shopRegions = ref<number[]>([]);
const confirmLoading = ref<boolean>(false);
const countryList = ref<Regions[]>([]);
const batchCategoryLoading = ref<boolean>(false);
const batchProductLoading = ref<boolean>(false);
const workBuddyLoading = ref<boolean>(false);
const workBuddyLoadFailed = ref<boolean>(false);
const workBuddyExpiresAt = ref<string>("");
const copyingWorkBuddyKey = ref<boolean>(false);
const copyingMcpConfig = ref<boolean>(false);
const mcpConnectionTemplate = getMcpConnectionTemplate();
const pendingWorkBuddyKey = ref<string>("");
const workBuddySettings = ref({ maskedKey: "", expiresAt: undefined as number | undefined, configured: false });
/** 已有密钥或刚生成待提交的密钥，都必须配套一个晚于当前时间的到期时间，否则后端会把密钥判定为无效。 */
const hasWorkBuddyKey = computed(() => workBuddySettings.value.configured || !!pendingWorkBuddyKey.value);
const validateWorkBuddyExpiresAtRule = (_rule: any, _value: unknown, callback: (error?: Error) => void) => {
    if (workBuddyLoadFailed.value || (!hasWorkBuddyKey.value && !workBuddyExpiresAt.value)) {
        callback();
        return;
    }
    if (!validateWorkBuddyExpiresAt(workBuddyExpiresAt.value)) {
        callback(new Error(t("请选择晚于当前时间的密钥到期时间")));
        return;
    }
    callback();
};
const workBuddyExpiresAtRules = computed(() => [{ required: hasWorkBuddyKey.value, validator: validateWorkBuddyExpiresAtRule, trigger: "change" }]);

const formState = ref<Partial<GlobalConfig>>({
    layout: "default",
    navTheme: "dark",
    primaryColor: "blue",
    adminLightLogo: "",
    versionInfoHidden: 0,
    pcDomain: "",
    h5Domain: "",
    adminDomain: "",
    // imDomain: "",
    uploadMaxSize: 100,
    imageUploadMaxSize: 20,
    materialUploadNameType: 0,
    autoRedirect: undefined,
    shopTitle: "",
    shopTitleSuffix: "",
    shopKeywords: "",
    shopDesc: "",
    defaultAvatar: "",
    icoImg: "",
    icoDefinedCss: "",
    storageType: undefined,
    storageSaveFullPath: undefined,
    storageLocalUrl: "",
    storageOssUrl: "",
    storageOssAccessKeyId: "",
    storageOssAccessKeySecret: "",
    storageOssBucket: "",
    storageOssRegion: "",
    storageCosUrl: "",
    storageCosSecretId: "",
    storageCosSecretKey: "",
    storageCosBucket: "",
    storageCosRegion: "",
    langOn: undefined,
    langType: undefined,
    defaultCountry: undefined,
    langVolcengineAccessKey: "",
    langVolcengineSecret: "",
    msgHackWord: "",
    isOpenPscws: "",
    selectSearchType: "",
    shopDefaultRegions: "",
    // searchKeywords: "",
    seoCategoryEnable: undefined,
    seoProductEnable: undefined,
    storageQiniuUrl: "",
    storageQiniuSecretKey: "",
    storageQiniuAccessKey: "",
    storageQiniuBucket: "",
    storageQiniuRegion: "",
    storageS3Url: "",
    storageS3AccessKey: "",
    storageS3SecretKey: "",
    storageS3Bucket: "",
    storageS3Region: "",
    storageS3Endpoint: "",
});
// 加载列表
onMounted(async () => {
    await loadFilter();
});
const scrollToSection = (id: string) => {
    setTimeout(() => {
        const el = document.getElementById(id);
        if (el) {
            el.scrollIntoView({ behavior: "smooth" });
        }
    }, 100);
};
const scrollToStorage = () => scrollToSection("storageSettings");
const validateImageUploadMaxSize = (rule: any, value: unknown, callback: (error?: Error) => void) => {
    if (!isPositiveIntegerImageUploadMaxSize(value)) {
        callback(new Error(t("请输入不小于 1 的整数")));
        return;
    }
    callback();
};
const validateDomain = (rule: any, value: any, callback: any) => {
    if (!value) {
        callback();
        return;
    }
    if (/[\u4e00-\u9fa5]/.test(value)) {
        callback(new Error(t('域名不能包含中文')));
        scrollToSection("domain-setting");
        return;
    }
    if (!/^https?:\/\//i.test(value)) {
        callback(new Error("域名必须以 http:// 或 https:// 开头"));
        scrollToSection("domain-setting");
        return;
    }
    callback();
};
const isErrorText = ref<boolean>(false);
const validateUrl = (url: string | any) => {
    // 只要以/结尾的url才是正确的
    const u = typeof url === "string" ? url : url == null ? "" : String(url);
    isErrorText.value = !(u && u.endsWith("/"));
};
const loading = ref<boolean>(true);
// MCP 设置接口失败只降级该区块，不能拖垮整个全局设置页
const loadFilter = async () => {
    const [configResult, workBuddyResult] = await Promise.allSettled([getConfigGlobal(), getWorkBuddyKeySettings()]);
    if (configResult.status === "fulfilled") {
        const result = configResult.value;
        if (result.shopDefaultRegions) {
            shopRegions.value = result.shopDefaultRegions.split(",").map(Number);
        }
        Object.assign(formState.value, convertNullsToEmptyStrings(result));
        countryList.value = result.countries || [];
    } else {
        message.error(configResult.reason?.message);
    }
    if (workBuddyResult.status === "fulfilled") {
        const workBuddy = workBuddyResult.value;
        workBuddyLoadFailed.value = false;
        workBuddySettings.value = workBuddy;
        formState.value.workbuddyApiKey = workBuddy.maskedKey;
        formState.value.workbuddyApiKeyExpiresAt = workBuddy.expiresAt;
        workBuddyExpiresAt.value = fromWorkBuddyExpiresAt(workBuddy.expiresAt);
    } else {
        workBuddyLoadFailed.value = true;
        message.warning(t("MCP 设置加载失败，本次提交不会修改 MCP 密钥"));
    }
    loading.value = false;
};

const onGenerateWorkBuddyKey = async () => {
    if (!validateWorkBuddyExpiresAt(workBuddyExpiresAt.value)) {
        message.error(t('请选择晚于当前时间的密钥到期时间'));
        return;
    }
    workBuddyLoading.value = true;
    try {
        const result = await generateWorkBuddyKey();
        if (!result.key) throw new Error(t('密钥生成失败'));
        pendingWorkBuddyKey.value = result.key;
        workBuddySettings.value = { maskedKey: maskWorkBuddyKey(result.key), expiresAt: toWorkBuddyExpiresAt(workBuddyExpiresAt.value), configured: true };
        formState.value.workbuddyApiKey = workBuddySettings.value.maskedKey;
        message.success(t('密钥生成成功，请点击页面底部提交保存'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        workBuddyLoading.value = false;
    }
};

const copyWorkBuddyKey = async () => {
    if (!isPlatformSuperAdmin.value) {
        message.error(t('权限不足'));
        return;
    }
    copyingWorkBuddyKey.value = true;
    try {
        const key = pendingWorkBuddyKey.value || (await revealWorkBuddyKey()).key;
        if (!key) throw new Error(t('密钥未配置'));
        await copyWorkBuddyText(key);
        message.success(t('复制成功'));
    } catch (error: any) {
        message.error(error?.message || t('复制失败，请手动复制'));
    } finally {
        copyingWorkBuddyKey.value = false;
    }
};

const copyMcpConnectionTemplate = async () => {
    copyingMcpConfig.value = true;
    try {
        await copyWorkBuddyText(mcpConnectionTemplate);
        message.success(t('配置模板复制成功'));
    } catch (error: any) {
        message.error(error?.message || t('复制失败，请手动复制'));
    } finally {
        copyingMcpConfig.value = false;
    }
};

const InitAllToEsLoadding = ref(false);
const _InitAllToEsProduct = async () => {
    InitAllToEsLoadding.value = true;
    try {
        const result = await InitAllToEsProduct();
        message.success(t('已开始异步更新数据，更新过程可能需要几分钟，请稍后查看结果。'));
        setTimeout(() => {
            InitAllToEsLoadding.value = false;
        }, 1000);
    } catch (error: any) {
        message.error(error.message);
    }
};

const onBatchUpdateCategoryLink = async () => {
    try {
        batchCategoryLoading.value = true;
        const result = await batchUpdateCategoryLink({});
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        batchCategoryLoading.value = false;
    }
};

const onBatchUpdateProductLink = async () => {
    try {
        batchProductLoading.value = true;
        const result = await batchUpdateProductLink({});
        message.success(t('操作成功'));
    } catch (error: any) {
        message.error(error.message);
    } finally {
        batchProductLoading.value = false;
    }
};

// 表单通过验证后提交
const onSubmit = async () => {
    await formRef.value.validate();
    confirmLoading.value = true;
    const storageType = formState.value.storageType;
    let url = "";
    switch (storageType) {
        case 0:
            url = formState.value.storageLocalUrl || "";
            break;
        case 1:
            url = formState.value.storageOssUrl || "";
            break;
        case 2:
            url = formState.value.storageCosUrl || "";
            break;
        case 3:
            url = formState.value.storageQiniuUrl || "";
            break;
        case 4:
            url = formState.value.storageS3Url || "";
            break;
        default:
            url = "";
            break;
    }
    await validateUrl(url);
    if (isErrorText.value) {
        message.error(t('图片访问域名结尾必须带斜杠 /'));
        scrollToStorage();
        confirmLoading.value = false;
        return;
    }
    if (shopRegions.value.length > 0) {
        formState.value.shopDefaultRegions = shopRegions.value.join(",");
    }
    // MCP 设置未加载成功时不回传密钥字段（undefined 会被 JSON 序列化丢弃），避免用空值覆盖服务端已有密钥
    const workBuddyPayload = workBuddyLoadFailed.value
        ? { workbuddyApiKey: undefined, workbuddyApiKeyExpiresAt: undefined }
        : {
              workbuddyApiKey: pendingWorkBuddyKey.value || formState.value.workbuddyApiKey,
              workbuddyApiKeyExpiresAt: toWorkBuddyExpiresAt(workBuddyExpiresAt.value)
          };
    try {
        const result = await saveConfigGlobal({
            ...formState.value,
            ...workBuddyPayload,
            imageUploadMaxSize: normalizeImageUploadMaxSize(formState.value.imageUploadMaxSize)
        });
        if (result) {
            Object.assign(formState.value, result);
            workBuddySettings.value.maskedKey = result.workbuddyApiKey || workBuddySettings.value.maskedKey;
            workBuddySettings.value.expiresAt = result.workbuddyApiKeyExpiresAt;
        }
        pendingWorkBuddyKey.value = "";
        message.success(t('修改成功'));
        await configStore.updateConfig();
        themeStore.getThemeInfo();
        setTimeout(() => {
            location.reload();
        }, 500);
    } catch (error: any) {
        message.error(error.message);
    } finally {
        confirmLoading.value = false;
    }
};
</script>
<style lang="less" scoped>
.content {
    padding: 0 20px;
}

.title {
    font-weight: bold;
    padding-bottom: 30px;
    font-size: 16px;
    line-height: 24px;
}

.subtitle {
    color: #999;
    font-weight: normal;
    font-size: 12px;
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

.seo-link {
    display: flex;
    align-items: center;
    gap: 15px;
}

.link-extra {
    margin-left: 20px;
    margin-bottom: 10px;
    display: flex;
    align-items: center;
    &::before {
        display: none;
    }
}

.mcp-template-wrapper {
    width: min(680px, 100%);
}

.mcp-connection-template {
    box-sizing: border-box;
    width: 100%;
    margin: 0 0 8px;
    padding: 12px 16px;
    overflow-x: auto;
    color: #4e5969;
    font-family: "SFMono-Regular", Consolas, "Liberation Mono", monospace;
    font-size: 13px;
    line-height: 1.65;
    white-space: pre-wrap;
    word-break: break-word;
    background: #f7f8fa;
    border: 1px solid #e5e6eb;
    border-radius: 4px;
}
</style>
