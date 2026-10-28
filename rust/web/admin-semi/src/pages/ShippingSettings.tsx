import { useEffect, useState } from 'react';
import { Button, Card, Input, Radio, RadioGroup, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

interface ShippingSettings {
  defaultLogisticsName?: string;
  logisticsProvider?: number;
  kdniaoApiKey?: string;
  kdniaoBusinessId?: string;
  kuaidi100Key?: string;
  kuaidi100Secret?: string;
  kuaidi100Customer?: string;
  sender?: string;
  mobile?: string;
  provinceName?: string;
  cityName?: string;
  areaName?: string;
  address?: string;
  receiveSender?: string;
  receiveMobile?: string;
  receiveProvinceName?: string;
  receiveCityName?: string;
  receiveAreaName?: string;
  receiveAddress?: string;
}

const initial: ShippingSettings = {
  defaultLogisticsName: '商家配送',
  logisticsProvider: 1,
};

export default function ShippingSettingsPage() {
  const [form, setForm] = useState<ShippingSettings>(initial);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    apiGet<ShippingSettings>('/setting/config/shippingSettings')
      .then((result) => setForm({ ...initial, ...(result || {}) }))
      .catch((error) => Toast.error({ content: error instanceof Error ? error.message : '加载失败' }))
      .finally(() => setLoading(false));
  }, []);

  const update = (key: keyof ShippingSettings, value: string | number) => {
    setForm((current) => ({ ...current, [key]: value }));
  };

  const save = async () => {
    if (!form.defaultLogisticsName?.trim()) {
      Toast.warning({ content: '自定义名称不能为空' });
      return;
    }
    setSaving(true);
    try {
      await apiPost('/setting/config/saveShipping', form);
      Toast.success({ content: '保存成功' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  const isKdniao = Number(form.logisticsProvider) !== 2;

  const field = (label: string, key: keyof ShippingSettings, placeholder = '') => (
    <div className="config-field">
      <label>{label}</label>
      <Input
        value={(form[key] as string) || ''}
        placeholder={placeholder}
        onChange={(value) => update(key, value)}
      />
    </div>
  );

  return (
    <div className="config-page">
      <Card className="config-card" shadows="hover">
        <div className="config-section">
          <h3>快递发货设置</h3>
          <div className="config-field">
            <label>自定义名称</label>
            <div>
              <Input
                value={form.defaultLogisticsName || ''}
                maxLength={10}
                onChange={(value) => update('defaultLogisticsName', value)}
              />
              <Text type="tertiary" size="small">
                该名称会显示在买家可选择的配送方式中，默认名称“商家配送”
              </Text>
            </div>
          </div>
        </div>

        <div className="config-section">
          <h3>快递接口设置</h3>
          <div className="config-field">
            <label>快递接口</label>
            <RadioGroup
              value={form.logisticsProvider || 1}
              onChange={(event) => update('logisticsProvider', Number(event.target.value))}
            >
              <Radio value={1}>快递鸟</Radio>
              <Radio value={2}>快递100</Radio>
            </RadioGroup>
          </div>
          {isKdniao ? (
            <>
              {field('快递鸟 API Key', 'kdniaoApiKey')}
              {field('快递鸟服务 ID', 'kdniaoBusinessId')}
            </>
          ) : (
            <>
              {field('Key', 'kuaidi100Key')}
              {field('Secret', 'kuaidi100Secret')}
              {field('Customer', 'kuaidi100Customer')}
            </>
          )}
        </div>

        <div className="config-section">
          <h3>发货信息设置</h3>
          <div className="config-grid">
            {field('发货人', 'sender')}
            {field('发货人联系方式', 'mobile')}
            {field('发货地省份', 'provinceName')}
            {field('发货城市', 'cityName')}
            {field('发货地区', 'areaName')}
            {field('发货详细地址', 'address')}
          </div>
        </div>

        <div className="config-section">
          <h3>退货信息设置</h3>
          <div className="config-grid">
            {field('退货人', 'receiveSender')}
            {field('退货人联系方式', 'receiveMobile')}
            {field('退货省份', 'receiveProvinceName')}
            {field('退货城市', 'receiveCityName')}
            {field('退货地区', 'receiveAreaName')}
            {field('退货详细地址', 'receiveAddress')}
          </div>
        </div>

        <div className="config-actions">
          <Button theme="solid" type="primary" loading={saving || loading} onClick={save}>
            保存
          </Button>
        </div>
      </Card>
    </div>
  );
}
