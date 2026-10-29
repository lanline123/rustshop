'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Button, Card, Input, Modal, Select, Tag, Toast } from '@douyinfe/semi-ui';
import { apiGet, apiPost } from '@/lib/client-api';

interface Address {
  addressId: number;
  consignee?: string;
  mobile?: string;
  telephone?: string;
  email?: string;
  regionName?: string;
  regionNames?: string[];
  regionIds?: number[];
  address?: string;
  fullAddress?: string;
  isSelected?: number;
}

interface Region {
  regionId: number;
  regionName: string;
  parentId?: number;
  level?: number;
}

const EMPTY_FORM = {
  consignee: '',
  mobile: '',
  telephone: '',
  email: '',
  address: '',
};

export default function AddressClient() {
  const [rows, setRows] = useState<Address[]>([]);
  const [loading, setLoading] = useState(true);
  const [needLogin, setNeedLogin] = useState(false);
  const [open, setOpen] = useState(false);
  const [editingId, setEditingId] = useState(0);
  const [form, setForm] = useState(EMPTY_FORM);
  const [provinces, setProvinces] = useState<Region[]>([]);
  const [cities, setCities] = useState<Region[]>([]);
  const [districts, setDistricts] = useState<Region[]>([]);
  const [regionIds, setRegionIds] = useState<number[]>([]);
  const [saving, setSaving] = useState(false);

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

  const loadProvinces = () => {
    apiGet<Region[]>('sys/region/getRegionListByRegionId', { regionId: 1 })
      .then((result) => setProvinces(result || []))
      .catch(() => undefined);
  };

  useEffect(() => {
    load();
    loadProvinces();
  }, []);

  const openCreate = () => {
    setEditingId(0);
    setForm(EMPTY_FORM);
    setRegionIds([]);
    setCities([]);
    setDistricts([]);
    setOpen(true);
  };

  const openEdit = async (address: Address) => {
    setEditingId(address.addressId);
    setForm({
      consignee: address.consignee || '',
      mobile: address.mobile || '',
      telephone: address.telephone || '',
      email: address.email || '',
      address: address.address || '',
    });
    const ids = address.regionIds || [];
    setRegionIds(ids);
    if (ids[0]) {
      const cityList = await apiGet<Region[]>('sys/region/getRegionListByRegionId', { regionId: ids[0] }).catch(() => []);
      setCities(cityList || []);
      if (ids[1]) {
        const districtList = await apiGet<Region[]>('sys/region/getRegionListByRegionId', { regionId: ids[1] }).catch(() => []);
        setDistricts(districtList || []);
      }
    }
    setOpen(true);
  };

  const onProvince = async (value: number) => {
    setRegionIds([value]);
    setCities([]);
    setDistricts([]);
    const list = await apiGet<Region[]>('sys/region/getRegionListByRegionId', { regionId: value }).catch(() => []);
    setCities(list || []);
  };

  const onCity = async (value: number) => {
    setRegionIds((current) => [current[0], value]);
    setDistricts([]);
    const list = await apiGet<Region[]>('sys/region/getRegionListByRegionId', { regionId: value }).catch(() => []);
    setDistricts(list || []);
  };

  const onDistrict = (value: number) => {
    setRegionIds((current) => [current[0], current[1], value]);
  };

  const save = async () => {
    if (!form.consignee.trim() || !form.mobile.trim() || !regionIds.length || !form.address.trim()) {
      Toast.warning({ content: '请填写收货人、手机号、地区和详细地址' });
      return;
    }
    setSaving(true);
    try {
      const body = {
        ...form,
        regionIds,
        addressId: editingId || undefined,
      };
      await apiPost(editingId ? 'user/address/update' : 'user/address/add', body);
      Toast.success({ content: '保存成功' });
      setOpen(false);
      load();
    } catch (error) {
      Toast.error({ content: error instanceof Error ? error.message : '保存失败' });
    } finally {
      setSaving(false);
    }
  };

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
        <Button theme="solid" type="primary" onClick={openCreate}>新增地址</Button>
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
                <Button size="small" onClick={() => void openEdit(address)}>编辑</Button>
                <Button size="small" onClick={() => void setDefault(address.addressId)}>设为默认</Button>
                <Button size="small" theme="borderless" type="danger" onClick={() => remove(address.addressId)}>删除</Button>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <Card className="member-card">暂无收货地址</Card>
      )}

      <Modal
        title={editingId ? '编辑收货地址' : '新增收货地址'}
        visible={open}
        onCancel={() => setOpen(false)}
        onOk={() => void save()}
        confirmLoading={saving}
        width={560}
      >
        <div className="member-address__form">
          <label>收货人</label>
          <Input value={form.consignee} onChange={(value) => setForm((current) => ({ ...current, consignee: value }))} placeholder="请输入收货人姓名" />
          <label>手机号</label>
          <Input value={form.mobile} onChange={(value) => setForm((current) => ({ ...current, mobile: value }))} placeholder="请输入手机号" />
          <label>所在地区</label>
          <div className="member-address__region">
            <Select value={regionIds[0]} placeholder="省份" onChange={(value) => void onProvince(Number(value))} showClear={false}>
              {provinces.map((item) => <Select.Option key={item.regionId} value={item.regionId}>{item.regionName}</Select.Option>)}
            </Select>
            <Select value={regionIds[1]} placeholder="城市" onChange={(value) => void onCity(Number(value))} showClear={false} disabled={!cities.length}>
              {cities.map((item) => <Select.Option key={item.regionId} value={item.regionId}>{item.regionName}</Select.Option>)}
            </Select>
            <Select value={regionIds[2]} placeholder="区县" onChange={(value) => onDistrict(Number(value))} showClear={false} disabled={!districts.length}>
              {districts.map((item) => <Select.Option key={item.regionId} value={item.regionId}>{item.regionName}</Select.Option>)}
            </Select>
          </div>
          <label>详细地址</label>
          <Input value={form.address} onChange={(value) => setForm((current) => ({ ...current, address: value }))} placeholder="街道、门牌号" />
          <label>邮箱（选填）</label>
          <Input value={form.email} onChange={(value) => setForm((current) => ({ ...current, email: value }))} placeholder="用于接收订单通知" />
        </div>
      </Modal>
    </div>
  );
}
