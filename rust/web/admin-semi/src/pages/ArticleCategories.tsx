import { useEffect, useState } from 'react';
import { Button, Modal, Select, Space, Table, Toast, Typography } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import FormDrawer from '../framework/FormDrawer';
import type { FormDrawerConfig } from '../framework/types';

const { Text } = Typography;

interface CategoryRow {
  articleCategoryId: number;
  articleCategoryName?: string;
  articleCategorySn?: string;
  parentId?: number;
  keywords?: string;
  description?: string;
  sortOrder?: number;
  hasChildren?: boolean;
  children?: CategoryRow[];
}

/** 文章分类：对应旧版 content/articleCategory/List.vue + Info.vue（树形 + 行内改 + 移动分类）。
 * 旧版上级分类下拉的选项列头为"权限名称"（沿用权限树组件文案），新版直接展示分类名称。 */
export default function ArticleCategories() {
  const [rows, setRows] = useState<CategoryRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState<'create' | 'edit'>('create');
  const [drawerInitial, setDrawerInitial] = useState<Record<string, any>>({});
  const [editingId, setEditingId] = useState<number | null>(null);
  const [saving, setSaving] = useState(false);
  const [moveTarget, setMoveTarget] = useState<CategoryRow | null>(null);
  const [moveTo, setMoveTo] = useState<number | undefined>();
  const [moveSaving, setMoveSaving] = useState(false);

  const loadTree = () => {
    setLoading(true);
    apiGet<any>('/content/articleCategory/tree')
      .then((result) => {
        const list = Array.isArray(result) ? result : result?.records || result?.list || [];
        setRows(list);
      })
      .catch((error) => Toast.error({ content: error.message }))
      .finally(() => setLoading(false));
  };

  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => {
    loadTree();
  }, [reloadKey]);

  const reload = () => setReloadKey((key) => key + 1);

  const updateField = async (row: CategoryRow, field: string, value: unknown) => {
    try {
      await apiPost('/content/articleCategory/updateField', { id: row.articleCategoryId, field, value });
      Toast.success({ content: '修改成功' });
      loadTree();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '修改失败' });
    }
  };

  const openCreate = (parentId?: number) => {
    setDrawerMode('create');
    setEditingId(null);
    setDrawerInitial({ parentId: parentId ?? 0, articleCategoryName: '', keywords: '', description: '', sortOrder: 50 });
    setDrawerOpen(true);
  };

  const openEdit = async (row: CategoryRow) => {
    setDrawerMode('edit');
    setEditingId(row.articleCategoryId);
    try {
      const detail = await apiGet<any>('/content/articleCategory/detail', { id: row.articleCategoryId });
      setDrawerInitial({ ...row, ...(detail || {}) });
    } catch {
      setDrawerInitial({ ...row });
    }
    setDrawerOpen(true);
  };

  const saveCategory = async (payload: Record<string, any>) => {
    if (!String(payload.articleCategoryName || '').trim()) {
      Toast.error({ content: '文章分类名称不能为空!' });
      return;
    }
    setSaving(true);
    try {
      const body = {
        ...payload,
        parentId: Number(payload.parentId ?? 0),
        sortOrder: Number(payload.sortOrder ?? 50),
      };
      if (drawerMode === 'edit' && editingId) {
        await apiPost('/content/articleCategory/update', { ...body, articleCategoryId: editingId });
      } else {
        await apiPost('/content/articleCategory/create', body);
      }
      Toast.success({ content: '操作成功' });
      setDrawerOpen(false);
      loadTree();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

  const deleteCategory = (row: CategoryRow) => {
    Modal.confirm({
      title: '确认删除该分类吗？',
      onOk: async () => {
        try {
          await apiPost('/content/articleCategory/del', { id: row.articleCategoryId });
          Toast.success({ content: '删除成功' });
          loadTree();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
        }
      },
    });
  };

  const submitMove = async () => {
    if (!moveTarget || moveTo === undefined) return;
    setMoveSaving(true);
    try {
      await apiPost('/content/articleCategory/moveCat', {
        id: moveTarget.articleCategoryId,
        parentId: moveTo,
      });
      Toast.success({ content: '移动成功' });
      setMoveTarget(null);
      loadTree();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '移动失败' });
    } finally {
      setMoveSaving(false);
    }
  };

  const flatten = (list: CategoryRow[], depth = 0): Array<CategoryRow & { __depth: number }> =>
    list.flatMap((item) => [
      { ...item, __depth: depth },
      ...(item.children?.length ? flatten(item.children, depth + 1) : []),
    ]);

  const drawerConfig: FormDrawerConfig = {
    createApi: '/content/articleCategory/create',
    title: '文章分类',
    width: 560,
    labelWidth: 110,
    sections: [
      {
        key: 'basic',
        title: '分类信息',
        fields: [
          { field: 'articleCategoryName', label: '文章分类名称', type: 'input', required: true },
          { field: 'keywords', label: '关键字', type: 'input' },
          { field: 'description', label: '描述', type: 'textarea' },
          { field: 'sortOrder', label: '排序', type: 'number', initValue: 50 },
        ],
      },
    ],
    toPayload: (values) => {
      if (!String(values.articleCategoryName || '').trim()) throw new Error('文章分类名称不能为空!');
      return values;
    },
  };

  return (
    <div className="article-categories-page">
      <Table
        loading={loading}
        rowKey="articleCategoryId"
        dataSource={flatten(rows)}
        pagination={false}
        empty={loading ? null : <Text type="tertiary">暂无数据</Text>}
        columns={[
          {
            title: '文章分类名称',
            dataIndex: 'articleCategoryName',
            width: 280,
            render: (value: string, row: CategoryRow & { __depth: number }) => (
              <span style={{ paddingLeft: row.__depth * 24 }}>
                <span
                  title="文章分类名称（点击行内可改）"
                  style={{ cursor: 'pointer' }}
                  onClick={() => {
                    const next = window.prompt('修改文章分类名称', value || '');
                    if (next && next !== value) updateField(row, 'articleCategoryName', next);
                  }}
                >
                  {value || '-'}
                </span>
              </span>
            ),
          },
          {
            title: '分类编号',
            dataIndex: 'articleCategorySn',
            width: 160,
            render: (value: string, row: CategoryRow & { __depth: number }) => (
              <span
                title="分类编号（点击可改）"
                style={{ cursor: 'pointer' }}
                onClick={() => {
                  const next = window.prompt('修改分类编号', value || '');
                  if (next !== null && next !== value) updateField(row, 'articleCategorySn', next);
                }}
              >
                {value || '--'}
              </span>
            ),
          },
          {
            title: '排序',
            dataIndex: 'sortOrder',
            width: 100,
            render: (value: number, row: CategoryRow & { __depth: number }) => (
              <span
                title="排序（默认值为50，数值越小，排序越靠前；点击可改）"
                style={{ cursor: 'pointer' }}
                onClick={() => {
                  const next = window.prompt('修改排序', String(value ?? 50));
                  if (next !== null && next !== String(value ?? '')) updateField(row, 'sortOrder', Number(next));
                }}
              >
                {value ?? 50}
              </span>
            ),
          },
          {
            title: '操作',
            width: 260,
            render: (_: unknown, row: CategoryRow & { __depth: number }) => (
              <Space spacing={2}>
                <Button theme="borderless" size="small" onClick={() => openEdit(row)}>
                  编辑
                </Button>
                <Button theme="borderless" size="small" onClick={() => openCreate(row.articleCategoryId)}>
                  添加下级
                </Button>
                <Button theme="borderless" size="small" onClick={() => { setMoveTarget(row); setMoveTo(undefined); }}>
                  移动
                </Button>
                <Button theme="borderless" type="danger" size="small" onClick={() => deleteCategory(row)}>
                  删除
                </Button>
              </Space>
            ),
          },
        ]}
      />

      <Space style={{ marginTop: 14 }}>
        <Button theme="solid" type="primary" onClick={() => openCreate()}>
          添加文章分类
        </Button>
        <Button onClick={loadTree}>刷新</Button>
      </Space>

      <FormDrawer
        visible={drawerOpen}
        mode={drawerMode}
        initialValues={drawerInitial}
        config={drawerConfig}
        saving={saving}
        onCancel={() => setDrawerOpen(false)}
        onSubmit={saveCategory}
      />

      <Modal
        title={moveTarget ? `移动分类：${moveTarget.articleCategoryName || ''}` : '移动分类'}
        visible={Boolean(moveTarget)}
        confirmLoading={moveSaving}
        onOk={submitMove}
        onCancel={() => setMoveTarget(null)}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <Text type="tertiary">选择目标上级分类（不选表示移动为顶级）：</Text>
          <Select
            style={{ width: 320 }}
            placeholder="顶级分类"
            showClear
            optionList={flatten(rows)
              .filter((item) => item.articleCategoryId !== moveTarget?.articleCategoryId)
              .map((item) => ({
                label: `${'　'.repeat(item.__depth)}${item.articleCategoryName || ''}`,
                value: item.articleCategoryId,
              }))}
            value={moveTo}
            onChange={(next) => setMoveTo(next === undefined ? undefined : Number(next))}
          />
        </div>
      </Modal>
    </div>
  );
}
