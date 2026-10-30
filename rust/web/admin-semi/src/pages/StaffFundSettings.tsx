import { useEffect, useState } from 'react';
import { Button, Card, TextArea, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

const TITLES: Record<string, string> = {
  deliveryFundManagement: '配送设置',
  serverFundManagement: '服务设置',
};

interface StaffFundSettingsProps {
  apiPath: 'deliveryFundManagement' | 'serverFundManagement';
}

/** 配送员/服务员资金设置：后端为自由 schema 配置，旧版即原始 JSON 编辑器。 */
export default function StaffFundSettings({ apiPath }: StaffFundSettingsProps) {
  const title = TITLES[apiPath] || apiPath;
  const [json, setJson] = useState('{}');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = () => {
    setLoading(true);
    apiGet<Record<string, unknown>>(`/finance/${apiPath}/settings/detail`)
      .then((data) => setJson(JSON.stringify(data ?? {}, null, 2)))
      .catch((error) => Toast.error({ content: error instanceof Error ? error.message : '加载失败' }))
      .finally(() => setLoading(false));
  };

  useEffect(load, [apiPath]);

  const save = async () => {
    let data: Record<string, unknown>;
    try {
      data = JSON.parse(json || '{}');
    } catch {
      Toast.error({ content: 'JSON 格式错误' });
      return;
    }
    setSaving(true);
    try {
      await apiPost(`/finance/${apiPath}/settings/save`, data);
      Toast.success({ content: '保存成功' });
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card className="staff-fund-settings" title={title} shadows="hover" loading={loading}>
      <div className="staff-fund-settings-body">
        <label className="staff-fund-settings-label">配置 JSON</label>
        <TextArea
          value={json}
          rows={18}
          spellCheck={false}
          onChange={(next) => setJson(next)}
        />
        <p className="staff-fund-settings-tip">保存前请确认 JSON 格式合法。</p>
        <Button theme="solid" type="primary" loading={saving} onClick={save}>
          保存
        </Button>
      </div>
      <Text type="tertiary" size="small">
        接口：/finance/{apiPath}/settings/detail
      </Text>
    </Card>
  );
}
