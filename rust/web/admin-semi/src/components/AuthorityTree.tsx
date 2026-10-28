import { useEffect, useState } from 'react';
import { Checkbox, Spin, Typography } from '@douyinfe/semi-ui';
import { apiGet } from '../api';

const { Text } = Typography;

interface AuthorityNode {
  authorityId?: number;
  authoritySn?: string;
  authorityName?: string;
  id?: number;
  name?: string;
  children?: AuthorityNode[];
  childAuth?: Array<{ authSn?: string; authName?: string }>;
}

interface AuthorityTreeProps {
  value?: string[];
  onChange: (value: string[]) => void;
}

function nodeSn(node: AuthorityNode) {
  return node.authoritySn || `authority-${node.authorityId || node.id}`;
}

function nodeName(node: AuthorityNode) {
  return node.authorityName || node.name || '';
}

function collectSns(node: AuthorityNode, out: string[] = []) {
  const sn = nodeSn(node);
  if (sn) out.push(sn);
  (node.childAuth || []).forEach((item) => item.authSn && out.push(item.authSn));
  (node.children || []).forEach((child) => collectSns(child, out));
  return out;
}

export default function AuthorityTree({ value = [], onChange }: AuthorityTreeProps) {
  const [tree, setTree] = useState<AuthorityNode[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setLoading(true);
    apiGet<AuthorityNode[]>('/authority/authority/getAllAuthority', { adminType: 'admin' })
      .then((result) => setTree(result || []))
      .catch(() => undefined)
      .finally(() => setLoading(false));
  }, []);

  const allSns = collectSns({ children: tree } as AuthorityNode);
  const effective = value.includes('all') ? allSns : value;

  const toggle = (node: AuthorityNode, checked: boolean) => {
    const sns = collectSns(node);
    const next = new Set(effective);
    sns.forEach((sn) => (checked ? next.add(sn) : next.delete(sn)));
    onChange(Array.from(next));
  };

  const renderNode = (node: AuthorityNode, depth = 0) => {
    const sn = nodeSn(node);
    const checked = effective.includes(sn);
    return (
      <div key={sn} className="authority-tree-node" style={{ paddingLeft: depth * 20 }}>
        <Checkbox checked={checked} onChange={(event) => toggle(node, Boolean(event.target.checked))}>
          {nodeName(node)}
        </Checkbox>
        {(node.childAuth || []).map((auth) => (
          <Checkbox
            key={auth.authSn}
            className="authority-tree-child"
            checked={Boolean(auth.authSn && effective.includes(auth.authSn))}
            onChange={(event) => {
              const next = new Set(effective);
              if (auth.authSn) {
                if (event.target.checked) next.add(auth.authSn);
                else next.delete(auth.authSn);
              }
              onChange(Array.from(next));
            }}
          >
            {auth.authName}
          </Checkbox>
        ))}
        {(node.children || []).map((child) => renderNode(child, depth + 1))}
      </div>
    );
  };

  return (
    <Spin spinning={loading}>
      <div className="authority-tree">
        <div className="authority-tree-all">
          <Checkbox
            checked={value.includes('all')}
            onChange={(event) => onChange(event.target.checked ? ['all'] : [])}
          >
            全选
          </Checkbox>
          <Text type="tertiary" size="small">
            勾选父级会同时选中其子权限
          </Text>
        </div>
        {tree.map((node) => renderNode(node))}
      </div>
    </Spin>
  );
}
