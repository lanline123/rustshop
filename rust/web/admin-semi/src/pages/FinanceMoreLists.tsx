import ListPage from '../framework/ListPage';
import type { ColumnConfig } from '../framework/types';

interface Row extends Record<string, any> {
  id?: number;
}

function Simple({
  api,
  rowKey = 'id',
  label,
  columns,
}: {
  api: string;
  rowKey?: string;
  label: string;
  columns: ColumnConfig<Row>[];
}) {
  return (
    <ListPage<Row>
      api={api}
      rowKey={rowKey}
      pageSize={10}
      filters={[
        { field: 'keyword', label, type: 'input', placeholder: `输入${label}`, width: 240 },
      ]}
      columns={columns}
    />
  );
}

export function DepositRules() {
  return (
    <Simple
      api="/deposit/rule/list"
      rowKey="shopDepositRuleId"
      label="规则名称"
      columns={[
        { title: '规则名称', dataIndex: 'ruleName' },
        { title: '保证金金额', dataIndex: 'depositAmount', width: 130 },
        { title: '规则状态', dataIndex: 'statusText', width: 120 },
        { title: '创建时间', dataIndex: 'createTime', width: 180 },
      ]}
    />
  );
}

export function DepositAccounts() {
  return (
    <Simple
      api="/deposit/payeeAccount/list"
      label="账户名称"
      columns={[
        { title: '账户名称', dataIndex: 'accountName' },
        { title: '账号', dataIndex: 'accountNo' },
        { title: '账户类型', dataIndex: 'accountTypeText', width: 130 },
        { title: '创建时间', dataIndex: 'createTime', width: 180 },
      ]}
    />
  );
}

export function DepositQuery() {
  return (
    <Simple
      api="/deposit/account/list"
      rowKey="shopId"
      label="组织名称"
      columns={[
        { title: '组织名称', dataIndex: 'shopName' },
        { title: '应缴金额', dataIndex: 'shouldAmount', width: 130 },
        { title: '已缴金额', dataIndex: 'paidAmount', width: 130 },
        { title: '剩余待缴', dataIndex: 'remainingAmount', width: 130 },
        { title: '保证金状态', dataIndex: 'statusText', width: 130 },
      ]}
    />
  );
}

export function DepositAudit() {
  return (
    <Simple
      api="/deposit/recharge/list"
      rowKey="shopDepositRechargeId"
      label="组织名称"
      columns={[
        { title: '组织名称', dataIndex: 'shopName' },
        { title: '主体类型', dataIndex: 'subjectTypeText', width: 120 },
        { title: '交易金额', dataIndex: 'amount', width: 130 },
        { title: '审核状态', dataIndex: 'statusText', width: 120 },
        { title: '提交时间', dataIndex: 'addTimeText', width: 170 },
      ]}
    />
  );
}

export function DepositLogs() {
  return (
    <Simple
      api="/deposit/log/list"
      rowKey="shopDepositLogId"
      label="组织名称"
      columns={[
        { title: '组织名称', dataIndex: 'shopName' },
        { title: '交易类型', dataIndex: 'typeText', width: 130 },
        { title: '交易金额', dataIndex: 'amount', width: 130 },
        { title: '交易时间', dataIndex: 'addTimeText', width: 170 },
      ]}
    />
  );
}

export function CategoryFeeRules() {
  return (
    <Simple
      api="/finance/categoryFeeRule/list"
      label="费率名称"
      columns={[
        { title: '费率名称', dataIndex: 'ruleName' },
        { title: '适用类目', dataIndex: 'categoryCount', width: 130 },
        { title: '服务费率', dataIndex: 'serviceFeeRate', width: 130 },
        { title: '规则状态', dataIndex: 'statusText', width: 120 },
      ]}
    />
  );
}

export function MerchantApplications() {
  return (
    <Simple
      api="/finance/merchantApplication/list"
      label="商户名称"
      columns={[
        { title: '商户名称', dataIndex: 'merchantName' },
        { title: '主体类型', dataIndex: 'subjectTypeText', width: 120 },
        { title: '状态', dataIndex: 'statusText', width: 120 },
        { title: '申请时间', dataIndex: 'createTime', width: 170 },
      ]}
    />
  );
}

export function SubMerchants() {
  return (
    <Simple
      api="/finance/subMerchant/list"
      label="商户名称"
      columns={[
        { title: '商户名称', dataIndex: 'merchantName' },
        { title: '子商户号', dataIndex: 'subMerchantNo' },
        { title: '支付渠道', dataIndex: 'channelText', width: 120 },
        { title: '状态', dataIndex: 'statusText', width: 100 },
        { title: '创建时间', dataIndex: 'createTime', width: 170 },
      ]}
    />
  );
}

export function ProfitSharing() {
  return (
    <Simple
      api="/finance/profitSharing/list"
      label="分账单号"
      columns={[
        { title: '分账单号', dataIndex: 'outOrderNo' },
        { title: '订单号', dataIndex: 'orderSn' },
        { title: '分账金额', dataIndex: 'amount', width: 120 },
        { title: '分账状态', dataIndex: 'statusText', width: 120 },
        { title: '创建时间', dataIndex: 'createTime', width: 170 },
      ]}
    />
  );
}

export function StatementDownload() {
  return (
    <Simple
      api="/finance/statement/getStatementStatisticsList"
      rowKey="invoiceId"
      label="日期"
      columns={[
        { title: '日期', dataIndex: 'statementDate', width: 160 },
        { title: '账户', dataIndex: 'accountType', width: 140 },
        { title: '收入', dataIndex: 'income', width: 150 },
        { title: '支出', dataIndex: 'expenditure', width: 150 },
      ]}
    />
  );
}
