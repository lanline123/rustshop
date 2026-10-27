import { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Modal,
  Popconfirm,
  Select,
  Space,
  Switch,
  Table,
  Tabs,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost, isDemo } from '../api';
import ListPage from '../framework/ListPage';
import FormDrawer from '../framework/FormDrawer';
import type { ColumnConfig } from '../framework/types';
import type { ArticleCategoryRow, ArticleRow } from '../types';
import ImageUploader from '../components/ImageUploader';
import RichTextEditor from '../components/RichTextEditor';

const { Text } = Typography;

function formatTime(value: number) {
  if (!value) return '-';
  return new Date(value * 1000).toLocaleString('zh-CN', { hour12: false });
}

function ArticleSwitch({
  record,
  field,
  value,
}: {
  record: ArticleRow;
  field: 'isShow' | 'isHot';
  value: number;
}) {
  const [checked, setChecked] = useState(value === 1);

  const toggle = async (next: boolean) => {
    if (isDemo()) {
      Toast.warning({ content: '演示模式不支持操作' });
      return;
    }
    try {
      if (field === 'isShow') {
        await apiPost('/content/article/updateField', {
          articleId: record.articleId,
          isShow: next ? 1 : 0,
        });
      } else {
        await apiPost('/content/article/update', {
          id: record.articleId,
          isHot: next ? 1 : 0,
        });
      }
      setChecked(next);
      Toast.success({ content: '已更新' });
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '更新失败' });
    }
  };

  return <Switch checked={checked} onChange={toggle} size="small" />;
}

export default function Content() {
  const [tab, setTab] = useState('articles');
  const [categories, setCategories] = useState<ArticleCategoryRow[]>([]);
  const [reloadKey, setReloadKey] = useState(0);
  const [catDrawerVisible, setCatDrawerVisible] = useState(false);
  const [catMode, setCatMode] = useState<'create' | 'edit'>('create');
  const [catInitial, setCatInitial] = useState<Record<string, any>>({});
  const [catSaving, setCatSaving] = useState(false);
  const [moveVisible, setMoveVisible] = useState(false);
  const [moveIds, setMoveIds] = useState<number[]>([]);
  const [moveCategoryId, setMoveCategoryId] = useState<number | undefined>(undefined);
  const [moveClear, setMoveClear] = useState<(() => void) | null>(null);

  const loadCategories = () => {
    if (isDemo()) return;
    apiGet<ArticleCategoryRow[]>('/content/articleCategory/list')
      .then((result) => setCategories(result || []))
      .catch(() => undefined);
  };

  useEffect(() => {
    loadCategories();
  }, []);

  const categoryName = (id: number) =>
    categories.find((item) => item.articleCategoryId === id)?.articleCategoryName || `分类 ${id}`;

  const columns = useMemo<ColumnConfig<ArticleRow>[]>(
    () => [
      {
        title: '文章标题',
        dataIndex: 'articleTitle',
        width: 300,
        render: (value: string) => <span className="article-title">{value}</span>,
      },
      {
        title: '文章分类',
        dataIndex: 'articleCategoryId',
        width: 140,
        render: (value: number) => <Tag color="blue">{categoryName(value)}</Tag>,
      },
      {
        title: '文章类型',
        dataIndex: 'articleTypeText',
        width: 110,
        render: (value: string) => value || '-',
      },
      {
        title: '文章重要性',
        dataIndex: 'isTop',
        width: 110,
        render: (value: number) => (value === 1 ? <Tag color="orange">置顶</Tag> : <span>普通</span>),
      },
      {
        title: '发布时间',
        dataIndex: 'addTime',
        width: 170,
        render: (value: number) => formatTime(value),
      },
      {
        title: '是否热门',
        dataIndex: 'isHot',
        width: 100,
        render: (value: number, record) => (
          <ArticleSwitch record={record} field="isHot" value={value} />
        ),
      },
      {
        title: '是否显示',
        dataIndex: 'isShow',
        width: 100,
        render: (value: number, record) => (
          <ArticleSwitch record={record} field="isShow" value={value} />
        ),
      },
    ],
    [categories],
  );

  const openCreateCategory = () => {
    setCatMode('create');
    setCatInitial({ parentId: 0, sortOrder: 50 });
    setCatDrawerVisible(true);
  };

  const openEditCategory = (record: ArticleCategoryRow) => {
    setCatMode('edit');
    setCatInitial({
      articleCategoryId: record.articleCategoryId,
      articleCategoryName: record.articleCategoryName,
      parentId: record.parentId || 0,
      sortOrder: record.sortOrder ?? 50,
    });
    setCatDrawerVisible(true);
  };

  const saveCategory = async (payload: Record<string, any>) => {
    setCatSaving(true);
    try {
      await apiPost(
        catMode === 'create' ? '/content/articleCategory/create' : '/content/articleCategory/update',
        {
          ...payload,
          articleCategoryId: catMode === 'create' ? undefined : catInitial.articleCategoryId,
          parentId: Number(payload.parentId || 0),
          sortOrder: Number(payload.sortOrder || 50),
        },
      );
      Toast.success({ content: catMode === 'create' ? '分类已创建' : '分类已更新' });
      setCatDrawerVisible(false);
      loadCategories();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存分类失败' });
    } finally {
      setCatSaving(false);
    }
  };

  const removeCategory = async (record: ArticleCategoryRow) => {
    try {
      await apiPost('/content/articleCategory/del', { id: record.articleCategoryId });
      Toast.success({ content: '分类已删除' });
      loadCategories();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '删除分类失败' });
    }
  };

  const batchArticle = async (
    action: 'del' | 'show' | 'hide' | 'moveCat',
    ids: number[],
    clear: () => void,
    targetCat?: number,
  ) => {
    try {
      await apiPost('/content/article/batch', {
        ids,
        type: action,
        targetCat: action === 'moveCat' ? [targetCat] : undefined,
      });
      Toast.success({ content: '批量操作成功' });
      clear();
      setReloadKey((key) => key + 1);
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '批量操作失败' });
    }
  };

  const confirmBatch = (
    title: string,
    action: 'del' | 'show' | 'hide',
    ids: number[],
    clear: () => void,
  ) => {
    Modal.confirm({
      title,
      content: `将对 ${ids.length} 篇文章执行操作`,
      onOk: () => batchArticle(action, ids, clear),
    });
  };

  return (
    <>
      <div className="content-tabs">
        <Tabs
          type="button"
          activeKey={tab}
          onChange={setTab}
          tabList={[
            { tab: '文章管理', itemKey: 'articles' },
            { tab: '文章分类', itemKey: 'categories' },
          ]}
        />
      </div>

      {tab === 'articles' && (
        <ListPage<ArticleRow>
          key={reloadKey}
          api="/content/article/list"
          rowKey="articleId"
          pageSize={10}
          selectable
          batchActions={(rows, clear) => {
            const ids = rows.map((row) => row.articleId);
            return (
              <Space>
                <Button size="small" onClick={() => confirmBatch('批量删除文章', 'del', ids, clear)}>
                  批量删除
                </Button>
                <Button size="small" onClick={() => confirmBatch('批量显示文章', 'show', ids, clear)}>
                  批量显示
                </Button>
                <Button size="small" onClick={() => confirmBatch('批量隐藏文章', 'hide', ids, clear)}>
                  批量隐藏
                </Button>
                <Button
                  size="small"
                  onClick={() => {
                    setMoveIds(ids);
                    setMoveCategoryId(undefined);
                    setMoveClear(() => clear);
                    setMoveVisible(true);
                  }}
                >
                  批量转移分类
                </Button>
              </Space>
            );
          }}
          filters={[
            {
              field: 'keyword',
              label: '文章名称',
              type: 'input',
              placeholder: '输入文章名称',
              width: 260,
            },
            {
              field: 'articleCategoryId',
              label: '全部分类',
              type: 'select',
              width: 160,
              options: categories.map((item) => ({
                label: item.articleCategoryName,
                value: item.articleCategoryId,
              })),
            },
            {
              field: 'isShow',
              label: '是否显示',
              type: 'select',
              width: 130,
              options: [
                { label: '显示', value: 1 },
                { label: '不显示', value: 0 },
              ],
            },
            {
              field: 'isHot',
              label: '是否热门',
              type: 'select',
              width: 130,
              options: [
                { label: '热门', value: 1 },
                { label: '不热门', value: 0 },
              ],
            },
          ]}
          columns={columns}
          drawer={{
            title: '文章',
            addText: '添加文章',
            width: 580,
            labelWidth: 'auto',
            inputWidth: 420,
            createApi: '/content/article/create',
            updateApi: '/content/article/update',
            detailApi: '/content/article/detail',
            deleteApi: '/content/article/del',
            idField: 'articleId',
            sections: [
              {
                key: 'basic',
                title: '基本信息',
                fields: [
                  {
                    field: 'articleTitle',
                    label: '文章标题',
                    required: true,
                    placeholder: '请输入文章标题',
                  },
                  {
                    field: 'articleSn',
                    label: '文章编号',
                    placeholder: '编号用于前台调用某些指定文章',
                  },
                  {
                    field: 'articleCategoryId',
                    label: '文章分类',
                    type: 'select',
                    required: true,
                    options: categories.map((item) => ({
                      label: item.articleCategoryName,
                      value: item.articleCategoryId,
                    })),
                  },
                  {
                    field: 'articleThumb',
                    label: '缩略图',
                    type: 'custom',
                    render: ({ formApi, values }) => (
                      <ImageUploader
                        value={values.articleThumb ? [values.articleThumb] : []}
                        onChange={(urls) => formApi?.setValue('articleThumb', urls[0] || '')}
                        max={1}
                      />
                    ),
                  },
                  { field: 'articleAuthor', label: '文章作者', placeholder: '请输入文章作者' },
                  { field: 'keywords', label: '关键字', placeholder: '请输入关键字' },
                  { field: 'articleTag', label: '标签', placeholder: '请输入标签' },
                  {
                    field: 'description',
                    label: '描述',
                    type: 'textarea',
                  },
                  { field: 'link', label: '相关链接', placeholder: 'https://...' },
                  {
                    field: 'articleType',
                    label: '文章类型',
                    type: 'radio',
                    options: [
                      { label: '普通文章', value: 1 },
                      { label: '帮助文章', value: 2 },
                    ],
                  },
                  {
                    field: 'isTop',
                    label: '是否置顶',
                    type: 'radio',
                    options: [
                      { label: '是', value: 1 },
                      { label: '否', value: 0 },
                    ],
                  },
                  {
                    field: 'isHot',
                    label: '是否热门',
                    type: 'radio',
                    options: [
                      { label: '是', value: 1 },
                      { label: '否', value: 0 },
                    ],
                  },
                  {
                    field: 'isShow',
                    label: '是否显示',
                    type: 'radio',
                    options: [
                      { label: '是', value: 1 },
                      { label: '否', value: 0 },
                    ],
                  },
                ],
              },
              {
                key: 'content',
                title: '文章内容',
                fields: [
                  {
                    field: 'content',
                    label: '正文',
                    type: 'custom',
                    render: ({ formApi, values }) => (
                      <RichTextEditor
                        value={values.content || ''}
                        onChange={(html) => formApi?.setValue('content', html)}
                        height={380}
                      />
                    ),
                  },
                ],
              },
            ],
            toPayload: (values) => ({
              ...values,
              articleCategoryId: Number(values.articleCategoryId),
            }),
          }}
        />
      )}

      {tab === 'categories' && (
        <>
          <Card className="table-card" shadows="hover">
            <div className="table-toolbar">
              <Text type="tertiary">共 {categories.length} 个文章分类</Text>
              <Button theme="solid" type="primary" onClick={openCreateCategory}>
                新增分类
              </Button>
            </div>
            <Table
              dataSource={categories}
              rowKey="articleCategoryId"
              pagination={false}
              columns={[
                { title: 'ID', dataIndex: 'articleCategoryId', width: 80 },
                { title: '分类名称', dataIndex: 'articleCategoryName' },
                {
                  title: '上级分类',
                  dataIndex: 'parentId',
                  width: 160,
                  render: (value: number) =>
                    value
                      ? categories.find((item) => item.articleCategoryId === value)
                          ?.articleCategoryName || `分类 ${value}`
                      : '顶级分类',
                },
                { title: '标识', dataIndex: 'categorySn', width: 130, render: (value: string) => value || '-' },
                { title: '排序', dataIndex: 'sortOrder', width: 90 },
                {
                  title: '操作',
                  width: 150,
                  render: (_: unknown, record: ArticleCategoryRow) => (
                    <Space>
                      <Button
                        theme="borderless"
                        size="small"
                        onClick={() => openEditCategory(record)}
                      >
                        编辑
                      </Button>
                      <Popconfirm
                        title="确定删除该分类？分类下有文章或子分类时不能删除。"
                        onConfirm={() => removeCategory(record)}
                      >
                        <Button theme="borderless" type="danger" size="small">
                          删除
                        </Button>
                      </Popconfirm>
                    </Space>
                  ),
                },
              ]}
            />
          </Card>

          <FormDrawer
            visible={catDrawerVisible}
            mode={catMode}
            initialValues={catInitial}
            saving={catSaving}
            onCancel={() => setCatDrawerVisible(false)}
            onSubmit={saveCategory}
            config={{
              title: '文章分类',
              addText: '新增分类',
              width: 560,
              labelWidth: 'auto',
              inputWidth: 420,
              createApi: '/content/articleCategory/create',
              updateApi: '/content/articleCategory/update',
              sections: [
                {
                  key: 'basic',
                  title: '分类信息',
                  fields: [
                    {
                      field: 'articleCategoryName',
                      label: '分类名称',
                      required: true,
                      placeholder: '请输入分类名称',
                    },
                    {
                      field: 'parentId',
                      label: '上级分类',
                      type: 'select',
                      options: [
                        { label: '顶级分类', value: 0 },
                        ...categories
                          .filter((item) => item.articleCategoryId !== catInitial.articleCategoryId)
                          .map((item) => ({
                            label: item.articleCategoryName,
                            value: item.articleCategoryId,
                          })),
                      ],
                    },
                    {
                      field: 'sortOrder',
                      label: '排序',
                      type: 'number',
                    },
                  ],
                },
              ],
              toPayload: (values) => ({
                articleCategoryName: values.articleCategoryName,
                parentId: values.parentId ?? 0,
                sortOrder: values.sortOrder ?? 50,
              }),
            }}
          />
        </>
      )}

      <Modal
        title="批量转移分类"
        visible={moveVisible}
        onCancel={() => setMoveVisible(false)}
        width={480}
        footer={
          <Space>
            <Button onClick={() => setMoveVisible(false)}>取消</Button>
            <Button
              theme="solid"
              type="primary"
              onClick={async () => {
                if (!moveCategoryId) {
                  Toast.warning({ content: '请选择目标分类' });
                  return;
                }
                await batchArticle('moveCat', moveIds, moveClear || (() => undefined), moveCategoryId);
                setMoveVisible(false);
              }}
            >
              确认转移
            </Button>
          </Space>
        }
      >
        <div style={{ marginBottom: 10 }}>已选择 {moveIds.length} 篇文章</div>
        <Select
          placeholder="请选择目标分类"
          value={moveCategoryId}
          onChange={(value) => setMoveCategoryId(Number(value))}
          optionList={categories.map((item) => ({
            label: item.articleCategoryName,
            value: item.articleCategoryId,
          }))}
          style={{ width: '100%' }}
        />
      </Modal>
    </>
  );
}
