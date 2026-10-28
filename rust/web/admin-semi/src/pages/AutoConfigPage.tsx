import { useEffect, useMemo, useState } from 'react';
import { Button, Card, ColorPicker, Input, InputNumber, Radio, RadioGroup, Switch, TextArea, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import { configLabels } from '../config/configLabels';
import ImageUploader from '../components/ImageUploader';
import RichTextEditor from '../components/RichTextEditor';
import RegionSelect from '../components/RegionSelect';
import SecretInput from '../components/SecretInput';

const { Text } = Typography;

export interface ConfigWhen {
  field?: string;
  equals?: unknown;
  notEquals?: unknown;
  in?: unknown[];
  any?: ConfigWhen[];
  all?: ConfigWhen[];
}

export interface ConfigFieldRule {
  visibleWhen?: ConfigWhen;
  disabledWhen?: ConfigWhen;
}

export interface ConfigFieldGroup {
  title: string;
  description?: string;
  tag?: string;
  icon?: string;
  fields: string[];
  visibleWhen?: ConfigWhen;
}

interface AutoConfigPageProps {
  title: string;
  getApi: string;
  saveApi: string;
  sections?: Array<{ title: string; fields: string[] }>;
  rules?: Record<string, ConfigFieldRule>;
  fieldGroups?: ConfigFieldGroup[];
  controls?: Record<string, 'radio' | 'switch' | 'segmented' | 'color'>;
  options?: Record<string, Array<{ label: string; value: string | number; color?: string }>>;
  hiddenFields?: string[];
}

function matchesCondition(condition: ConfigWhen | undefined, values: Record<string, unknown>): boolean {
  if (!condition) return true;
  if (condition.any) return condition.any.some((item) => matchesCondition(item, values));
  if (condition.all) return condition.all.every((item) => matchesCondition(item, values));
  const current = condition.field ? values[condition.field] : undefined;
  if ('equals' in condition) return String(current) === String(condition.equals);
  if ('notEquals' in condition) return String(current) !== String(condition.notEquals);
  if (condition.in) return condition.in.some((item) => String(item) === String(current));
  return true;
}

const labelMap: Record<string, string> = {
  businessLicense: '营业执照',
  businessLicenseEnabled: '经营许可证开关',
  businessLicenseType: '经营许可证类型',
  closeOrder: '关闭下单',
  closeShop: '关闭商城',
  ediLicense: '电子营业执照',
  shopLogo: '商城 Logo',
  shopRegClosed: '关闭注册',
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
  navTheme: '外观',
  primaryColor: '主题色',
  versionInfoHidden: '是否隐藏版本号',
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
  if (configLabels[key]) return configLabels[key];
  if (labelMap[key]) return labelMap[key];
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (char) => char.toUpperCase())
    .trim();
}

export default function AutoConfigPage({
  title,
  getApi,
  saveApi,
  sections,
  rules,
  fieldGroups,
  controls,
  options,
  hiddenFields,
}: AutoConfigPageProps) {
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

  const grouped = useMemo(() => {
    if (!sections?.length) return [{ title: '', items: entries }];
    const known = new Set(sections.flatMap((section) => section.fields));
    const result = sections
      .map((section) => ({
        title: section.title,
        items: section.fields
          .map((key) => entries.find(([entryKey]) => entryKey === key))
          .filter((item): item is [string, unknown] => Boolean(item)),
      }))
      .filter((section) => section.items.length > 0);
    const rest = entries.filter(([key]) => !known.has(key));
    if (rest.length) {
      if (result.length) result[result.length - 1].items.push(...rest);
      else result.push({ title: '其它', items: rest });
    }
    return result;
  }, [entries, sections]);

  const isFieldVisible = (key: string) => {
    if (hiddenFields?.includes(key)) return false;
    return matchesCondition(rules?.[key]?.visibleWhen, values);
  };
  const isFieldDisabled = (key: string) => {
    const condition = rules?.[key]?.disabledWhen;
    return condition ? matchesCondition(condition, values) : false;
  };

  const renderField = ([key, value]: [string, unknown], disabled = false) => {
    const isObject = value !== null && typeof value === 'object';
    const text = value === null || value === undefined ? '' : String(value);
    const isImage =
      typeof value === 'string' &&
      /(logo|img|image|ico|icon|avatar|banner|photo|qrcode)/i.test(key) &&
      !/(defined|css|size|type|mode|name)/i.test(key);
    const isImageList =
      Array.isArray(value) &&
      value.every((item) => typeof item === 'string') &&
      /(gallery|images|photos|pics|banner)/i.test(key);
    const isColor =
      typeof value === 'string' &&
      /(color|colour)/i.test(key) &&
      /^#([0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})$/i.test(text);
    const isRegion = /(^regionId$|provinceId$|cityId$|areaId$|^region$|^province$|^city$|^area$)/i.test(
      key,
    );
    const isSecret = typeof value === 'string' && /(secret|password|pass|token|key$)/i.test(key);
    const isRichText =
      typeof value === 'string' && /(content|notice|agreement|protocol)/i.test(key);
    const isBinaryFlag =
      (typeof value === 'number' || typeof value === 'string') &&
      ['0', '1'].includes(String(value)) &&
      /(enabled|enable|on$|is[A-Z]|show|open|close|can|use|auto|review|status|powered|hidden|switch|sign|bind)/i.test(
        key,
      ) &&
      !/(type|mode|provider|days|minute|limit|rate|size|count|sort|order|percent|number|price|id|amount)$/i.test(
        key,
      );
    const wide = isObject || isRichText || isImage || isImageList;
    return (
      <div className={`auto-config-field ${wide ? 'is-wide' : ''}`} key={key}>
        <label>{formatLabel(key)}</label>
        {isImageList ? (
          <ImageUploader
            value={(value as string[]) || []}
            onChange={(next) => update(key, next)}
          />
        ) : isImage ? (
          <ImageUploader
            single
            value={text ? [text] : []}
            onChange={(next) => update(key, next[0] || '')}
          />
        ) : isRichText ? (
          <RichTextEditor value={text} onChange={(next) => update(key, next)} height={260} />
        ) : isColor ? (
          <ColorPicker
            value={text as never}
            alpha={false}
            onChange={(color: any) =>
              update(key, typeof color === 'string' ? color : color?.hex || '')
            }
          />
        ) : isRegion ? (
          <RegionSelect value={value as number | string} onChange={(next) => update(key, next)} />
        ) : isSecret ? (
          <SecretInput value={text} onChange={(next) => update(key, next)} />
        ) : controls?.[key] === 'color' ? (
          <div className="auto-config-color-list">
            {(options?.[key] || []).map((option) => (
              <button
                type="button"
                key={String(option.value)}
                title={option.label}
                aria-label={option.label}
                className={`auto-config-color-item ${
                  String(value) === String(option.value) ? 'is-active' : ''
                }`}
                style={{ backgroundColor: option.color }}
                onClick={() => update(key, option.value)}
              />
            ))}
          </div>
        ) : controls?.[key] === 'segmented' ? (
          <RadioGroup
            type="button"
            value={String(value)}
            disabled={disabled}
            options={options?.[key] || []}
            onChange={(event) => update(key, event.target.value)}
          />
        ) : controls?.[key] === 'radio' ? (
          <RadioGroup
            value={String(value)}
            disabled={disabled}
            onChange={(event) => update(key, Number(event.target.value))}
          >
            <Radio value="1">是</Radio>
            <Radio value="0">否</Radio>
          </RadioGroup>
        ) : isBinaryFlag ? (
          <Switch
            disabled={disabled}
            checked={String(value) === '1'}
            onChange={(next) => update(key, next ? 1 : 0)}
          />
        ) : typeof value === 'boolean' ? (
          <Switch disabled={disabled} checked={value} onChange={(next) => update(key, next)} />
        ) : typeof value === 'number' ? (
          <InputNumber
            disabled={disabled}
            value={value}
            onChange={(next) => update(key, Number(next || 0))}
          />
        ) : isObject ? (
          <TextArea
            value={jsonDrafts[key] || ''}
            autosize={{ minRows: 3, maxRows: 8 }}
            onChange={(next) => setJsonDrafts((current) => ({ ...current, [key]: next }))}
          />
        ) : (
          <Input
            disabled={disabled}
            value={String(value ?? '')}
            onChange={(next) => update(key, next)}
            mode={/secret|password|pass/i.test(key) ? 'password' : undefined}
          />
        )}
      </div>
    );
  };

  const visibleEntries = entries.filter(([key]) => {
    if (!isFieldVisible(key)) return false;
    const fieldGroup = fieldGroups?.find((item) => item.fields.includes(key));
    return !fieldGroup || matchesCondition(fieldGroup.visibleWhen, values);
  });

  return (
    <Card className="auto-config-card" shadows="hover">
      <div className="auto-config-head">
        <Text type="tertiary" size="small">
          共 {visibleEntries.length} 项配置
        </Text>
      </div>
      {grouped.map((group) => {
        const renderedGroups = new Set<string>();
        return (
          <div className="auto-config-section" key={group.title || 'all'}>
            {group.title && <h4>{group.title}</h4>}
            <div className="auto-config-grid">
              {group.items.map(([key, value]) => {
                const fieldGroup = (fieldGroups || []).find((item) => item.fields.includes(key));
                if (fieldGroup) {
                  if (renderedGroups.has(fieldGroup.title)) return null;
                  if (!matchesCondition(fieldGroup.visibleWhen, values)) return null;
                  renderedGroups.add(fieldGroup.title);
                  const items = fieldGroup.fields
                    .map((itemKey) => group.items.find(([innerKey]) => innerKey === itemKey))
                    .filter((item): item is [string, unknown] => Boolean(item))
                    .filter(([itemKey]) => isFieldVisible(itemKey));
                  if (!items.length) return null;
                  return (
                    <div className="auto-config-subgroup" key={fieldGroup.title}>
                      <div className="auto-config-subgroup-head">
                        {fieldGroup.icon && (
                          <span className="auto-config-subgroup-icon">{fieldGroup.icon}</span>
                        )}
                        <div className="auto-config-subgroup-head-main">
                          <h5>{fieldGroup.title}</h5>
                          {fieldGroup.description && (
                            <p className="auto-config-subgroup-desc">{fieldGroup.description}</p>
                          )}
                        </div>
                        {fieldGroup.tag && (
                          <span className="auto-config-subgroup-tag">{fieldGroup.tag}</span>
                        )}
                      </div>
                      <div className="auto-config-subgroup-body">
                        <div className="auto-config-grid">
                          {items.map((item) => renderField(item, isFieldDisabled(item[0])))}
                        </div>
                      </div>
                    </div>
                  );
                }
                if (!isFieldVisible(key)) return null;
                return renderField([key, value], isFieldDisabled(key));
              })}
            </div>
          </div>
        );
      })}
      <div className="auto-config-actions">
        <Button theme="solid" type="primary" loading={saving || loading} onClick={save}>
          保存
        </Button>
      </div>
    </Card>
  );
}
