import { useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Input,
  Popconfirm,
  Popover,
  Select,
  Space,
  Table,
  Toast,
} from '@douyinfe/semi-ui';
import { IconClose, IconPlus, IconRefresh, IconSearch } from '@douyinfe/semi-icons';
import { apiGet, apiPost, isDemo } from '../api';
import type { FilterConfig, ListPageProps } from './types';
import FormDrawer from './FormDrawer';
import TagFilter from './TagFilter';

function renderFilter(
  filter: FilterConfig,
  value: any,
  onChange: (value: any) => void,
) {
  const style = { width: filter.width || 180 };
  if (filter.type === 'tags') {
    return <TagFilter value={value} onChange={onChange} width={Number(filter.width) || 220} />;
  }
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
  showEdit = true,
  actionWidth = 160,
  selectable = false,
  batchActions,
  filterLayout = 'inline',
  onDataLoaded,
  editRequest,
  onEditRequestHandled,
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
  const [selectedKeys, setSelectedKeys] = useState<Array<string | number>>([]);
  const cardRef = useRef<HTMLDivElement | null>(null);
  const [containerWidth, setContainerWidth] = useState(0);
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
        onDataLoaded?.({ total: result.total || 0, rows: result.records || [] });
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

  useEffect(() => {
    if (!editRequest) return;
    void openEdit(editRequest);
    onEditRequestHandled?.();
  }, [editRequest]);

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

  useEffect(() => {
    const element = cardRef.current;
    if (!(element instanceof Element)) return;
    const observer = new ResizeObserver((entries) => {
      setContainerWidth(entries[0]?.contentRect.width || 0);
    });
    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  const hasActions = Boolean(drawer || rowActions);
  const selectedRows = rows.filter((row) => selectedKeys.includes(row[rowKey]));
  const activeEntries = Object.entries(filterValues).filter(
    ([, value]) => value !== '' && value !== undefined && value !== null,
  );
  const displayFilterValue = (field: string, value: any) => {
    const config = filterMap[field];
    const option = config?.options?.find((item) => item.value === value);
    return option ? `：${option.label}` : `：${value}`;
  };
  const scrollX =
    columns.reduce((sum, column) => sum + (column.width || 150), 0) + (hasActions ? actionWidth : 0);
  const tableColumns = [
    ...columns,
    ...(hasActions
      ? [
          {
            title: '操作',
            width: actionWidth,
            fixed: 'right',
            render: (_: unknown, record: T) => (
              <Space spacing={2}>
                {drawer && showEdit && (
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
      <div ref={cardRef}>
        <Card className="table-card" shadows="hover">
        <div className={`table-toolbar ${filterLayout === 'popover' ? 'is-popover' : ''}`}>
          <Space>
            {filters[0] && (
              <span className="framework-filter">
                {renderFilter(filterMap[filters[0].field], filterValues[filters[0].field], (value) =>
                  setFilterValues((current) => ({ ...current, [filters[0].field]: value })),
                )}
              </span>
            )}
            {filters[0] && (
              <Button theme="solid" type="primary" icon={<IconSearch />} onClick={search}>
                搜索
              </Button>
            )}
            {filterLayout === 'popover' && filters.length > 1 ? (
              <Popover
                trigger="click"
                position="bottomLeft"
                content={
                  <div className="framework-filter-popover">
                    {filters.slice(1).map((filter) => (
                      <div className="framework-filter-row" key={filter.field}>
                        <label>{filter.label}</label>
                        {renderFilter(filter, filterValues[filter.field], (value) =>
                          setFilterValues((current) => ({ ...current, [filter.field]: value })),
                        )}
                      </div>
                    ))}
                    <div className="framework-filter-actions">
                      <Button onClick={reset}>重置</Button>
                      <Button theme="solid" type="primary" onClick={search}>
                        应用筛选
                      </Button>
                    </div>
                  </div>
                }
              >
                <Button>
                  筛选
                  {activeEntries.length > 0 ? ` (${activeEntries.length})` : ''}
                </Button>
              </Popover>
            ) : (
              <>
                {filters.slice(1).map((filter) => (
                  <span key={filter.field} className="framework-filter">
                    {renderFilter(filter, filterValues[filter.field], (value) =>
                      setFilterValues((current) => ({ ...current, [filter.field]: value })),
                    )}
                  </span>
                ))}
                {filters.length > 0 && (
                  <Button onClick={reset}>重置</Button>
                )}
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

        {filterLayout === 'popover' && activeEntries.length > 0 && (
          <div className="framework-active-filters">
            <span className="framework-active-label">已选条件</span>
            {activeEntries.map(([field, value]) => (
              <span className="framework-filter-chip" key={field}>
                {filterMap[field]?.label || field}
                {displayFilterValue(field, value)}
                <IconClose
                  size="small"
                  onClick={() =>
                    setFilterValues((current) => {
                      const next = { ...current };
                      delete next[field];
                      return next;
                    })
                  }
                />
              </span>
            ))}
            <button type="button" onClick={reset}>
              清空
            </button>
          </div>
        )}

        {batchActions && selectedRows.length > 0 && (
          <div className="batch-floating-bar">
            <span>已选择 {selectedRows.length} 项</span>
            {batchActions(selectedRows, () => setSelectedKeys([]))}
            <Button theme="borderless" size="small" onClick={() => setSelectedKeys([])}>
              取消选择
            </Button>
          </div>
        )}

        <Table
          loading={loading}
          dataSource={rows}
          rowKey={rowKey}
          rowSelection={
            selectable
              ? {
                  selectedRowKeys: selectedKeys,
                  onChange: (keys) => setSelectedKeys((keys || []) as Array<string | number>),
                }
              : undefined
          }
          onRow={onRowClick ? (record) => ({ onClick: () => onRowClick(record as T) }) : undefined}
          scroll={containerWidth > 0 && scrollX > containerWidth - 48 ? { x: scrollX } : undefined}
          pagination={{
            currentPage: page,
            pageSize,
            total,
            onChange: (nextPage: number) => setPage(nextPage),
          }}
          columns={tableColumns as any}
        />
      </Card>
      </div>

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
