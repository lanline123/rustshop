import { useEffect, useState } from 'react';
import {
  Avatar,
  Button,
  CheckboxGroup,
  Descriptions,
  Select,
  SideSheet,
  Space,
  Table,
  Tag,
  Toast,
  Typography,
} from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '../api';
import type { MemberRow } from '../types';

const { Text } = Typography;

interface DrawerProps {
  visible: boolean;
  member: MemberRow | null;
  onCancel: () => void;
}

export function MemberDetailDrawer({ visible, member, onCancel }: DrawerProps) {
  const [detail, setDetail] = useState<Record<string, any> | null>(null);
  const [tags, setTags] = useState<Array<Record<string, any>>>([]);

  useEffect(() => {
    if (!visible || !member) return;
    let alive = true;
    apiGet<Record<string, any>>('/user/user/detail', { id: member.userId })
      .then((result) => alive && setDetail(result))
      .catch(() => alive && setDetail(null));
    apiGet<Array<Record<string, any>>>('/user/userMemberTag/listByUser', { userId: member.userId })
      .then((result) => alive && setTags(result || []))
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible, member]);

  const data: Record<string, any> = detail || (member as Record<string, any>) || {};
  const address = data.userAddress || {};

  return (
    <SideSheet
      title="客户详情"
      visible={visible}
      onCancel={onCancel}
      width={800}
      placement="right"
      bodyStyle={{ padding: '16px 24px', overflowY: 'auto' }}
      footer={<Button onClick={onCancel}>关闭</Button>}
    >
      <div className="member-detail-head">
        <Avatar src={data.avatar} size="large" color="blue">
          {(data.username || 'U').slice(0, 1)}
        </Avatar>
        <div>
          <div className="member-detail-name">{data.nickname || data.username || '-'}</div>
          <Text type="tertiary">{data.rankName || '普通会员'}</Text>
        </div>
        <div className="member-detail-tags">
          {tags.length ? (
            tags.map((tag) => (
              <Tag key={tag.tagId} color="blue">
                {tag.tagName}
              </Tag>
            ))
          ) : (
            <Text type="tertiary" size="small">
              暂无标签
            </Text>
          )}
        </div>
      </div>

      <div className="detail-section-title">基本信息</div>
      <Descriptions
        column={3}
        layout="horizontal"
        align="plain"
        data={[
          { key: '客户名称', value: data.username || '-' },
          { key: '昵称', value: data.nickname || '-' },
          { key: '手机号', value: data.mobile || '-' },
          { key: '邮箱', value: data.email || '-' },
          { key: '客户等级', value: data.rankName || '-' },
          { key: '注册时间', value: data.regTime || '-' },
          { key: '生日', value: data.birthday || '-' },
          { key: '是否认证', value: data.isCompanyAuth ? '已认证' : '未认证' },
          { key: '状态', value: data.status === 1 ? '启用' : '禁用' },
        ]}
      />

      <div className="detail-section-title">资金信息</div>
      <Descriptions
        column={3}
        layout="horizontal"
        align="plain"
        data={[
          { key: '可用资金', value: `¥ ${data.balance ?? '0.00'}` },
          { key: '冻结资金', value: `¥ ${data.frozenBalance ?? '0.00'}` },
          { key: '消费积分', value: data.points ?? 0 },
          { key: '成长积分', value: data.growthPoints ?? 0 },
          { key: '累计消费', value: `¥ ${data.orderAmount ?? '0.00'}` },
          { key: '订单数', value: data.orderCount ?? 0 },
        ]}
      />

      <div className="detail-section-title">收货信息</div>
      <Descriptions
        column={2}
        layout="horizontal"
        align="plain"
        data={[
          { key: '收货人', value: address.consignee || '-' },
          { key: '联系电话', value: address.mobile || '-' },
          { key: '所在地区', value: (address.regionNames || []).join(' ') || '-' },
          { key: '详细地址', value: address.address || '-' },
        ]}
      />
    </SideSheet>
  );
}

export function MemberAccountDrawer({ visible, member, onCancel }: DrawerProps) {
  const [rows, setRows] = useState<Array<Record<string, any>>>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [fromTag, setFromTag] = useState('1');

  useEffect(() => {
    if (!visible || !member) return;
    let alive = true;
    apiGet<{ records: Array<Record<string, any>>; total: number }>('/user/user/userFundDetail', {
      userId: member.userId,
      fromTag,
      page,
      size: 10,
    })
      .then((result) => {
        if (!alive) return;
        setRows(result.records || []);
        setTotal(result.total || 0);
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible, member, page, fromTag]);

  return (
    <SideSheet
      title={`${member?.username || ''} 的账目明细`}
      visible={visible}
      onCancel={onCancel}
      width={800}
      placement="right"
      bodyStyle={{ padding: '16px 24px', overflowY: 'auto' }}
    >
      <Space style={{ marginBottom: 12 }}>
        <Select
          value={fromTag}
          onChange={(value) => {
            setFromTag(String(value));
            setPage(1);
          }}
          optionList={[
            { label: '可用资金账户', value: '1' },
            { label: '冻结资金账户', value: '2' },
            { label: '成长积分账户', value: '3' },
            { label: '消费积分账户', value: '4' },
          ]}
          style={{ width: 160 }}
        />
      </Space>
      <Table
        dataSource={rows}
        rowKey={(record: any) => String(record.logId ?? record.id ?? record.changeTime)}
        pagination={{ currentPage: page, pageSize: 10, total, onChange: setPage }}
        columns={[
          { title: '变动时间', dataIndex: 'changeTime', width: 170 },
          { title: '变动原因', dataIndex: 'changeDesc' },
          { title: '可用资金', dataIndex: 'balance', width: 110 },
          { title: '冻结资金', dataIndex: 'frozenBalance', width: 110 },
          { title: '积分', dataIndex: 'points', width: 90 },
        ]}
      />
    </SideSheet>
  );
}

export function MemberOrdersDrawer({ visible, member, onCancel }: DrawerProps) {
  const [rows, setRows] = useState<Array<Record<string, any>>>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);

  useEffect(() => {
    if (!visible || !member) return;
    let alive = true;
    apiGet<{ records: Array<Record<string, any>>; total: number }>('/order/order/list', {
      userId: member.userId,
      page,
      size: 10,
    })
      .then((result) => {
        if (!alive) return;
        setRows(result.records || []);
        setTotal(result.total || 0);
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible, member, page]);

  return (
    <SideSheet
      title={`${member?.username || ''} 的订单`}
      visible={visible}
      onCancel={onCancel}
      width={1200}
      placement="right"
      bodyStyle={{ padding: '16px 24px', overflowY: 'auto' }}
    >
      <Table
        dataSource={rows}
        rowKey={(record: any) => String(record.orderId)}
        pagination={{ currentPage: page, pageSize: 10, total, onChange: setPage }}
        columns={[
          { title: '订单号', dataIndex: 'orderSn' },
          { title: '收货人', dataIndex: 'consignee' },
          { title: '订单金额', dataIndex: 'totalAmount', width: 130 },
          { title: '支付状态', dataIndex: 'payStatusName', width: 110 },
          { title: '订单状态', dataIndex: 'orderStatusName', width: 120 },
          { title: '下单时间', dataIndex: 'addTime', width: 170 },
        ]}
      />
    </SideSheet>
  );
}

export function MemberTagsDrawer({
  visible,
  member,
  onCancel,
  onSaved,
}: DrawerProps & { onSaved: () => void }) {
  const [allTags, setAllTags] = useState<Array<Record<string, any>>>([]);
  const [selected, setSelected] = useState<number[]>([]);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!visible || !member) return;
    let alive = true;
    Promise.all([
      apiGet<Array<Record<string, any>>>('/user/userMemberTag/listByUser', { userId: member.userId }),
      apiGet<any>('/user/memberTagGroup/list', { page: 1, size: 100 }),
    ])
      .then(async ([userTags, groupsResult]) => {
        if (!alive) return;
        const groups = Array.isArray(groupsResult)
          ? groupsResult
          : groupsResult?.records || groupsResult?.list || [];
        const tagGroups = await Promise.all(
          groups.map((group: Record<string, any>) =>
            apiGet<Array<Record<string, any>>>('/user/memberTag/listByGroup', {
              groupId: group.groupId,
            })
              .then((tags) => tags || [])
              .catch(() => []),
          ),
        );
        if (!alive) return;
        setAllTags(tagGroups.flat());
        setSelected((userTags || []).map((tag) => Number(tag.tagId)));
      })
      .catch(() => undefined);
    return () => {
      alive = false;
    };
  }, [visible, member]);

  const save = async () => {
    if (!member) return;
    setSaving(true);
    try {
      const current = selected;
      const before = new Set<number>();
      const userTags = await apiGet<Array<Record<string, any>>>('/user/userMemberTag/listByUser', {
        userId: member.userId,
      });
      (userTags || []).forEach((tag) => before.add(Number(tag.tagId)));
      const added = current.filter((id) => !before.has(id));
      const removed = [...before].filter((id) => !current.includes(id));
      if (added.length) {
        await apiPost('/user/userMemberTag/bindTags', { userId: member.userId, tagIds: added });
      }
      if (removed.length) {
        await apiPost('/user/userMemberTag/unbindTags', { userId: member.userId, tagIds: removed });
      }
      Toast.success({ content: '标签已更新' });
      onSaved();
      onCancel();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存标签失败' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <SideSheet
      title={`${member?.username || ''} 的标签`}
      visible={visible}
      onCancel={onCancel}
      width={560}
      placement="right"
      bodyStyle={{ padding: '16px 24px', overflowY: 'auto' }}
      footer={
        <Space>
          <Button onClick={onCancel}>取消</Button>
          <Button theme="solid" type="primary" loading={saving} onClick={save}>
            保存
          </Button>
        </Space>
      }
    >
      {allTags.length ? (
        <CheckboxGroup
          value={selected}
          onChange={(value) => setSelected((value as number[]).map(Number))}
          options={allTags.map((tag) => ({
            label: tag.tagName,
            value: Number(tag.tagId),
          }))}
          direction="vertical"
        />
      ) : (
        <Typography.Text type="tertiary">
          暂无可用标签，请先在「客户标签」中创建标签组和标签。
        </Typography.Text>
      )}
    </SideSheet>
  );
}
