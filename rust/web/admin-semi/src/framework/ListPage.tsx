import { useEffect, useMemo, useState, type ReactNode } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Input,
  Popconfirm,
  Select,
  Space,
  Table,
  Toast,
} from '@douyinfe/semi-ui';
import { IconPlus, IconRefresh, IconSearch } from '@douyinfe/semi-icons';
import { apiGet, apiPost, isDemo } from '../api';
import type { FilterConfig, ListPageProps } from './types';
import FormDrawer from './FormDrawer';

function renderFilter(
  filter: FilterConfig,
  value: any,
  onChange: (value: any) => void,
) {
  const style = { width: filter.width || 180 };
  if (filter.type === 'select') {
    return (
      <Select
        placeholder={filter.placeholder || filter.label}
        value={value}
        optionList={filter.options || []}
        onChange={onChange}
        style={style}
      />
    );
  }
  if (filter.type === 'date') {
    return <DatePicker placeholder={filter.placeholder || filter.label} value={value} onChange={onChange} style={style} />;
  }
  if (filter.type === 'number') {
    return (
      <Input
        placeholder={filter.placeholder || filter.label}
        value={value ?? ''}
        onChange={(next) => onChange(next)}
        style={style}
      />
    );
  }
  return (
    <Input
      prefix={<IconSearch />}
      placeholder={filter.placeholder || filter.label}
      value={value ?? ''}
      onChange={(next) => onChange(next)}
      onEnterPress={() => onChange(value)}
      style={style}
    />
  );
}

export default function ListPage<T extends Record<string, any>>({
  api,
  rowKey,
  columns,
  filters = [],
  query,
  pageSize = 10,
  drawer,
  toolbarExtra,
  cardTitle,
  onRowClick,
  rowActions,
}: ListPageProps<T>) {
  const [rows, setRows] = useState<T[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(!isDemo());
  const [reloadKey, setReloadKey] = useState(0);
  const [filterValues, setFilterValues] = useState<Record<string, any>>({});
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [drawerMode, setDrawerMode] = useState<'create' | 'edit'>('create');
  const [drawerInitial, setDrawerInitial] = useState<Record<string, any>>({});
  const [editingRecord, setEditingRecord] = useState<T | null>(null);
  const [saving, setSaving] = useState(false);

  const filterMap = useMemo(
    () => Object.fromEntries(filters.map((item) => [item.field, item])),
    [filters],
  );

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    setLoading(true);
    apiGet<{ records: T[]; total: number }>(api, {
      page,
      size: pageSize,
      ...query,
      ...filterValues,
    })
      .then((result) => {
        if (!alive) return;
        setRows(result.records || []);
        setTotal(result.total || 0);
      })
      .catch((error) => alive && Toast.error({ content: error.message }))
      .finally(() => alive && setLoading(false));
    return () => {
      alive = false;
    };
  }, [api, page, pageSize, query, filterValues, reloadKey]);

  const search = () => {
    setPage(1);
    setReloadKey((key) => key + 1);
  };

  const reset = () => {
    setFilterValues({});
    setPage(1);
    setReloadKey((key) => key + 1);
  };

  const openCreate = () => {
    if (!drawer) return;
    setDrawerMode('create');
    setEditingRecord(null);
    setDrawerInitial(drawer.sections.reduce<Record<string, any>>((acc, section) => {
      section.fields.forEach((field) => {
        if (field.initValue !== undefined) {
          acc[field.field] = field.initValue;
        } else if (field.type === 'switch') {
          acc[field.field] = false;
        }
      });
      return acc;
    }, {}));
    setDrawerVisible(true);
  };

  const openEdit = async (record: T) => {
    if (!drawer) return;
    setDrawerMode('edit');
    setEditingRecord(record);
    if (drawer.detailApi && drawer.idField) {
      try {
        const detail = await apiGet<Record<string, any>>(drawer.detailApi, {
          id: record[rowKey],
        });
        setDrawerInitial(detail || record);
      } catch {
        setDrawerInitial(record);
      }
    } else {
      setDrawerInitial(record);
    }
    setDrawerVisible(true);
  };

  const save = async (payload: Record<string, any>) => {
    if (!drawer) return;
    setSaving(true);
    try {
      const targetApi =
        drawerMode === 'create' ? drawer.createApi : drawer.updateApi || drawer.createApi;
      const body =
        drawerMode === 'edit' && drawer.idField
          ? { ...payload, [drawer.idField]: editingRecord?.[rowKey] }
          : payload;
      await apiPost(targetApi, body);
      Toast.success({ content: drawerMode === 'create' ? '创建成功' : '更新成功' });
      setDrawerVisible(false);
      setPage(1);
      setReloadKey((key) => key + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  const remove = async (record: T) => {
    if (!drawer?.deleteApi) return;
    try {
      await apiPost(drawer.deleteApi, { id: record[rowKey] });
      Toast.success({ content: '删除成功' });
      setReloadKey((key) => key + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
    }
  };

  const hasActions = Boolean(drawer || rowActions);
  const tableColumns = [
    ...columns,
    ...(hasActions
      ? [
          {
            title: '操作',
            width: 160,
            render: (_: unknown, record: T) => (
              <Space spacing={2}>
                {drawer && (
                  <Button theme="borderless" size="small" onClick={() => openEdit(record)}>
                    编辑
                  </Button>
                )}
                {drawer?.deleteApi && (
                  <Popconfirm title="确定删除？" onConfirm={() => remove(record)}>
                    <Button theme="borderless" type="danger" size="small">
                      删除
                    </Button>
                  </Popconfirm>
                )}
                {rowActions?.(record as T)}
              </Space>
            ),
          },
        ]
      : []),
  ];

  return (
    <>
      <Card className="table-card" shadows="hover">
        <div className="table-toolbar">
          <Space>
            {filters.map((filter) => (
              <span key={filter.field} className="framework-filter">
                {renderFilter(filterMap[filter.field], filterValues[filter.field], (value) =>
                  setFilterValues((current) => ({ ...current, [filter.field]: value })),
                )}
              </span>
            ))}
            {filters.length > 0 && (
              <>
                <Button theme="solid" type="primary" icon={<IconSearch />} onClick={search}>
                  查询
                </Button>
                <Button onClick={reset}>重置</Button>
              </>
            )}
          </Space>
          <Space>
            {toolbarExtra}
            <Button icon={<IconRefresh />} onClick={() => setReloadKey((key) => key + 1)}>
              刷新
            </Button>
            {drawer && (
              <Button theme="solid" type="primary" icon={<IconPlus />} onClick={openCreate}>
                {drawer.addText || '新增'}
              </Button>
            )}
          </Space>
        </div>

        <Table
          loading={loading}
          dataSource={rows}
          rowKey={rowKey}
          onRow={onRowClick ? (record) => ({ onClick: () => onRowClick(record as T) }) : undefined}
          pagination={{
            currentPage: page,
            pageSize,
            total,
            onChange: (nextPage: number) => setPage(nextPage),
          }}
          columns={tableColumns as any}
        />
      </Card>

      {drawer && (
        <FormDrawer
          visible={drawerVisible}
          mode={drawerMode}
          initialValues={drawerInitial}
          config={drawer}
          saving={saving}
          onCancel={() => setDrawerVisible(false)}
          onSubmit={save}
        />
      )}
    </>
  );
}
