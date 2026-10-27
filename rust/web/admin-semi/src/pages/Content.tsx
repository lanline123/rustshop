import { useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
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

  useEffect(() => {
    if (isDemo()) return;
    let alive = true;
    apiGet<ArticleCategoryRow[]>('/content/articleCategory/list')
      .then((result) => alive && setCategories(result || []))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
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
        <Card className="table-card" shadows="hover">
          <div className="table-toolbar">
            <Text type="tertiary">共 {categories.length} 个文章分类</Text>
            <Button theme="solid" type="primary">
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
              { title: '标识', dataIndex: 'categorySn', render: (value: string) => value || '-' },
              { title: '父级 ID', dataIndex: 'parentId', width: 100 },
            ]}
          />
        </Card>
      )}
    </>
  );
}
