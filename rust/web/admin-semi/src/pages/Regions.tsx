import { useEffect, useState } from 'react';
import { Button, Card, Input, Modal, Space, Switch, Table, Toast, Typography } from '@douyinfe/semi-ui';
import { IconPlus, IconRefresh } from '@douyinfe/semi-icons';
import { apiGet, apiPost, isDemo } from '../api';
import FormDrawer from '../framework/FormDrawer';
import type { RegionRow } from '../types';

const { Text } = Typography;

function HotSwitch({
  row,
  value,
  onChange,
}: {
  row: RegionRow;
  value?: number;
  onChange: () => void;
}) {
  const [checked, setChecked] = useState(value === 1);
  const toggle = async (next: boolean) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持操作' });
      return;
    }
    try {
      await apiPost('/setting/region/updateField', {
        id: row.regionId,
        field: 'isHot',
        val: next ? 1 : 0,
      });
      setChecked(next);
      onChange();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '更新失败' });
    }
  };
  return <Switch checked={checked} onChange={toggle} size="small" />;
}

export default function Regions() {
  const [rows, setRows] = useState<RegionRow[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [parentId, setParentId] = useState(0);
  const [parentName, setParentName] = useState('顶级地区');
  const [loading, setLoading] = useState(false);
  const [reloadKey, setReloadKey] = useState(0);
  const [selectedKeys, setSelectedKeys] = useState<Array<string | number>>([]);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [drawerMode, setDrawerMode] = useState<'create' | 'edit'>('create');
  const [drawerValues, setDrawerValues] = useState<Record<string, any>>({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    apiGet<{ records: RegionRow[]; total: number }>('/setting/region/list', {
      parentId,
      page,
      size: 10,
    })
      .then((result) => {
        if (!alive) return;
        setRows(result.records || []);
        setTotal(result.total || 0);
      })
      .catch((error) => {
        if (alive) Toast.error({ content: error instanceof Error ? error.message : '地区加载失败' });
      })
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [parentId, page, reloadKey]);

  const refresh = () => setReloadKey((key) => key + 1);

  const openCreate = () => {
    setDrawerMode('create');
    setDrawerValues({
      parentId,
      parentName,
      isHot: 0,
      sortOrder: 50,
    });
    setDrawerVisible(true);
  };

  const openEdit = (record: RegionRow) => {
    setDrawerMode('edit');
    setDrawerValues({
      ...record,
      parentName: record.parentId ? parentName : '顶级地区',
    });
    setDrawerVisible(true);
  };

  const save = async (payload: Record<string, any>) => {
    setSaving(true);
    try {
      await apiPost(drawerMode === 'create' ? '/setting/region/create' : '/setting/region/update', {
        ...payload,
        regionId: drawerMode === 'create' ? undefined : drawerValues.regionId,
        parentId: Number(payload.parentId ?? parentId),
        isHot: payload.isHot ? 1 : 0,
        sortOrder: Number(payload.sortOrder ?? 50),
      });
      Toast.success({ content: drawerMode === 'create' ? '地区已创建' : '地区已更新' });
      setDrawerVisible(false);
      refresh();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存地区失败' });
      throw error;
    } finally {
      setSaving(false);
    }
  };

  const removeRegion = (record: RegionRow) => {
    Modal.confirm({
      title: '删除地区',
      content: `确定删除「${record.regionName}」？`,
      onOk: async () => {
        try {
          await apiPost('/setting/region/del', { id: record.regionId });
          Toast.success({ content: '删除成功' });
          refresh();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
        }
      },
    });
  };

  const batchDelete = () => {
    if (!selectedKeys.length) return;
    Modal.confirm({
      title: '批量删除地区',
      content: `确定删除选中的 ${selectedKeys.length} 个地区？`,
      onOk: async () => {
        try {
          await apiPost('/setting/region/batch', { ids: selectedKeys.map(Number), type: 'del' });
          Toast.success({ content: '批量删除成功' });
          setSelectedKeys([]);
          refresh();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '批量删除失败' });
        }
      },
    });
  };

  const drillDown = (record: RegionRow) => {
    setParentId(record.regionId);
    setParentName(record.regionName);
    setPage(1);
    setSelectedKeys([]);
  };

  const back = () => {
    setParentId(0);
    setParentName('顶级地区');
    setPage(1);
    setSelectedKeys([]);
  };

  const columns = [
    { title: '地区名称', dataIndex: 'regionName', width: 260 },
    {
      title: '首字母',
      dataIndex: 'firstWord',
      width: 100,
      render: (value: string) => value || '-',
    },
    {
      title: '是否热门',
      dataIndex: 'isHot',
      width: 100,
      render: (value: number, record: RegionRow) => (
        <HotSwitch row={record} value={value} onChange={refresh} />
      ),
    },
    {
      title: '操作',
      width: 300,
      fixed: 'right' as const,
      render: (_: unknown, record: RegionRow) => (
        <Space spacing={2}>
          <Button theme="borderless" size="small" onClick={() => openEdit(record)}>
            编辑
          </Button>
          <Button theme="borderless" size="small" onClick={() => drillDown(record)}>
            子地区
          </Button>
          <Button
            theme="borderless"
            size="small"
            onClick={() => {
              setDrawerMode('create');
              setDrawerValues({
                parentId: record.regionId,
                parentName: record.regionName,
                isHot: 0,
                sortOrder: 50,
              });
              setDrawerVisible(true);
            }}
          >
            添加子地区
          </Button>
          <Button theme="borderless" type="danger" size="small" onClick={() => removeRegion(record)}>
            删除
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <>
      <Card className="table-card" shadows="hover">
        <div className="table-toolbar">
          <Space>
            <Text type="tertiary">
              当前：{parentId ? parentName : '顶级地区'}，共 {total} 个
            </Text>
            {selectedKeys.length > 0 && <Text type="tertiary">已选 {selectedKeys.length} 项</Text>}
          </Space>
          <Space>
            {parentId > 0 && <Button onClick={back}>返回上一级</Button>}
            <Button icon={<IconRefresh />} onClick={refresh} loading={loading}>
              刷新
            </Button>
            <Button onClick={batchDelete} disabled={!selectedKeys.length}>
              批量删除
            </Button>
            <Button theme="solid" type="primary" icon={<IconPlus />} onClick={openCreate}>
              添加地区
            </Button>
          </Space>
        </div>
        <Table
          loading={loading}
          dataSource={rows}
          rowKey="regionId"
          scroll={{ x: 800 }}
          rowSelection={{
            selectedRowKeys: selectedKeys,
            onChange: (keys) => setSelectedKeys((keys || []) as Array<string | number>),
          }}
          pagination={{
            currentPage: page,
            pageSize: 10,
            total,
            onChange: setPage,
          }}
          columns={columns as any}
        />
      </Card>

      <FormDrawer
        visible={drawerVisible}
        mode={drawerMode}
        initialValues={drawerValues}
        saving={saving}
        onCancel={() => setDrawerVisible(false)}
        onSubmit={save}
        config={{
          title: '地区',
          addText: '添加地区',
          width: 580,
          labelWidth: 'auto',
          inputWidth: 400,
          createApi: '/setting/region/create',
          updateApi: '/setting/region/update',
          idField: 'regionId',
          sections: [
            {
              key: 'basic',
              title: '地区信息',
              fields: [
                {
                  field: 'regionName',
                  label: '地区名称',
                  required: true,
                  placeholder: '请输入地区名称',
                },
                {
                  field: 'parentName',
                  label: '上级地区',
                  disabled: true,
                  placeholder: '顶级地区',
                },
                {
                  field: 'isHot',
                  label: '是否热门',
                  type: 'switch',
                  initValue: 0,
                  checkedText: '热门',
                  uncheckedText: '不热门',
                },
                { field: 'sortOrder', label: '排序', type: 'number', initValue: 50 },
              ],
            },
          ],
          toPayload: (values) => ({
            regionName: values.regionName,
            parentId: Number(drawerValues.parentId ?? 0),
            isHot: values.isHot ? 1 : 0,
            sortOrder: Number(values.sortOrder ?? 50),
          }),
        }}
      />
    </>
  );
}
