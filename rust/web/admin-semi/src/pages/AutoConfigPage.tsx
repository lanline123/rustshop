import { useEffect, useMemo, useState } from 'react';
import { Button, Card, Input, InputNumber, Switch, TextArea, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

interface AutoConfigPageProps {
  title: string;
  getApi: string;
  saveApi: string;
}

const labelMap: Record<string, string> = {
  shopName: '商城名称',
  shopTitle: '首页 SEO 标题',
  shopTitleSuffix: 'SEO 标题后缀',
  shopKeywords: '首页 SEO 关键词',
  shopDesc: '首页 SEO 描述',
  shopIcpNo: 'ICP 备案号',
  shop110No: '公安备案号',
  shop110Link: '公安备案链接',
  shopCompany: '公司信息',
  shopCompanyTxt: '版权信息',
  defaultCopyright: '默认版权',
  defaultTechSupport: '技术支持',
  kefuAddress: '客服地址',
  lightShopLogo: '浅色 Logo',
  darkShopLogo: '深色 Logo',
  icoImg: 'ICO 图标',
  adminLightLogo: '后台 Logo',
  adminDomain: '后台域名',
  pcDomain: 'PC 域名',
  h5Domain: 'H5 域名',
  isOpenPscws: '开启 PC 客服',
  langOn: '开启多语言',
  navTheme: '导航主题',
  layout: '布局',
  defaultCountry: '默认国家',
  autoRedirect: '自动跳转',
  productCommentEnabled: '开启商品评价',
  anonymousCommentEnabled: '匿名评价',
  showMarketprice: '显示市场价',
  showSelledCount: '显示销量',
  marketPriceRate: '市场价系数',
  snPrefix: '商品编号前缀',
  customProductNameLength: '商品名称长度',
  dollarSign: '货币符号',
  dollarSignCn: '货币单位',
  canInvoice: '允许开发票',
  invoiceAdded: '发票附加',
  integralName: '积分名称',
  integralPercent: '积分比例',
  autoCancelOrderMinute: '自动取消订单（分钟）',
  autoConfirmReceiptDays: '自动确认收货（天）',
  autoDeliveryDays: '自动发货（天）',
  afterSalesLimitDays: '售后期限（天）',
  afterSalesReview: '售后审核',
  afterSalesReasonEnabled: '售后原因启用',
  kefuPhone: '客服电话',
  kefuTime: '客服时间',
  kefuType: '客服类型',
  kefuCode: '客服代码',
  kefuCodeBlank: '客服代码新窗口',
  kefuWorkwxId: '企业微信 ID',
  corpId: '企业 ID',
  wechatAppId: '微信 AppID',
  wechatAppSecret: '微信 AppSecret',
  wechatServerToken: '微信服务器 Token',
  wechatServerSecret: '微信服务器 Secret',
  googleLoginOn: 'Google 登录',
  googleClientId: 'Google Client ID',
  googleClientSecret: 'Google Client Secret',
  facebookLoginOn: 'Facebook 登录',
  facebookClientId: 'Facebook Client ID',
  facebookClientSecret: 'Facebook Client Secret',
  twitterLoginOn: 'Twitter 登录',
  twitterClientId: 'Twitter Client ID',
  twitterClientSecret: 'Twitter Client Secret',
  usernamePrefix: '用户名前缀',
  openEmailRegister: '邮箱注册',
  isOpenMobileAreaCode: '手机区号',
  mapProvider: '地图服务商',
  amapWebKey: '高德 Web Key',
  amapWebSecret: '高德 Web Secret',
  amapJsKey: '高德 JS Key',
  amapJsSecret: '高德 JS Secret',
  amapMiniKey: '高德小程序 Key',
  amapMiniSecret: '高德小程序 Secret',
  tiandituWebTk: '天地图 Web TK',
  tiandituJsTk: '天地图 JS TK',
  imageUploadMaxSize: '图片上传上限（MB）',
  materialUploadNameType: '素材命名方式',
  msgHackWord: '消息屏蔽词',
  themeStyle: '主题风格',
  themeColor: '主题色',
  pageWidth: '页面宽度',
  noticeType: '通知方式',
  mailHost: '邮件服务器',
  mailPort: '邮件端口',
  mailUser: '邮箱账号',
  mailPass: '邮箱密码',
};

function formatLabel(key: string) {
  if (labelMap[key]) return labelMap[key];
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (char) => char.toUpperCase())
    .trim();
}

export default function AutoConfigPage({ title, getApi, saveApi }: AutoConfigPageProps) {
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [jsonDrafts, setJsonDrafts] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiGet<Record<string, unknown>>(getApi)
      .then((result) => {
        const data = result || {};
        setValues(data);
        const drafts: Record<string, string> = {};
        Object.entries(data).forEach(([key, value]) => {
          if (value && typeof value === 'object') drafts[key] = JSON.stringify(value, null, 2);
        });
        setJsonDrafts(drafts);
      })
      .catch((error) => Toast.error({ content: error instanceof Error ? error.message : '加载失败' }))
      .finally(() => setLoading(false));
  }, [getApi]);

  const entries = useMemo(() => Object.entries(values), [values]);

  const update = (key: string, value: unknown) => {
    setValues((current) => ({ ...current, [key]: value }));
  };

  const save = async () => {
    setSaving(true);
    try {
      const payload: Record<string, unknown> = { ...values };
      Object.entries(jsonDrafts).forEach(([key, raw]) => {
        try {
          payload[key] = JSON.parse(raw);
        } catch {
          throw new Error(`${formatLabel(key)} JSON 格式不正确`);
        }
      });
      await apiPost(saveApi, payload);
      Toast.success({ content: '保存成功' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card className="auto-config-card" shadows="hover">
      <div className="auto-config-head">
        <div>
          <h3>{title}</h3>
          <Text type="tertiary" size="small">
            共 {entries.length} 项配置
          </Text>
        </div>
      </div>
      <div className="auto-config-grid">
        {entries.map(([key, value]) => {
          const isObject = value !== null && typeof value === 'object';
          return (
            <div className={`auto-config-field ${isObject ? 'is-wide' : ''}`} key={key}>
              <label>{formatLabel(key)}</label>
              {typeof value === 'boolean' ? (
                <Switch checked={value} onChange={(next) => update(key, next)} />
              ) : typeof value === 'number' ? (
                <InputNumber value={value} onChange={(next) => update(key, Number(next || 0))} />
              ) : isObject ? (
                <TextArea
                  value={jsonDrafts[key] || ''}
                  autosize={{ minRows: 3, maxRows: 8 }}
                  onChange={(next) => setJsonDrafts((current) => ({ ...current, [key]: next }))}
                />
              ) : (
                <Input
                  value={String(value ?? '')}
                  onChange={(next) => update(key, next)}
                  mode={/secret|password|pass/i.test(key) ? 'password' : undefined}
                />
              )}
            </div>
          );
        })}
      </div>
      <div className="auto-config-actions">
        <Button theme="solid" type="primary" loading={saving || loading} onClick={save}>
          保存
        </Button>
      </div>
    </Card>
  );
}
