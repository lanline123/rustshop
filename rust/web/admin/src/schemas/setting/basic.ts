import type { FormSchema } from '@/components/schema/types';

export const basicSettingSchema: FormSchema = {
  title: '商城基础设置',
  apiGet: '/setting/config/basicSettings',
  apiSave: '/setting/config/saveBasic',
  labelWidth: '180px',
  sections: [
    {
      title: '商城基本信息',
      description: '配置商城的对外展示名称、LOGO及客服联系方式',
      fields: [
        {
          prop: 'shopName',
          label: '商城名称',
          type: 'input',
          defaultValue: 'TigShop 商城',
          rules: [{ required: true, message: '商城名称不能为空', trigger: 'blur' }],
          extra: '将展示在商城各端顶部、发票及短信签名中'
        },
        {
          prop: 'shopLogo',
          label: '商城 LOGO',
          type: 'image',
          extra: '建议尺寸：240x60 像素，支持 png、jpg、webp 格式'
        },
        {
          prop: 'servicePhone',
          label: '客服电话',
          type: 'input',
          placeholder: '例如：400-888-8888',
          extra: '显示在前端帮助中心和底部客服栏中'
        },
        {
          prop: 'shopDesc',
          label: '商城简介 / SEO描述',
          type: 'textarea',
          placeholder: '请输入用于搜索引擎收录的商城描述'
        }
      ]
    },
    {
      title: '公司与备案信息',
      description: '用于网站底部资质展示与政策合规',
      fields: [
        {
          prop: 'shopCompany',
          label: '公司全称',
          type: 'input',
          placeholder: '例如：某某科技有限公司'
        },
        {
          prop: 'icpNumber',
          label: 'ICP 备案号',
          type: 'input',
          placeholder: '例如：京ICP备12345678号-1'
        },
        {
          prop: 'policeRecordNumber',
          label: '公安联网备案号',
          type: 'input',
          placeholder: '例如：京公网安备 11010802020110号'
        }
      ]
    },
    {
      title: '营业状态控制',
      description: '紧急维护或休业期间可一键关闭商城前台访问',
      fields: [
        {
          prop: 'closeMall',
          label: '商城营业状态',
          type: 'radio',
          defaultValue: 0,
          options: [
            { label: '正常营业', value: 0 },
            { label: '打烊 / 暂停营业', value: 1 }
          ]
        },
        {
          prop: 'closeMallReason',
          label: '打烊关店说明',
          type: 'textarea',
          placeholder: '打烊期间前台用户访问时展示的公告说明',
          hidden: (form) => form.closeMall !== 1
        }
      ]
    }
  ]
};
