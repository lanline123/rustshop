import test from 'node:test';
import assert from 'node:assert/strict';
import { createConnector, TOOL_DEFINITIONS } from '../src/connector.mjs';

const TOOL_NAMES = [
  'tigshop_connection_status', 'tigshop_product_query', 'tigshop_product_prepare',
  'tigshop_product_execute', 'tigshop_product_status', 'tigshop_product_copy', 'tigshop_category_query',
  'tigshop_capabilities', 'tigshop_media_upload', 'tigshop_media_query', 'tigshop_media_prepare',
  'tigshop_media_execute', 'tigshop_category_prepare', 'tigshop_category_execute', 'tigshop_product_analysis'
];

function connectorWith(calls, payload = {}) {
  return createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url, options) => { calls.push({ url, options }); return new Response(JSON.stringify(payload), { status: 200 }); }
  });
}

test('exposes the domain tools including capabilities and media upload', async () => {
  assert.deepEqual(Object.keys(TOOL_DEFINITIONS).sort(), [...TOOL_NAMES].sort());
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret' });
  const listed = await connector.handle({ jsonrpc: '2.0', id: 1, method: 'tools/list' });
  assert.deepEqual(listed.result.tools.map((tool) => tool.name).sort(), [...TOOL_NAMES].sort());
  assert.equal(listed.result.tools.some((tool) => tool.name === 'tigshop_import_products'), false);
});
test('publishes strict schemas and annotations for domain tools', () => {
  for (const tool of Object.values(TOOL_DEFINITIONS)) {
    assert.equal(tool.inputSchema.type, 'object');
    assert.equal(tool.inputSchema.additionalProperties, false);
  }
  assert.deepEqual(TOOL_DEFINITIONS.tigshop_product_query.inputSchema.properties.operation.enum, ['search', 'detail', 'promotion_links', 'context']);
  assert.deepEqual(TOOL_DEFINITIONS.tigshop_product_prepare.inputSchema.properties.operation.enum, ['requirements', 'validate', 'batch_preview']);
  assert.deepEqual(TOOL_DEFINITIONS.tigshop_product_execute.inputSchema.properties.operation.enum, ['create', 'update', 'recycle', 'restore', 'delete', 'batch']);
  assert.equal(TOOL_DEFINITIONS.tigshop_product_query.annotations.readOnlyHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_product_execute.annotations.destructiveHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_upload.inputSchema.properties.confirmation.const, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_upload.inputSchema.properties.files.items.properties.fileToken.type, 'string');
  assert.equal(TOOL_DEFINITIONS.tigshop_media_upload.inputSchema.properties.files.items.anyOf, undefined);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_upload.inputSchema.properties.files.items.required.includes('fileToken'), true);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_upload.inputSchema.properties.files.items.properties.contentBase64, undefined);
  assert.match(TOOL_DEFINITIONS.tigshop_product_prepare.description, /自然语言/);
  assert.match(TOOL_DEFINITIONS.tigshop_product_execute.description, /明确确认/);
  assert.match(TOOL_DEFINITIONS.tigshop_media_upload.description, /明确确认/);
  assert.match(TOOL_DEFINITIONS.tigshop_media_upload.description, /fileToken/);
  assert.match(TOOL_DEFINITIONS.tigshop_capabilities.description, /全局指引/);
});

test('publishes vision-model guidance for image product workflows', async () => {
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret' });
  const initialized = await connector.handle({ jsonrpc: '2.0', id: 99, method: 'initialize' });
  assert.match(initialized.result.instructions, /支持图片识别/);
  assert.match(initialized.result.instructions, /按名称包含匹配/);
  assert.match(initialized.result.instructions, /不是语义搜索/);
  assert.match(initialized.result.instructions, /推荐只是建议/);
  assert.match(initialized.result.instructions, /分类导入必须经过用户确认/);
  assert.match(initialized.result.instructions, /multipart/);
  assert.match(initialized.result.instructions, /fileToken/);
  assert.match(initialized.result.instructions, /media\/upload-token/);
  const calls = [];
  const withCapabilities = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url, options) => { calls.push({ url, options }); return new Response(JSON.stringify({ assistant: { requiresVisionModelForImages: true, visionModelGuidance: '请使用支持图片识别的模型', imageWorkflow: 'vision_required' } }), { status: 200 }); }
  });
  const result = await withCapabilities.callTool('tigshop_capabilities', {});
  assert.equal(result.structuredContent.assistant.requiresVisionModelForImages, true);
  assert.equal(result.structuredContent.assistant.imageWorkflow, 'vision_required');
});

test('initialization instructions define one deterministic cross-client workflow', async () => {
  const connector = connectorWith([], {});
  const initialized = await connector.handle({ jsonrpc: '2.0', id: 101, method: 'initialize' });
  const instructions = initialized.result.instructions;
  for (const rule of [
    '先调用 tigshop_capabilities',
    'product_query(operation=context)',
    '用户提供的商品编码优先',
    '收到、好的、继续不算确认',
    '快速素材上传',
    '审核未通过不得上架',
    '重复编码时重新查询',
    '删除素材不可恢复',
    '不输出密钥、Base64、本地绝对路径'
  ]) assert.match(instructions, new RegExp(rule.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')), rule);
});

test('capabilities summary explains the multipart fileToken upload path', async () => {
  const connector = connectorWith([], {
    media: {
      preferredUploadMode: 'fileToken',
      stageEndpoint: '/adminapi/openapi/mcp/v1/media/upload-token',
      strictUploadMode: true,
      workflowVersion: '2026-09-04-upload-token-v2'
    }
  });
  const result = await connector.callTool('tigshop_capabilities', {});
  assert.match(result.content[0].text, /只允许.*fileToken/);
  assert.match(result.content[0].text, /multipart/);
  assert.match(result.content[0].text, /fileToken/);
  assert.match(result.content[0].text, /media\/upload-token/);
});

test('adds a vision-model reminder to image upload summaries', async () => {
  const connector = connectorWith([], { status: 'success', total: 1, successCount: 1, failedCount: 0, assets: [{ assetId: 1, type: 'image', url: 'https://cdn/a.jpg' }] });
  const result = await connector.callTool('tigshop_media_upload', { files: [{ fileToken: 'token', role: 'main_image' }], confirmation: true });
  assert.match(result.content[0].text, /支持图片识别/);
});

test('rejects Base64 media before any network call', async () => {
  const calls = [];
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async (...args) => { calls.push(args); return new Response('{}'); } });
  const result = await connector.callTool('tigshop_media_upload', { files: [{ name: 'a.jpg', mimeType: 'image/jpeg', contentBase64: 'AAAA' }], confirmation: true });
  assert.equal(result.isError, true);
  assert.equal(result.structuredContent.errorCode, 'UPLOAD_MODE_REQUIRED');
  assert.equal(calls.length, 0);
});

test('rejects a missing fileToken before capabilities or upload calls', async () => {
  const calls = [];
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async (...args) => { calls.push(args); return new Response('{}'); } });
  const result = await connector.callTool('tigshop_media_upload', { files: [{ name: 'a.jpg', mimeType: 'image/jpeg' }], confirmation: true });
  assert.equal(result.isError, true);
  assert.equal(result.structuredContent.errorCode, 'UPLOAD_MODE_REQUIRED');
  assert.equal(calls.length, 0);
});
test('explains category matching limits and confirmation flow', async () => {
  const connector = connectorWith([], { categories: [], total: 0 });
  const result = await connector.callTool('tigshop_category_query', { keyword: '狗玩具' });
  assert.match(result.content[0].text, /名称包含匹配/);
  assert.match(result.content[0].text, /不是语义搜索/);
  assert.match(result.content[0].text, /推荐只是建议/);
  assert.match(result.content[0].text, /确认后才能创建分类/);
});

test('publishes a complete strict product draft schema for natural-language fields', () => {
  const expected = ['productName', 'productSn', 'productPrice', 'categoryId', 'categoryName', 'productStock', 'productStatus', 'productBrief', 'productDesc', 'images', 'detailImages', 'media', 'marketPrice', 'costPrice', 'productTsn', 'keywords', 'skus', 'fieldSources'];
  for (const toolName of ['tigshop_product_prepare', 'tigshop_product_execute']) {
    const products = TOOL_DEFINITIONS[toolName].inputSchema.properties.products;
    const item = products.items;
    assert.equal(products.maxItems, 50, `${toolName} products.maxItems must match the server limit`);
    assert.equal(item.additionalProperties, false, `${toolName} products.items must be strict`);
    assert.deepEqual(Object.keys(item.properties).sort(), [...expected].sort(), toolName);
    assert.equal(item.properties.fieldSources.additionalProperties, false);
    assert.equal(item.properties.fieldSources.patternProperties['^.+$'].enum.includes('ai_recommended'), true);
  }
});

test('routes product query operations to existing REST endpoints', async () => {
  const cases = [
    [{ operation: 'search', keyword: '耳机', page: 1, size: 20 }, /\/products\?keyword=%E8%80%B3%E6%9C%BA&page=1&size=20$/],
    [{ operation: 'detail', productId: 7 }, /\/products\/7$/],
    [{ operation: 'promotion_links', productId: 7 }, /\/products\/7\/promotion-links$/],
    [{ operation: 'context', keyword: '耳机', sampleSize: 5 }, /\/products\/context\?keyword=%E8%80%B3%E6%9C%BA&sampleSize=5$/]
  ];
  for (const [args, expected] of cases) {
    const calls = [];
    const connector = connectorWith(calls, { ok: true });
    const response = await connector.handle({ jsonrpc: '2.0', id: 2, method: 'tools/call', params: { name: 'tigshop_product_query', arguments: args } });
    assert.equal(response.result.isError, undefined);
    assert.match(calls[0].url, expected);
  }
});

test('context summary exposes encoding evidence and confirmation boundary', async () => {
  const connector = connectorWith([], {
    sampleSize: 3,
    metrics: { productCount: 42 },
    encoding: {
      samples: ['SN00021', 'SN00022', 'SN00023'],
      pattern: 'SN{5位数字}',
      nextCandidate: 'SN00024',
      confidence: 0.99,
      needsConfirmation: false,
      reason: '多个商品使用同一前缀和数字位数，按最大序号顺延'
    }
  });
  const response = await connector.callTool('tigshop_product_query', { operation: 'context', keyword: '鞋' });
  assert.match(response.content[0].text, /SN00021、SN00022、SN00023/);
  assert.match(response.content[0].text, /SN\{5位数字\}/);
  assert.match(response.content[0].text, /SN00024/);
  assert.match(response.content[0].text, /99%/);
  assert.match(response.content[0].text, /必须.*确认/);
});

test('context summary refuses to invent a code when history is insufficient', async () => {
  const connector = connectorWith([], {
    sampleSize: 1,
    encoding: { samples: ['SN00021'], confidence: 0, needsConfirmation: true, reason: '可用商品编码样本不足，无法可靠推断格式' }
  });
  const response = await connector.callTool('tigshop_product_query', { operation: 'context', keyword: '鞋' });
  assert.match(response.content[0].text, /样本不足/);
  assert.match(response.content[0].text, /提供或确认.*编码规则/);
  assert.doesNotMatch(response.content[0].text, /下一个候选编码为/);
});

test('routes capabilities lookup to the capabilities endpoint', async () => {
  const calls = [];
  const connector = connectorWith(calls, { version: '1.0', media: { maxBatchBytes: 40 } });
  const response = await connector.handle({ jsonrpc: '2.0', id: 20, method: 'tools/call', params: { name: 'tigshop_capabilities', arguments: {} } });
  assert.equal(response.result.isError, undefined);
  assert.match(calls[0].url, /\/capabilities$/);
  assert.equal(calls[0].options.method, 'GET');
});

test('capabilities documents the CSP-safe local gallery preview fallback', async () => {
  const calls = [];
  const connector = connectorWith(calls, {
    media: {
      supportsMarkdownPreview: true,
      externalUrlMayBeBlockedByCsp: true,
      previewFallback: 'client_local_gallery'
    }
  });
  const response = await connector.callTool('tigshop_capabilities', {});
  assert.equal(response.structuredContent.media.supportsMarkdownPreview, true);
  assert.equal(response.structuredContent.media.externalUrlMayBeBlockedByCsp, true);
  assert.equal(response.structuredContent.media.previewFallback, 'client_local_gallery');
});

test('validates media uploads before network and never sends local paths', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  for (const arguments_ of [
    { files: [{ name: 'C:\\tmp\\a.jpg', mimeType: 'image/jpeg', contentBase64: 'AAAA' }], confirmation: true },
    { files: [{ name: 'a.jpg', mimeType: 'image/jpeg', contentBase64: 'not base64 !!!' }], confirmation: true },
    { files: [{ fileToken: 'token-a', size: 2 }], confirmation: false }
  ]) {
    const response = await connector.handle({ jsonrpc: '2.0', id: 21, method: 'tools/call', params: { name: 'tigshop_media_upload', arguments: arguments_ } });
    assert.equal(response.result.isError, true);
    assert.equal(response.result.structuredContent.errorCode, arguments_.confirmation === false ? 'INVALID_ARGUMENT' : 'UPLOAD_MODE_REQUIRED');
    assert.equal(JSON.stringify(response).includes('AAAA'), false);
    assert.equal(JSON.stringify(response).includes('C:\\tmp'), false);
  }
  assert.equal(calls, 0);
});

test('accepts short-lived fileToken without forcing Base64 payload', async () => {
  const calls = [];
  const connector = connectorWith(calls, { status: 'success', total: 1, successCount: 1, failedCount: 0, assets: [] });
  const response = await connector.handle({ jsonrpc: '2.0', id: 211, method: 'tools/call', params: { name: 'tigshop_media_upload', arguments: {
    files: [{ fileToken: 'mcp_token_123', role: 'main_image' }], confirmation: true
  } } });
  assert.equal(response.result.isError, undefined);
  const upload = calls.find((call) => call.url.endsWith('/media/upload'));
  assert.ok(upload);
  assert.match(upload.options.body, /mcp_token_123/);
});

test('batches media by capabilities and returns one merged summary', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith('/capabilities')) return new Response(JSON.stringify({ media: { maxBatchBytes: 3 } }), { status: 200 });
      return new Response(JSON.stringify({ status: 'success', total: 1, successCount: 1, failedCount: 0, assets: [{ assetId: calls.length, type: 'image', url: 'https://cdn/a.jpg' }], failureSummary: {} }), { status: 200 });
    }
  });
  const response = await connector.handle({ jsonrpc: '2.0', id: 22, method: 'tools/call', params: { name: 'tigshop_media_upload', arguments: {
    files: [
      { fileToken: 'token-a', size: 2 },
      { fileToken: 'token-b', size: 2 }
    ], confirmation: true
  } } });
  assert.equal(response.result.isError, undefined);
  assert.equal(calls.filter((call) => call.url.endsWith('/media/upload')).length, 2);
  assert.equal(response.result.structuredContent.total, 2);
  assert.equal(response.result.structuredContent.successCount, 2);
  assert.equal(response.result.content.length, 1);
  assert.match(response.result.content[0].text, /本次上传完成：共 2 个文件/);
});

test('forwards the default media group to every upload batch', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith('/capabilities')) return new Response(JSON.stringify({ media: { maxBatchBytes: 40 } }), { status: 200 });
      return new Response(JSON.stringify({ status: 'success', total: 1, successCount: 1, failedCount: 0, assets: [] }), { status: 200 });
    }
  });
  await connector.callTool('tigshop_media_upload', {
    groupId: 9,
    files: [{ fileToken: 'token-a', size: 2 }], confirmation: true
  });
  const upload = calls.find((call) => call.url.endsWith('/media/upload'));
  assert.equal(JSON.parse(upload.options.body).groupId, 9);
});

test('routes prepare operations without performing writes', async () => {
  const calls = [];
  const connector = connectorWith(calls, { valid: true, previewId: 'pv-1' });
  await connector.handle({ jsonrpc: '2.0', id: 3, method: 'tools/call', params: { name: 'tigshop_product_prepare', arguments: { operation: 'validate', products: [{ productName: 'A' }] } } });
  assert.equal(calls[0].options.method, 'POST');
  assert.match(calls[0].url, /\/products\/validate$/);
  assert.deepEqual(JSON.parse(calls[0].options.body), { products: [{ productName: 'A' }] });
});

test('routes execute create and carries confirmation plus idempotency', async () => {
  const calls = [];
  const connector = connectorWith(calls, { success: true, total: 1 });
  await connector.handle({ jsonrpc: '2.0', id: 4, method: 'tools/call', params: { name: 'tigshop_product_execute', arguments: { operation: 'create', products: [{ productName: 'A' }], previewId: 'pv-1', confirmation: true, requestId: 'req-1' } } });
  assert.match(calls[0].url, /\/products\/import$/);
  assert.equal(calls[0].options.headers['Idempotency-Key'], 'req-1');
  assert.deepEqual(JSON.parse(calls[0].options.body), { products: [{ productName: 'A' }], previewId: 'pv-1', confirmation: true });
});

test('create result shows one business summary with post-write verification', async () => {
  const connector = connectorWith([], {
    total: 1,
    successCount: 1,
    failureCount: 0,
    items: [{ productName: '商品A', productSn: 'A001', productId: 7, success: true, verification: { status: 'verified' } }]
  });
  const response = await connector.callTool('tigshop_product_execute', {
    operation: 'create', products: [{ productName: '商品A' }], previewId: 'pv-1', requestId: 'req-1', confirmation: true
  });
  assert.equal(response.content.length, 1);
  assert.match(response.content[0].text, /写后核对/);
  assert.match(response.content[0].text, /我还能帮你/);
  assert.match(response.content[0].text, /我还能帮你/);
  assert.match(response.content[0].text, /推广链接/);
  assert.match(response.content[0].text, /库存/);
  assert.match(response.content[0].text, /商品A/);
  assert.equal(JSON.stringify(response).includes('Base64'), false);
});

test('routes execute update to the field endpoint', async () => {
  const calls = [];
  const connector = connectorWith(calls, { changedField: 'productName' });
  await connector.handle({ jsonrpc: '2.0', id: 41, method: 'tools/call', params: { name: 'tigshop_product_execute', arguments: { operation: 'update', productId: 7, field: 'productName', value: '新名称', confirmation: true } } });
  assert.match(calls[0].url, /\/products\/7\/field$/);
  assert.deepEqual(JSON.parse(calls[0].options.body), { field: 'productName', value: '新名称' });
});

test('rejects invalid operations and unsafe writes before fetch', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  const cases = [
    ['tigshop_product_query', { operation: 'unknown' }], ['tigshop_product_query', { operation: 'detail' }],
    ['tigshop_product_prepare', { operation: 'validate', products: [] }],
    ['tigshop_product_execute', { operation: 'create', products: [{}], previewId: 'pv-1', requestId: 'r1' }],
    ['tigshop_product_execute', { operation: 'batch', mode: 'execute', productIds: [1], previewId: 'pv-1', confirmation: true }],
    ['tigshop_product_execute', { operation: 'batch', mode: 'preview', productIds: Array.from({ length: 51 }, (_, i) => i + 1) }],
    ['tigshop_product_status', { productId: 0, onSale: true }], ['tigshop_product_copy', { productId: 0 }]
  ];
  for (const [name, arguments_] of cases) {
    const response = await connector.handle({ jsonrpc: '2.0', id: 5, method: 'tools/call', params: { name, arguments: arguments_ } });
    assert.equal(response.result.isError, true, name);
    assert.equal(response.result.structuredContent.errorCode, 'INVALID_ARGUMENT', name);
  }
  assert.equal(calls, 0);
});

test('rejects product batches larger than the supported limit before fetch', async () => {
  let calls = 0;
  const connector = createConnector({
    baseUrl: 'http://example.test',
    apiKey: 'secret',
    fetchImpl: async () => { calls += 1; return new Response('{}'); }
  });
  const products = Array.from({ length: 51 }, (_, index) => ({ productName: `商品${index}` }));

  const prepare = await connector.handle({ jsonrpc: '2.0', id: 51, method: 'tools/call', params: {
    name: 'tigshop_product_prepare', arguments: { operation: 'validate', products }
  }});
  const execute = await connector.handle({ jsonrpc: '2.0', id: 52, method: 'tools/call', params: {
    name: 'tigshop_product_execute', arguments: {
      operation: 'create', products, previewId: 'pv-1', requestId: 'req-1', confirmation: true
    }
  }});

  assert.equal(prepare.result.isError, true);
  assert.equal(execute.result.isError, true);
  assert.equal(calls, 0);
});

test('returns retryable errors and readable summaries', async () => {
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => new Response(JSON.stringify({ errorCode: 'DOWN', message: '暂时不可用' }), { status: 503 }) });
  const response = await connector.handle({ jsonrpc: '2.0', id: 6, method: 'tools/call', params: { name: 'tigshop_product_query', arguments: { operation: 'search' } } });
  assert.equal(response.result.isError, true);
  assert.equal(response.result.structuredContent.retryable, true);
  assert.equal(typeof response.result.structuredContent.suggestion, 'string');
  const search = connectorWith([], { total: 2, page: 1, size: 20, items: [{ productId: 1 }, { productId: 2 }] });
  const ok = await search.handle({ jsonrpc: '2.0', id: 7, method: 'tools/call', params: { name: 'tigshop_product_query', arguments: { operation: 'search' } } });
  assert.match(ok.result.content[0].text, /已找到 2 个商品/);
});

test('does not leak configured API key in upstream errors', async () => {
  const secret = 'wb_super_secret';
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: secret, fetchImpl: async () => new Response(JSON.stringify({ message: `server saw ${secret}` }), { status: 400 }) });
  const response = await connector.handle({ jsonrpc: '2.0', id: 8, method: 'tools/call', params: { name: 'tigshop_product_prepare', arguments: { operation: 'requirements' } } });
  assert.equal(JSON.stringify(response).includes(secret), false);
});

test('does not leak media base64 or local paths from upstream errors', async () => {
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async () => new Response(JSON.stringify({ message: 'bad C:\\secret\\shoe.jpg', contentBase64: 'AAAA' }), { status: 400 })
  });
  const response = await connector.handle({ jsonrpc: '2.0', id: 23, method: 'tools/call', params: { name: 'tigshop_capabilities', arguments: {} } });
  assert.equal(JSON.stringify(response).includes('C:\\secret'), false);
  assert.equal(JSON.stringify(response).includes('AAAA'), false);
});

test('does not leak raw base64 embedded in upstream messages', async () => {
  const rawBase64 = 'QUJDREVGR0hJSktMTU5PUFFSU1RVVldYWVo=';
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async () => new Response(JSON.stringify({ message: `upload rejected: ${rawBase64}` }), { status: 400 })
  });
  const response = await connector.handle({ jsonrpc: '2.0', id: 24, method: 'tools/call', params: { name: 'tigshop_capabilities', arguments: {} } });
  assert.equal(JSON.stringify(response).includes(rawBase64), false);
});

test('stops media batches after authentication or transport failure', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url, options) => {
      calls.push({ url, options });
      if (url.endsWith('/capabilities')) return new Response(JSON.stringify({ media: { maxBatchBytes: 3 } }), { status: 200 });
      return new Response(JSON.stringify({ errorCode: 'AUTH', message: 'expired' }), { status: 401 });
    }
  });
  const response = await connector.callTool('tigshop_media_upload', { files: [
    { fileToken: 'token-a', size: 2 },
    { fileToken: 'token-b', size: 2 }
  ], confirmation: true });
  assert.equal(response.isError, true);
  assert.equal(calls.filter((call) => call.url.endsWith('/media/upload')).length, 1);
});

test('stops remaining media batches on any 5xx response and counts the failed batch', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url) => {
      calls.push(url);
      if (url.endsWith('/capabilities')) return new Response(JSON.stringify({ media: { maxBatchBytes: 3 } }), { status: 200 });
      if (calls.filter((item) => item.endsWith('/media/upload')).length === 1) {
        return new Response(JSON.stringify({ status: 'failed', failedCount: 0, retryable: false }), { status: 503 });
      }
      return new Response(JSON.stringify({ status: 'success', successCount: 1, failedCount: 0, assets: [] }), { status: 200 });
    }
  });
  const response = await connector.callTool('tigshop_media_upload', { files: [
    { fileToken: 'token-a', size: 2 },
    { fileToken: 'token-b', size: 2 }
  ], confirmation: true });
  assert.equal(response.isError, true);
  assert.equal(calls.filter((call) => call.endsWith('/media/upload')).length, 1);
  assert.equal(response.structuredContent.failedCount, 2);
});

test('counts every file in an authentication-failed batch even when upstream reports zero failures', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url) => {
      calls.push(url);
      return url.endsWith('/capabilities')
        ? new Response(JSON.stringify({ media: { maxBatchBytes: 3 } }), { status: 200 })
        : new Response(JSON.stringify({ status: 'failed', failedCount: 0, errorCode: 'AUTH' }), { status: 401 });
    }
  });
  const response = await connector.callTool('tigshop_media_upload', { files: [
    { fileToken: 'token-a', size: 2 },
    { fileToken: 'token-b', size: 2 }
  ], confirmation: true });
  assert.equal(response.isError, true);
  assert.equal(calls.filter((call) => call.endsWith('/media/upload')).length, 1);
  assert.equal(response.structuredContent.failedCount, 2);
});

test('does not double count server-reported failed files', async () => {
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url) => url.endsWith('/capabilities')
      ? new Response(JSON.stringify({ media: { maxBatchBytes: 40 } }), { status: 200 })
      : new Response(JSON.stringify({ status: 'failed', total: 1, successCount: 0, failedCount: 1, failureSummary: { unsupported_type: 1 } }), { status: 422 })
  });
  const response = await connector.callTool('tigshop_media_upload', { files: [{ fileToken: 'token-a', size: 2 }], confirmation: true });
  assert.equal(response.structuredContent.failedCount, 1);
});

test('rejects unknown media fields and never forwards localPath', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  const response = await connector.callTool('tigshop_media_upload', { files: [{ name: 'a.jpg', mimeType: 'image/jpeg', contentBase64: 'AAAA', localPath: 'D:\\private\\a.jpg' }], confirmation: true });
  assert.equal(response.isError, true);
  assert.equal(response.structuredContent.errorCode, 'UPLOAD_MODE_REQUIRED');
  assert.equal(calls, 0);
});

test('uses token metadata for batching and uploads the token', async () => {
  const calls = [];
  const connector = createConnector({
    baseUrl: 'https://tigshop.example', apiKey: 'wb_secret',
    fetchImpl: async (url) => { calls.push(url); return url.endsWith('/capabilities') ? new Response(JSON.stringify({ media: { maxBatchBytes: 2 } }), { status: 200 }) : new Response('{}'); }
  });
  const response = await connector.callTool('tigshop_media_upload', { files: [{ fileToken: 'token-a', size: 2 }], confirmation: true });
  assert.equal(response.isError, undefined);
  assert.equal(calls.filter((url) => url.endsWith('/media/upload')).length, 1);
});

test('publishes media category and product analysis domain tools', () => {
  const expected = [
    'tigshop_media_query', 'tigshop_media_prepare', 'tigshop_media_execute',
    'tigshop_category_prepare', 'tigshop_category_execute', 'tigshop_product_analysis'
  ];
  for (const name of expected) assert.ok(TOOL_DEFINITIONS[name], name);
  for (const name of expected) {
    assert.equal(TOOL_DEFINITIONS[name].inputSchema.additionalProperties, false, name);
  }
  assert.equal(TOOL_DEFINITIONS.tigshop_media_query.annotations.readOnlyHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_prepare.annotations.readOnlyHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_media_execute.annotations.destructiveHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_category_prepare.annotations.readOnlyHint, true);
  assert.equal(TOOL_DEFINITIONS.tigshop_category_execute.annotations.destructiveHint, false);
  assert.equal(TOOL_DEFINITIONS.tigshop_product_analysis.annotations.readOnlyHint, true);
});

test('routes media query and mutation operations to generic media endpoints', async () => {
  const calls = [];
  const connector = connectorWith(calls, { items: [], total: 0 });
  await connector.callTool('tigshop_media_query', { operation: 'list', type: 'image', groupId: 3, page: 1, pageSize: 20 });
  await connector.callTool('tigshop_media_prepare', { operation: 'delete', ids: [9], requestId: 'm-1' });
  await connector.callTool('tigshop_media_execute', { operation: 'delete', ids: [9], previewId: 'pv-m1', requestId: 'm-1', confirmation: true });
  assert.match(calls[0].url, /\/media(?:\?.*)?$/);
  assert.equal(calls[0].options.method, 'GET');
  assert.match(calls[1].url, /\/media\/prepare$/);
  assert.equal(calls[1].options.method, 'POST');
  assert.match(calls[2].url, /\/media\/execute$/);
  assert.deepEqual(JSON.parse(calls[2].options.body), { operation: 'delete', ids: [9], previewId: 'pv-m1', requestId: 'm-1', confirmation: true });
});

test('media query summary exposes item details and image preview', async () => {
  const connector = connectorWith([], {
    total: 1,
    items: [{ id: 123, name: '夏季女鞋.jpg', url: 'https://cdn.example.com/shoe.jpg', type: 'image', groupId: 7, format: 'jpg' }]
  });
  const response = await connector.callTool('tigshop_media_query', { operation: 'list', type: 'image', page: 1, pageSize: 20 });
  assert.match(response.content[0].text, /夏季女鞋\.jpg/);
  assert.match(response.content[0].text, /素材 ID：123/);
  assert.match(response.content[0].text, /!\[夏季女鞋\.jpg\]\(https:\/\/cdn\.example\.com\/shoe\.jpg\)/);
});

test('summarizes the groups field returned by the media group tree endpoint', async () => {
  const connector = connectorWith([], { groups: [{ id: 1 }, { id: 2 }] });
  const response = await connector.callTool('tigshop_media_query', { operation: 'groups', type: 'image' });
  assert.match(response.content[0].text, /已返回 2 个素材分组/);
});

test('rejects removed media recycle and restore operations', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  for (const operation of ['recycle', 'restore']) {
    const result = await connector.callTool('tigshop_media_prepare', { operation, type: 'image', ids: [9], requestId: `m-${operation}` });
    assert.equal(result.isError, true);
    assert.equal(result.structuredContent.errorCode, 'INVALID_ARGUMENT');
  }
  assert.equal(calls, 0);
});

test('rejects media execute without confirmation before network', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  const result = await connector.callTool('tigshop_media_execute', { operation: 'delete', ids: [9], previewId: 'pv-m1', requestId: 'm-1', confirmation: false });
  assert.equal(result.isError, true);
  assert.equal(result.structuredContent.errorCode, 'INVALID_ARGUMENT');
  assert.equal(calls, 0);
});

test('deletes media only through the confirmed preview flow', async () => {
  const calls = [];
  const connector = connectorWith(calls, { status: 'success', summary: 'deleted' });
  const result = await connector.callTool('tigshop_media_execute', { operation: 'delete', type: 'image', ids: [9], previewId: 'pv-m1', requestId: 'm-1', confirmation: true });
  assert.equal(result.isError, undefined);
  assert.equal(calls.length, 1);
});

test('requires an explicit media type for destructive id operations', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  const result = await connector.callTool('tigshop_media_execute', { operation: 'delete', type: 'audio', ids: [9], permanent: true, previewId: 'pv-m1', requestId: 'm-1', confirmation: true });
  assert.equal(result.isError, true);
  assert.equal(result.structuredContent.errorCode, 'INVALID_ARGUMENT');
  assert.equal(calls, 0);
});

test('routes category prepare and execute with parent confirmation fields', async () => {
  const calls = [];
  const connector = connectorWith(calls, { previewId: 'cat-pv-1', nodes: [] });
  const nodes = [{ name: '服装', children: [{ name: '女装' }] }];
  await connector.callTool('tigshop_category_prepare', { operation: 'import', nodes, requestId: 'cat-1' });
  await connector.callTool('tigshop_category_execute', { operation: 'import', nodes, previewId: 'cat-pv-1', requestId: 'cat-1', confirmation: true });
  assert.match(calls[0].url, /\/categories\/prepare$/);
  assert.match(calls[1].url, /\/categories\/execute$/);
  assert.deepEqual(JSON.parse(calls[1].options.body), { operation: 'import', nodes, previewId: 'cat-pv-1', requestId: 'cat-1', confirmation: true });
});

test('routes product analysis operations as read-only queries', async () => {
  const calls = [];
  const connector = connectorWith(calls, { metrics: {}, dataScope: { sampleSize: 0 } });
  const result = await connector.callTool('tigshop_product_analysis', { operation: 'inventory_risk', categoryId: 7, sampleSize: 20 });
  assert.equal(result.isError, undefined);
  assert.match(calls[0].url, /\/products\/analysis\?operation=inventory_risk&categoryId=7&sampleSize=20$/);
  assert.equal(calls[0].options.method, 'GET');
});

test('requires preview, request id and confirmation for category execute', async () => {
  let calls = 0;
  const connector = createConnector({ baseUrl: 'https://tigshop.example', apiKey: 'wb_secret', fetchImpl: async () => { calls += 1; return new Response('{}'); } });
  const result = await connector.callTool('tigshop_category_execute', { operation: 'import', nodes: [{ name: '服装' }], confirmation: true });
  assert.equal(result.isError, true);
  assert.equal(result.structuredContent.errorCode, 'INVALID_ARGUMENT');
  assert.equal(calls, 0);
});
