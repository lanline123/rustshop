import type { FormSchema } from '@/components/schema/types';

export const paymentSettingSchema: FormSchema = {
  title: '支付通道配置',
  apiGet: '/setting/config/wechatPaySettings',
  apiSave: '/setting/config/saveWechatPay',
  labelWidth: '180px',
  sections: [
    {
      title: '微信支付 (WeChat Pay)',
      description: '配置微信商户号及 V3 证书参数',
      fields: [
        {
          prop: 'wechatPayEnabled',
          label: '开启微信支付',
          type: 'switch',
          defaultValue: 1
        },
        {
          prop: 'wechatAppId',
          label: '微信 AppID',
          type: 'input',
          placeholder: '例如：wx1234567890abcdef'
        },
        {
          prop: 'wechatMchId',
          label: '商户号 (MchID)',
          type: 'input',
          placeholder: '例如：1600000000'
        },
        {
          prop: 'wechatApiV3Key',
          label: 'API v3 密钥',
          type: 'password',
          placeholder: '32位微信支付 API v3 密钥'
        }
      ]
    },
    {
      title: '支付宝 (Alipay)',
      description: '配置支付宝开放平台应用参数',
      fields: [
        {
          prop: 'alipayEnabled',
          label: '开启支付宝',
          type: 'switch',
          defaultValue: 1
        },
        {
          prop: 'alipayAppId',
          label: '支付宝 AppID',
          type: 'input',
          placeholder: '例如：2021000000000000'
        },
        {
          prop: 'alipayPrivateKey',
          label: '应用私钥',
          type: 'textarea',
          placeholder: 'RSA2 应用私钥字符串'
        }
      ]
    }
  ]
};
