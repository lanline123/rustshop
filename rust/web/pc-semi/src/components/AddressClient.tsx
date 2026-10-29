'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Modal, Tag, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface Address {
  addressId: number;
  consignee?: string;
  mobile?: string;
  telephone?: string;
  email?: string;
  regionName?: string;
  regionNames?: string[];
  address?: string;
  fullAddress?: string;
  isSelected?: number;
}

export default function AddressClient() {
  const [rows, setRows] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);

  const load = () => {
    setLoading(true);
    apiGet<{ records?: Address[] }>('user/address/list', { page: 1, size: 99 })
      .then((result) => setRows(result.records || []))
      .catch((error) => {
        const message = error instanceof Error ? error.message : '';
        setNeedLogin(message.includes('登录') || message.includes('401'));
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const setDefault = async (addressId: number) => {
    try {
      await apiPost('user/address/setSelected', { id: addressId });
      Toast.success({ content: '已设为默认地址' });
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '设置失败' });
    }
  };

  const remove = (addressId: number) => {
    Modal.confirm({
      title: '删除地址',
      content: '确认删除该收货地址？',
      onOk: async () => {
        try {
          await apiPost('user/address/del', { id: addressId });
          Toast.success({ content: '删除成功' });
          load();
        } catch (error) {
          Toast.error({ content: error instanceof Error ? error.message : '删除失败' });
        }
      },
    });
  };

  if (needLogin) {
    return (
      <Card className="cart-state">
        <h2>登录后管理收货地址</h2>
        <p>请先登录账号再添加或修改地址。</p>
        <Link href="/login?redirect=/member/address">
          <Button theme="solid" type="primary" size="large">去登录</Button>
        </Link>
      </Card>
    );
  }

  return (
    <div className="member-address">
      <div className="member-address__head">
        <span>共 {rows.length} 个收货地址</span>
        <Button theme="solid" type="primary" onClick={() => Toast.info({ content: '地址新增表单下一阶段接入' })}>新增地址</Button>
      </div>
      {loading ? (
        <Card className="member-card">地址加载中...</Card>
      ) : rows.length ? (
        <div className="member-address__list">
          {rows.map((address) => (
            <Card key={address.addressId} className="member-address__card">
              <div className="member-address__info">
                <div>
                  <b>{address.consignee}</b>
                  <span>{address.mobile}</span>
                  {address.isSelected ? <Tag color="green">默认</Tag> : null}
                </div>
                <p>{address.fullAddress || `${(address.regionNames || []).join(' ') || address.regionName || ''} ${address.address || ''}`}</p>
              </div>
              <div className="member-address__actions">
                <Button size="small" onClick={() => void setDefault(address.addressId)}>设为默认</Button>
                <Button size="small" theme="borderless" type="danger" onClick={() => remove(address.addressId)}>删除</Button>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="member-card">暂无收货地址</Card>
      )}
    </div>
  );
}
