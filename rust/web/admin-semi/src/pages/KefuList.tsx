import { useEffect, useState } from 'react';
import { Avatar, Button, Input, Radio, RadioGroup, Switch, Table, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';

const { Text } = Typography;

interface KefuRow {
  id?: number;
  adminId?: number;
  username?: string;
  avatar?: string;
  mobile?: string;
  roleName?: string;
  isUsing?: number;
  addTime?: string;
  adminUser?: { username?: string; avatar?: string; mobile?: string };
  role?: { roleName?: string };
}

/** 客服列表：对应旧版 im/kefuList/List.vue，接口 /im/adminUserKefu/*。 */
export default function KefuList() {
  const [rows, setRows] = useState<KefuRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const [keyword, setKeyword] = useState('');

  const reload = () => setReloadKey((key) => key + 1);

  const loadList = (kw = '') => {
    setLoading(true);
    apiGet<any>('/im/adminUserKefu/list', { page: 1, size: 50, keyword: kw })
      .then((result) => setRows(result?.records || result?.list || []))
      .catch((error) => Toast.error({ content: error.message }))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadList();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reloadKey]);

  const toggleUsing = async (row: KefuRow, next: number) => {
    try {
      await apiPost('/im/adminUserKefu/updateField', { id: row.id, field: 'isUsing', value: next });
      Toast.success({ content: '修改成功' });
      reload();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '修改失败' });
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'adminId',
      width: 100,
      render: (value: number) => <span>{value || '--'}</span>,
    },
    {
      title: '头像/姓名',
      dataIndex: 'username',
      width: 200,
      render: (_: unknown, row: KefuRow) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <Avatar size="small" src={row.avatar || row.adminUser?.avatar || undefined}>
            {(row.username || row.adminUser?.username || '?').slice(0, 1)}
          </Avatar>
          <span>{row.username || row.adminUser?.username || '--'}</span>
        </div>
      ),
    },
    {
      title: '员工身份',
      dataIndex: 'roleName',
      width: 140,
      render: (_: unknown, row: KefuRow) => <span>{row.role?.roleName || '客服人员'}</span>,
    },
    {
      title: '手机号',
      dataIndex: 'mobile',
      width: 140,
      render: (_: unknown, row: KefuRow) => <span>{row.adminUser?.mobile || row.mobile || '--'}</span>,
    },
    {
      title: '添加时间',
      dataIndex: 'addTime',
      width: 170,
      render: (value: string) => <span>{value || '--'}</span>,
    },
    {
      title: '账号状态',
      dataIndex: 'isUsing',
      width: 110,
      render: (value: number, row: KefuRow) => (
<span title={Number(value) === 1 ? '启用' : '停用'}><Switch checked={Number(value) === 1} onChange={(next) => toggleUsing(row, next ? 1 : 0)} /></span>
      ),
    },
  ];

  return (
    <div className="kefu-list-page">
      <Table
        loading={loading}
        rowKey="id"
        dataSource={rows}
        columns={columns as never}
        pagination={false}
        empty={loading ? null : <Text type="tertiary">暂无数据</Text>}
      />
      <div style={{ marginTop: 14, display: 'flex', gap: 10, alignItems: 'center' }}>
        <Input
          style={{ width: 240 }}
          placeholder="姓名/手机号"
          value={keyword}
          onChange={setKeyword}
          onEnterPress={() => loadList(keyword)}
        />
        <Button theme="solid" type="primary" onClick={() => loadList(keyword)}>
          搜索
        </Button>
        <Button onClick={() => { setKeyword(''); loadList(); }}>重置</Button>
        <Text type="tertiary" size="small">新增、编辑或删除人员，请前往员工管理。</Text>
      </div>
    </div>
  );
}
