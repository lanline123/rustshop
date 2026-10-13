const DEFAULT_PROTOCOL_VERSION = '2025-06-18';
const READ_ONLY = { readOnlyHint: true, destructiveHint: false, idempotentHint: true, openWorldHint: false };
const WRITE = { readOnlyHint: false, destructiveHint: false, idempotentHint: false, openWorldHint: false };
const DESTRUCTIVE = { ...WRITE, destructiveHint: true };
const PRODUCT_ID = { type: 'integer', minimum: 1, description: 'Tigshop 商品 ID' };
export const GLOBAL_INSTRUCTIONS = '你是 Tigshop 智能商品助手。每次新会话首次处理商品或素材时先调用 tigshop_capabilities，读取实时限制和支持方式；优先理解用户自然语言和附件，不要求固定模板，只询问当前阻塞信息；创建商品前必须调用 product_query(operation=context)，用户提供的商品编码优先，其次才使用历史高置信度候选。编码候选只是建议，样本不足、格式不统一、复杂格式或序号溢出时必须让用户提供或确认规则；重复编码时重新查询，不能随机生成或硬编码为 SN。用户明确输入优先于后台配置、历史推断和系统默认；AI 推荐只是建议，必须标注依据和来源。分类查询按名称包含匹配，不是语义搜索；没有合适的末级分类时先推荐并询问用户，不得擅自创建分类；分类导入必须先 prepare 预览，分类导入必须经过用户确认后 execute。图片和视频上传严格只允许 multipart/form-data 调用 /adminapi/openapi/mcp/v1/media/upload-token 获取短时 fileToken，再调用 tigshop_media_upload 传 fileToken；禁止 Base64、本地路径、任意 URL 和其他上传接口，缺少 fileToken 时立即停止并提示升级客户端，不得 fallback 或长时间尝试。图片参与商品创建或分析时必须使用支持图片识别的模型，不支持视觉时先提示切换或补充文字，不得臆测。商品、分类和素材修改/删除先展示草稿或预览；快速素材上传展示文件清单即可。确认必须包含具体对象和动作，收到、好的、继续不算确认。上架前检查审核状态，审核未通过不得上架；删除素材不可恢复。写入完成后返回结果和写后核对状态；任务完成说明风险并主动提供下一步建议，失败时说明可恢复步骤。不输出密钥、Base64、本地绝对路径或内部实现细节。';

function objectSchema(properties = {}, required = [], extra = {}) {
  return { type: 'object', properties, ...(required.length ? { required } : {}), additionalProperties: false, ...extra };
}

const SEARCH_PROPERTIES = {
  keyword: { type: 'string', description: '商品名称或货号关键词' },
  scope: { type: 'string', enum: ['on_sale', 'off_sale', 'pending_audit', 'audit_failed', 'recycled', 'all'], default: 'on_sale' },
  categoryId: { type: 'integer', minimum: 1 }, brandId: { type: 'integer', minimum: 1 }, productType: { type: 'integer', minimum: 1, maximum: 8 },
  minPrice: { type: 'number', minimum: 0 }, maxPrice: { type: 'number', minimum: 0 }, minStock: { type: 'integer', minimum: 0 }, maxStock: { type: 'integer', minimum: 0 },
  minSales: { type: 'integer', minimum: 0 }, maxSales: { type: 'integer', minimum: 0 },
  sortField: { type: 'string', enum: ['added', 'price', 'stock', 'sales', 'sort_order'] }, sortOrder: { type: 'string', enum: ['asc', 'desc'], default: 'desc' },
  page: { type: 'integer', minimum: 1, default: 1 }, size: { type: 'integer', minimum: 1, maximum: 100, default: 20 }
};

const SOURCE_VALUES = ['user_provided', 'ai_recommended', 'site_default'];

function productMediaSchema() {
  return objectSchema({
    assetId: PRODUCT_ID,
    type: { type: 'string' },
    role: { type: 'string' },
    url: { type: 'string' },
    coverUrl: { type: 'string' },
    format: { type: 'string' }
  });
}

function productSkuSchema() {
  return objectSchema({
    specs: { type: 'object', patternProperties: { '^.+$': { type: 'string' } }, additionalProperties: false },
    price: { type: 'number', minimum: 0 },
    stock: { type: 'integer' },
    skuSn: { type: 'string' },
    skuTsn: { type: 'string' },
    marketPrice: { type: 'number', minimum: 0 },
    costPrice: { type: 'number', minimum: 0 }
  });
}

function productDraftSchema() {
  return objectSchema({
    productName: { type: 'string' },
    productSn: { type: 'string' },
    productPrice: { type: 'number', minimum: 0 },
    categoryId: { type: 'integer', minimum: 1 },
    categoryName: { type: 'string' },
    productStock: { type: 'integer' },
    productStatus: { type: 'integer' },
    productBrief: { type: 'string' },
    productDesc: { type: 'string' },
    images: { type: 'array', items: { type: 'string' } },
    detailImages: { type: 'array', items: { type: 'string' } },
    media: { type: 'array', items: productMediaSchema() },
    marketPrice: { type: 'number', minimum: 0 },
    costPrice: { type: 'number', minimum: 0 },
    productTsn: { type: 'string' },
    keywords: { type: 'string' },
    skus: { type: 'array', items: productSkuSchema() },
    fieldSources: { type: 'object', patternProperties: { '^.+$': { type: 'string', enum: SOURCE_VALUES } }, additionalProperties: false }
  });
}

function categoryNodeSchema() {
  return objectSchema({
    id: { type: 'integer', minimum: 1 },
    name: { type: 'string', minLength: 1, maxLength: 100 },
    children: { type: 'array', maxItems: 200, items: { type: 'object' } }
  }, ['name']);
}

export const TOOL_DEFINITIONS = {
  tigshop_capabilities: { name: 'tigshop_capabilities', description: '获取当前 Tigshop MCP 能力、运行时限制和全局指引', inputSchema: objectSchema(), annotations: READ_ONLY },
  tigshop_connection_status: { name: 'tigshop_connection_status', description: '检查 Tigshop MCP 连接和密钥状态', inputSchema: objectSchema(), annotations: READ_ONLY },
  tigshop_product_query: {
    name: 'tigshop_product_query', description: '查询商品：搜索、详情或推广链接由 operation 区分；创建商品前必须用 context 读取当前商城编码习惯',
    inputSchema: objectSchema({ operation: { type: 'string', enum: ['search', 'detail', 'promotion_links', 'context'] }, ...SEARCH_PROPERTIES, productId: PRODUCT_ID, sampleSize: { type: 'integer', minimum: 1, maximum: 100 } }, ['operation']), annotations: READ_ONLY
  },
  tigshop_product_prepare: {
    name: 'tigshop_product_prepare', description: '用自然语言引导新建商品，或校验/预览商品操作；不输出固定客户模板，也不修改商品。创建前先调用 product_query(context) 获取编码候选',
    inputSchema: objectSchema({
      operation: { type: 'string', enum: ['requirements', 'validate', 'batch_preview'] },
      products: { type: 'array', minItems: 1, maxItems: 50, items: productDraftSchema() },
      productIds: { type: 'array', minItems: 1, maxItems: 50, uniqueItems: true, items: PRODUCT_ID },
      batchOperation: { type: 'string', enum: ['up', 'down', 'recycle', 'restore', 'del'] }
    }, ['operation']), annotations: READ_ONLY
  },
  tigshop_product_execute: {
    name: 'tigshop_product_execute', description: '执行商品新建、修改、回收、还原、删除或批量操作；create 必须携带 validate 返回的 previewId、唯一 requestId 和 confirmation: true；必须展示最终草稿并取得用户对具体动作的明确确认，模糊回复不算确认',
    inputSchema: objectSchema({
      operation: { type: 'string', enum: ['create', 'update', 'recycle', 'restore', 'delete', 'batch'] },
      productId: PRODUCT_ID, products: { type: 'array', minItems: 1, maxItems: 50, items: productDraftSchema() }, productIds: { type: 'array', minItems: 1, maxItems: 50, uniqueItems: true, items: PRODUCT_ID },
      field: { type: 'string', enum: ['productName', 'productSn', 'productPrice', 'productStock', 'productSku', 'sortOrder'] }, value: {},
      batchOperation: { type: 'string', enum: ['up', 'down', 'recycle', 'restore', 'del'] }, previewId: { type: 'string', minLength: 1 }, confirmation: { type: 'boolean', const: true }, requestId: { type: 'string', minLength: 1, maxLength: 128 }
    }, ['operation']), annotations: DESTRUCTIVE
  },
  tigshop_product_status: { name: 'tigshop_product_status', description: '上架或下架商品；上架前必须检查审核状态，审核未通过不得承诺上架；必须明确 confirmation: true', inputSchema: objectSchema({ productId: PRODUCT_ID, onSale: { type: 'boolean' }, confirmation: { type: 'boolean', const: true } }, ['productId', 'onSale', 'confirmation']), annotations: { ...WRITE, idempotentHint: true } },
  tigshop_product_copy: { name: 'tigshop_product_copy', description: '复制商品并生成新的商品货号；必须明确 confirmation: true', inputSchema: objectSchema({ productId: PRODUCT_ID, confirmation: { type: 'boolean', const: true } }, ['productId', 'confirmation']), annotations: WRITE },
  tigshop_category_query: { name: 'tigshop_category_query', description: '按名称检索或逐级浏览商品分类', inputSchema: objectSchema({ keyword: { type: 'string', minLength: 1 }, parentId: { type: 'integer', minimum: 0 } }), annotations: READ_ONLY }
  ,tigshop_media_upload: { name: 'tigshop_media_upload', description: '严格按唯一链路批量上传图片或视频：每个文件必须先通过 multipart media/upload-token 获取短时 fileToken，再传 fileToken；禁止 Base64、本地路径和其他上传方式，缺少 token 立即停止；必须取得用户的明确确认并传 confirmation: true', inputSchema: objectSchema({ files: { type: 'array', minItems: 1, maxItems: 20, items: objectSchema({ fileToken: { type: 'string', minLength: 1, description: '由 media/upload-token 返回的短时令牌' }, size: { type: 'integer', minimum: 0, description: '暂存接口返回的文件字节数，用于客户端分批' }, clientFileId: { type: 'string', minLength: 1, maxLength: 128 }, role: { type: 'string', enum: ['main_image', 'gallery_image', 'detail_image', 'video'] }, groupId: { type: 'integer', minimum: 0 } }, ['fileToken']) }, groupId: { type: 'integer', minimum: 0, description: '未在单文件指定时使用的默认素材分组' }, confirmation: { type: 'boolean', const: true } }, ['files', 'confirmation']), annotations: WRITE },
  tigshop_media_query: { name: 'tigshop_media_query', description: '查询素材、素材详情或分组；只返回当前管理后台作用域内的非敏感字段', inputSchema: objectSchema({ operation: { type: 'string', enum: ['list', 'detail', 'groups'] }, id: { type: 'integer', minimum: 1 }, type: { type: 'string', enum: ['image', 'video'] }, groupId: { type: 'integer', minimum: 0 }, keyword: { type: 'string' }, page: { type: 'integer', minimum: 1 }, pageSize: { type: 'integer', minimum: 1, maximum: 100 } }, ['operation']), annotations: READ_ONLY },
  tigshop_media_prepare: { name: 'tigshop_media_prepare', description: '预览素材修改或分组操作；上传必须使用 tigshop_media_upload 的 fileToken 单一路径', inputSchema: objectSchema({ operation: { type: 'string', enum: ['update', 'delete', 'group_create', 'group_update', 'group_delete'] }, items: { type: 'array', minItems: 1, maxItems: 50, items: objectSchema({ id: { type: 'integer', minimum: 1 }, ids: { type: 'array', minItems: 1, items: { type: 'integer', minimum: 1 } }, name: { type: 'string', minLength: 1, maxLength: 200 }, groupId: { type: 'integer', minimum: 0 }, mimeType: { type: 'string' } }) }, id: { type: 'integer', minimum: 1 }, ids: { type: 'array', minItems: 1, maxItems: 50, uniqueItems: true, items: { type: 'integer', minimum: 1 } }, type: { type: 'string', enum: ['image', 'video'] }, name: { type: 'string', minLength: 1, maxLength: 200 }, groupId: { type: 'integer', minimum: 0 }, requestId: { type: 'string', minLength: 1, maxLength: 128 } }, ['operation', 'requestId']), annotations: READ_ONLY },
  tigshop_media_execute: { name: 'tigshop_media_execute', description: '执行已预览的素材修改或分组操作；上传必须使用 tigshop_media_upload 的 fileToken 单一路径，删除不可恢复且必须明确确认', inputSchema: objectSchema({ operation: { type: 'string', enum: ['update', 'delete', 'group_create', 'group_update', 'group_delete'] }, items: { type: 'array', minItems: 1, maxItems: 50, items: objectSchema({ id: { type: 'integer', minimum: 1 }, ids: { type: 'array', minItems: 1, items: { type: 'integer', minimum: 1 } }, name: { type: 'string', minLength: 1, maxLength: 200 }, groupId: { type: 'integer', minimum: 0 }, mimeType: { type: 'string' }, permanent: { type: 'boolean' } }) }, id: { type: 'integer', minimum: 1 }, ids: { type: 'array', minItems: 1, maxItems: 50, uniqueItems: true, items: { type: 'integer', minimum: 1 } }, type: { type: 'string', enum: ['image', 'video'] }, name: { type: 'string', minLength: 1, maxLength: 200 }, groupId: { type: 'integer', minimum: 0 }, permanent: { type: 'boolean' }, confirmationToken: { type: 'string', minLength: 1, maxLength: 128 }, previewId: { type: 'string', minLength: 1 }, requestId: { type: 'string', minLength: 1, maxLength: 128 }, confirmation: { type: 'boolean', const: true } }, ['operation', 'previewId', 'requestId', 'confirmation']), annotations: DESTRUCTIVE },
  tigshop_category_prepare: { name: 'tigshop_category_prepare', description: '预览分类树导入，检查重复、父子层级和缺失父级；不产生写入', inputSchema: objectSchema({ operation: { type: 'string', enum: ['import'] }, nodes: { type: 'array', minItems: 1, maxItems: 200, items: categoryNodeSchema() }, requestId: { type: 'string', minLength: 1, maxLength: 128 } }, ['operation', 'nodes', 'requestId']), annotations: READ_ONLY },
  tigshop_category_execute: { name: 'tigshop_category_execute', description: '执行已确认的分类树导入；缺失父级必须先取得用户明确确认并按父级到子级创建', inputSchema: objectSchema({ operation: { type: 'string', enum: ['import'] }, nodes: { type: 'array', minItems: 1, maxItems: 200, items: categoryNodeSchema() }, previewId: { type: 'string', minLength: 1 }, requestId: { type: 'string', minLength: 1, maxLength: 128 }, confirmation: { type: 'boolean', const: true } }, ['operation', 'nodes', 'previewId', 'requestId', 'confirmation']), annotations: WRITE },
  tigshop_product_analysis: { name: 'tigshop_product_analysis', description: '基于 Tigshop 内部商品数据进行销量、库存、价格、品类和推荐分析；只读，不直接修改商品', inputSchema: objectSchema({ operation: { type: 'string', enum: ['overview', 'top_sellers', 'slow_movers', 'inventory_risk', 'price_bands', 'category_performance', 'recommend'] }, startTime: { type: 'string' }, endTime: { type: 'string' }, categoryId: { type: 'integer', minimum: 1 }, keyword: { type: 'string' }, sampleSize: { type: 'integer', minimum: 1, maximum: 100 }, page: { type: 'integer', minimum: 1 } }, ['operation']), annotations: READ_ONLY }
};

const PATHS = {
  capabilities: ['/adminapi/openapi/mcp/v1/capabilities', 'GET'], media: ['/adminapi/openapi/mcp/v1/media/upload', 'POST'], media_query: ['/adminapi/openapi/mcp/v1/media', 'GET'], media_prepare: ['/adminapi/openapi/mcp/v1/media/prepare', 'POST'], media_execute: ['/adminapi/openapi/mcp/v1/media/execute', 'POST'], context: ['/adminapi/openapi/mcp/v1/products/context', 'GET'],
  connection: ['/adminapi/openapi/mcp/v1/connection/status', 'GET'], requirements: ['/adminapi/openapi/mcp/v1/import/requirements', 'GET'], categories: ['/adminapi/openapi/mcp/v1/categories', 'GET'],
  category_prepare: ['/adminapi/openapi/mcp/v1/categories/prepare', 'POST'], category_execute: ['/adminapi/openapi/mcp/v1/categories/execute', 'POST'], analysis: ['/adminapi/openapi/mcp/v1/products/analysis', 'GET'],
  validate: ['/adminapi/openapi/mcp/v1/products/validate', 'POST'], import: ['/adminapi/openapi/mcp/v1/products/import', 'POST'], search: ['/adminapi/openapi/mcp/v1/products', 'GET'],
  detail: ['/adminapi/openapi/mcp/v1/products/{productId}', 'GET'], promotion_links: ['/adminapi/openapi/mcp/v1/products/{productId}/promotion-links', 'GET'], field: ['/adminapi/openapi/mcp/v1/products/{productId}/field', 'POST'],
  status: ['/adminapi/openapi/mcp/v1/products/{productId}/status', 'POST'], recycle: ['/adminapi/openapi/mcp/v1/products/{productId}/recycle', 'POST'], restore: ['/adminapi/openapi/mcp/v1/products/{productId}/restore', 'POST'],
  copy: ['/adminapi/openapi/mcp/v1/products/{productId}/copy', 'POST'], delete: ['/adminapi/openapi/mcp/v1/products/{productId}/delete', 'POST'], batch_preview: ['/adminapi/openapi/mcp/v1/products/batch/preview', 'POST'], batch_execute: ['/adminapi/openapi/mcp/v1/products/batch/execute', 'POST'], update: ['/adminapi/openapi/mcp/v1/products/{productId}/field', 'POST']
};

const ADMIN_PRODUCT_LIST = '/admin/product/list';
const PRODUCT_OPERATIONS = new Set(['create', 'update', 'recycle', 'restore', 'delete', 'batch']);
const QUERY_OPERATIONS = new Set(['search', 'detail', 'promotion_links', 'context']);
const PREPARE_OPERATIONS = new Set(['requirements', 'validate', 'batch_preview']);
const MEDIA_OPERATIONS = new Set(['update', 'delete', 'group_create', 'group_update', 'group_delete']);

function jsonContent(value) { return [{ type: 'text', text: JSON.stringify(value) }]; }
function invalidArgument(field, message, suggestion = '请按工具参数修正后重试') { return { status: 400, errorCode: 'INVALID_ARGUMENT', field, message, retryable: false, suggestion }; }
function positiveInteger(value) { return Number.isInteger(value) && value > 0; }
function nonNegativeNumber(value) { return typeof value === 'number' && Number.isFinite(value) && value >= 0; }
function productIdError(args, field = 'productId') { return positiveInteger(args?.[field]) ? null : invalidArgument(field, `${field} 必须是正整数`, '请先通过商品查询工具取得有效商品 ID'); }
function requireProducts(args) {
  if (!Array.isArray(args.products) || args.products.length < 1) return invalidArgument('products', 'products 至少包含一个商品对象');
  if (args.products.length > 50) return invalidArgument('products', '单次最多提交 50 个商品');
  return args.products.every((item) => item && typeof item === 'object' && !Array.isArray(item))
    ? null : invalidArgument('products', 'products 至少包含一个商品对象');
}
function mediaBytes(file) { return Number.isFinite(file?.size) && file.size >= 0 ? file.size : 0; }

function validateToolArguments(name, args = {}) {
  if (!args || typeof args !== 'object' || Array.isArray(args)) return invalidArgument('arguments', '工具参数必须是 JSON 对象');
  if (name === 'tigshop_product_query') {
    if (!QUERY_OPERATIONS.has(args.operation)) return invalidArgument('operation', 'operation 只能是 search、detail、promotion_links 或 context');
    if (args.operation === 'context') {
      if ((!args.keyword || typeof args.keyword !== 'string' || !args.keyword.trim()) && !(Number.isInteger(args.categoryId) && args.categoryId > 0)) return invalidArgument('keyword/categoryId', 'context 至少提供 keyword 或 categoryId');
      if (args.sampleSize !== undefined && (!Number.isInteger(args.sampleSize) || args.sampleSize < 1 || args.sampleSize > 100)) return invalidArgument('sampleSize', 'sampleSize 必须是 1 到 100 的整数');
      return null;
    }
    if (args.operation !== 'search') return productIdError(args);
    const ranges = ['minPrice', 'maxPrice', 'minStock', 'maxStock', 'minSales', 'maxSales'];
    for (const field of ranges) { if (args[field] !== undefined && (field.includes('Price') ? !nonNegativeNumber(args[field]) : !Number.isInteger(args[field]) || args[field] < 0)) return invalidArgument(field, `${field} 必须是非负${field.includes('Price') ? '数字' : '整数'}`); }
    if (args.minPrice !== undefined && args.maxPrice !== undefined && args.minPrice > args.maxPrice) return invalidArgument('maxPrice', 'maxPrice 不能小于 minPrice');
    if (args.page !== undefined && (!Number.isInteger(args.page) || args.page < 1)) return invalidArgument('page', 'page 必须是大于等于 1 的整数');
    if (args.size !== undefined && (!Number.isInteger(args.size) || args.size < 1 || args.size > 100)) return invalidArgument('size', 'size 必须是 1 到 100 的整数');
  }
  if (name === 'tigshop_media_upload') {
    if (!Array.isArray(args.files) || args.files.length < 1 || args.files.length > 20) return invalidArgument('files', 'files 必须包含 1 到 20 个文件');
    if (args.confirmation !== true) return invalidArgument('confirmation', '上传前必须明确 confirmation: true');
    if (args.groupId !== undefined && (!Number.isInteger(args.groupId) || args.groupId < 0)) return invalidArgument('groupId', 'groupId 必须是非负整数');
    const allowedFileFields = new Set(['fileToken', 'size', 'clientFileId', 'role', 'groupId']);
    for (const file of args.files) {
      if (!file || typeof file !== 'object') return invalidArgument('files', '每个文件必须是对象');
      if (Object.keys(file).some((key) => !allowedFileFields.has(key))) return { ...invalidArgument('fileToken', '媒体上传只允许 fileToken'), errorCode: 'UPLOAD_MODE_REQUIRED', suggestion: '请先调用 multipart media/upload-token；禁止使用 Base64、本地路径或其他上传方式' };
      if (typeof file.fileToken !== 'string' || !file.fileToken.trim()) return { ...invalidArgument('fileToken', '必须先通过 multipart 获取 fileToken'), errorCode: 'UPLOAD_MODE_REQUIRED', suggestion: '请先调用 multipart media/upload-token；禁止使用 Base64、本地路径或其他上传方式' };
      if (file.size !== undefined && (!Number.isInteger(file.size) || file.size < 0)) return invalidArgument('size', 'size 必须是非负整数');
      if (file.groupId !== undefined && (!Number.isInteger(file.groupId) || file.groupId < 0)) return invalidArgument('groupId', 'groupId 必须是非负整数');
    }
  }
  if (name === 'tigshop_media_query') {
    if (!['list', 'detail', 'groups'].includes(args.operation)) return invalidArgument('operation', 'operation 只能是 list、detail 或 groups');
    if (args.operation === 'detail' && !positiveInteger(args.id)) return invalidArgument('id', 'detail 必须提供正整数 id');
    if (args.operation !== 'detail' && args.id !== undefined) return invalidArgument('id', 'list/groups 不支持 id');
    if (args.page !== undefined && (!Number.isInteger(args.page) || args.page < 1)) return invalidArgument('page', 'page 必须是大于等于 1 的整数');
    if (args.pageSize !== undefined && (!Number.isInteger(args.pageSize) || args.pageSize < 1 || args.pageSize > 100)) return invalidArgument('pageSize', 'pageSize 必须是 1 到 100 的整数');
  }
  if (name === 'tigshop_media_prepare' || name === 'tigshop_media_execute') {
    if (!MEDIA_OPERATIONS.has(args.operation)) return invalidArgument('operation', '素材 operation 不受支持');
    const ids = Array.isArray(args.ids) ? args.ids : args.id !== undefined ? [args.id] : [];
    if (['update', 'delete'].includes(args.operation) && (!ids.length || ids.some((id) => !positiveInteger(id)))) return invalidArgument('ids', '素材操作必须提供正整数 id 列表');
    if (args.type !== undefined && !['image', 'video'].includes(args.type)) return invalidArgument('type', '素材 type 只能是 image 或 video');
    if (args.operation === 'update' && (!args.name && args.groupId === undefined)) return invalidArgument('name/groupId', 'update 至少提供 name 或 groupId');
    if (args.operation.startsWith('group_') && args.operation !== 'group_delete' && (!args.name || !args.name.trim())) return invalidArgument('name', '分组操作必须提供非空 name');
    if (name === 'tigshop_media_prepare' && !args.requestId?.trim()) return invalidArgument('requestId', 'prepare 必须提供 requestId');
    if (name === 'tigshop_media_execute' && (!args.previewId?.trim() || !args.requestId?.trim())) return invalidArgument('previewId/requestId', 'execute 必须提供 previewId 和 requestId');
    if (name === 'tigshop_media_execute' && args.confirmation !== true) return invalidArgument('confirmation', '素材写入前必须明确 confirmation: true');
  }
  if (name === 'tigshop_category_prepare' || name === 'tigshop_category_execute') {
    if (args.operation !== 'import') return invalidArgument('operation', '分类 operation 只能是 import');
    if (!Array.isArray(args.nodes) || args.nodes.length < 1 || args.nodes.length > 200) return invalidArgument('nodes', 'nodes 必须包含 1 到 200 个分类节点');
    if (name === 'tigshop_category_prepare' && !args.requestId?.trim()) return invalidArgument('requestId', 'prepare 必须提供 requestId');
    if (name === 'tigshop_category_execute' && (!args.previewId?.trim() || !args.requestId?.trim() || args.confirmation !== true)) return invalidArgument('previewId/requestId/confirmation', 'execute 必须提供 previewId、requestId 和 confirmation: true');
  }
  if (name === 'tigshop_product_analysis') {
    if (!['overview', 'top_sellers', 'slow_movers', 'inventory_risk', 'price_bands', 'category_performance', 'recommend'].includes(args.operation)) return invalidArgument('operation', '分析 operation 不受支持');
    if (args.categoryId !== undefined && !positiveInteger(args.categoryId)) return invalidArgument('categoryId', 'categoryId 必须是正整数');
    if (args.sampleSize !== undefined && (!Number.isInteger(args.sampleSize) || args.sampleSize < 1 || args.sampleSize > 100)) return invalidArgument('sampleSize', 'sampleSize 必须是 1 到 100 的整数');
    if (args.page !== undefined && (!Number.isInteger(args.page) || args.page < 1)) return invalidArgument('page', 'page 必须是大于等于 1 的整数');
  }
  if (name === 'tigshop_product_prepare') {
    if (!PREPARE_OPERATIONS.has(args.operation)) return invalidArgument('operation', 'operation 只能是 requirements、validate 或 batch_preview');
    if (args.operation === 'validate') return requireProducts(args);
    if (args.operation === 'batch_preview') {
      if (!Array.isArray(args.productIds) || args.productIds.length < 1 || args.productIds.length > 50 || new Set(args.productIds).size !== args.productIds.length || args.productIds.some((id) => !positiveInteger(id))) return invalidArgument('productIds', 'productIds 必须是 1 到 50 个互不重复的正整数');
      if (!['up', 'down', 'recycle', 'restore', 'del'].includes(args.batchOperation)) return invalidArgument('batchOperation', 'batchOperation 只能是 up、down、recycle、restore、del');
    }
  }
  if (name === 'tigshop_product_execute') {
    if (!PRODUCT_OPERATIONS.has(args.operation)) return invalidArgument('operation', 'operation 只能是 create、update、recycle、restore、delete 或 batch');
    if (args.operation === 'create') { const error = requireProducts(args); if (error) return error; if (!args.previewId?.trim() || !args.requestId?.trim()) return invalidArgument('previewId/requestId', 'create 必须提供 previewId 和 requestId'); if (args.confirmation !== true) return invalidArgument('confirmation', '写入前必须明确 confirmation: true'); }
    if (['update', 'recycle', 'restore', 'delete'].includes(args.operation)) { const error = productIdError(args); if (error) return error; if (args.confirmation !== true) return invalidArgument('confirmation', '写入前必须明确 confirmation: true'); if (args.operation === 'update' && (!['productName', 'productSn', 'productPrice', 'productStock', 'productSku', 'sortOrder'].includes(args.field) || args.value === undefined || args.value === null)) return invalidArgument('field/value', 'update 必须提供受支持的 field 和非空 value'); }
    if (args.operation === 'batch') { if (!Array.isArray(args.productIds) || args.productIds.length < 1 || args.productIds.length > 50 || new Set(args.productIds).size !== args.productIds.length || args.productIds.some((id) => !positiveInteger(id))) return invalidArgument('productIds', 'productIds 必须是 1 到 50 个互不重复的正整数'); if (!['up', 'down', 'recycle', 'restore', 'del'].includes(args.batchOperation)) return invalidArgument('batchOperation', 'batchOperation 只能是 up、down、recycle、restore、del'); if (!args.previewId?.trim() || args.confirmation !== true) return invalidArgument('previewId/confirmation', 'batch 执行必须提供 previewId 和 confirmation: true'); }
  }
  if (name === 'tigshop_product_status') return productIdError(args) || (typeof args.onSale === 'boolean' ? (args.confirmation === true ? null : invalidArgument('confirmation', '写入前必须明确 confirmation: true')) : invalidArgument('onSale', 'onSale 必须是布尔值'));
  if (name === 'tigshop_product_copy') return productIdError(args) || (args.confirmation === true ? null : invalidArgument('confirmation', '复制前必须明确 confirmation: true'));
  if (name === 'tigshop_category_query' && args.parentId !== undefined && (!Number.isInteger(args.parentId) || args.parentId < 0)) return invalidArgument('parentId', 'parentId 必须是非负整数');
  return null;
}

function redact(value, secret, key = '') { if (/contentbase64|localpath|filepath|file_path/i.test(key)) return '***REDACTED***'; if (typeof value === 'string') { const withoutSecret = value.split(secret).join('***REDACTED***').replace(/(^|[^A-Za-z0-9+/])([A-Za-z0-9+/]{8,}={0,2})(?=$|[^A-Za-z0-9+/])/g, (match, prefix, token) => (token.length >= 16 || token.includes('=') ? `${prefix}***REDACTED_BASE64***` : match)); return /(?<![A-Za-z0-9])[A-Za-z]:[\\/]|file:\/\/|(?:^|[\s"'=])\/(?!\/)[^\s"']+|(?:^|[\s"'=])\\\\[^\s"']+/.test(withoutSecret) ? '***REDACTED_PATH***' : withoutSecret; } if (Array.isArray(value)) return value.map((item) => redact(item, secret, key)); if (value && typeof value === 'object') return Object.fromEntries(Object.entries(value).map(([entryKey, item]) => [entryKey, redact(item, secret, entryKey)])); return value; }
function sanitizeError(value, status, secret) { const body = value && typeof value === 'object' ? redact(value, secret) : {}; const retryable = typeof body.retryable === 'boolean' ? body.retryable : status === 0 || status >= 500; return { ...body, status, errorCode: body.errorCode || body.code || 'UPSTREAM_ERROR', message: body.message || 'Tigshop API 请求失败', retryable, suggestion: body.suggestion || (status === 401 ? '请重新生成有效 MCP 密钥并更新连接器配置。' : retryable ? '请稍后重试并检查 Tigshop 服务状态。' : '请根据错误信息修正参数或商品数据后重试。') }; }
function markdownText(value) { return String(value ?? '素材').replaceAll('[', '\\[').replaceAll(']', '\\]'); }
function httpUrl(value) { return typeof value === 'string' && /^https?:\/\//i.test(value); }
function mediaItemText(item, title = '') {
  const name = item?.name || '未命名素材';
  const type = item?.type || '未知';
  const lines = [];
  if (title) lines.push(`${title}：${name}`); else lines.push(`- ${name}`);
  lines.push(`  素材 ID：${item?.id ?? '未返回'}，类型：${type}，分组：${item?.groupId ?? '未分组'}`);
  lines.push(`  地址：${item?.url || '未返回'}`);
  if (type.toLowerCase() === 'image' && httpUrl(item?.url)) lines.push(`  ![${markdownText(name)}](${item.url})`);
  if (type.toLowerCase() === 'video' && httpUrl(item?.coverUrl)) lines.push(`  视频封面：![${markdownText(name)}](${item.coverUrl})`);
  return lines.join('\n');
}
function summarize(name, payload, root, args) {
  const total = Number(payload?.total ?? payload?.items?.length ?? 0);
  if (name === 'tigshop_media_upload') return `${payload?.summary || `本次上传完成：共 ${total} 个文件，成功 ${payload?.successCount ?? 0} 个，失败 ${payload?.failedCount ?? 0} 个。`} 当前任务包含图片或视频；创建商品前请确认使用支持图片识别的模型。接下来可以：用这些素材生成商品草稿、查看素材预览或调整素材分组。你想继续哪一项？`;
  if (name === 'tigshop_media_query') {
    if (args.operation === 'groups') {
      const groups = Array.isArray(payload?.groups) ? payload.groups : [];
      if (!groups.length) return '素材分组为空。';
      const lines = [`已返回 ${groups.length} 个素材分组。`];
      groups.slice(0, 20).forEach((group) => lines.push(`- ${group?.name || '未命名分组'}（分组 ID：${group?.id ?? '未返回'}，类型：${group?.type || 'image'}，素材数：${group?.assetCount ?? 0}）`));
      if (groups.length > 20) lines.push('其余分组已省略，可按分组 ID 再查询。');
      return lines.join('\n');
    }
    if (args.operation === 'detail') return payload?.item ? mediaItemText(payload.item, '素材详情') : '没有找到该素材。';
    const items = Array.isArray(payload?.items) ? payload.items : [];
    if (!items.length) return total === 0 ? '素材中心没有找到符合条件的素材。' : '素材查询完成，但当前页没有可展示的素材。';
    const lines = [`素材查询完成：共找到 ${total} 个素材，本次展示 ${Math.min(items.length, 20)} 个。`];
    items.slice(0, 20).forEach((item) => lines.push(mediaItemText(item)));
    if (items.length > 20 || total > 20) lines.push('其余素材已省略，可通过分页或关键词继续查询。');
    return lines.join('\\n');
  }
  if (name === 'tigshop_media_prepare') return `素材操作预览已生成：${payload?.summary || `预计影响 ${payload?.total ?? payload?.items?.length ?? 0} 项`}。请确认后执行。`;
  if (name === 'tigshop_media_execute') return `${payload?.summary || `素材操作完成：成功 ${payload?.successCount ?? 0} 个，失败 ${payload?.failureCount ?? payload?.failedCount ?? 0} 个。`} 接下来可以继续查询素材、调整分组或将已确认素材用于商品草稿。`;
  if (name === 'tigshop_category_prepare') return `分类导入预览已生成：${payload?.summary || `共 ${payload?.total ?? payload?.nodes?.length ?? 0} 个节点`}。缺失父级需用户确认后再创建。`;
  if (name === 'tigshop_category_execute') return payload?.summary || `分类导入完成：成功 ${payload?.successCount ?? 0} 个，失败 ${payload?.failureCount ?? payload?.failedCount ?? 0} 个。`;
  if (name === 'tigshop_capabilities') {
    const advertised = payload?.media?.stageEndpoint;
    const endpoint = typeof advertised === 'string' && advertised.includes('/media/')
      ? advertised : '/adminapi/openapi/mcp/v1/media/upload-token';
    const strict = payload?.media?.strictUploadMode !== false;
    const version = payload?.media?.workflowVersion || '2026-09-04-upload-token-v2';
    return `当前支持商品创建与管理、素材中心管理、分类批量导入和商品分析推荐能力。媒体上传${strict ? '严格' : ''}只允许 fileToken：先用 multipart/form-data 调用 ${root}${endpoint} 获取短时 fileToken，再调用 tigshop_media_upload；禁止 Base64、本地路径和其他上传方式，缺少 fileToken 时立即停止并提示升级客户端。上传流程版本：${version}。图片参与商品创建时请使用支持图片识别的模型。`;
  }
  if (name === 'tigshop_product_analysis') return `${payload?.summary || `商品分析已完成：${payload?.dataScope?.sampleSize ?? payload?.sampleSize ?? 0} 条内部数据参与分析。`} 分析结果仅供建议，请核对依据后再回到商品草稿确认链路；接下来可以查看风险商品、价格区间或生成商品建议。`;
  if (name === 'tigshop_category_query') {
    const categories = Array.isArray(payload?.categories) ? payload.categories : [];
    if (!categories.length) return `没有找到名称含「${args.keyword || ''}」的分类。当前查询是按名称包含匹配，不是语义搜索；可换个词或不带关键词逐级浏览。推荐只是建议，必须确认后才能创建分类。`;
    const lines = [`找到 ${categories.length} 个分类（按名称包含匹配，不是语义搜索）：`];
    categories.slice(0, 20).forEach((category) => lines.push(`- ${category?.path || category?.categoryName || '未命名分类'}（categoryId ${category?.categoryId ?? '未返回'}）${category?.hasChildren ? '，还有下级' : ''}`));
    lines.push('商品必须挂在末级分类；AI 推荐只是建议，分类导入必须先预览并取得用户明确确认。');
    return lines.join('\\n');
  }
  if (name === 'tigshop_product_query' && args.operation === 'search') return total ? `已找到 ${total} 个商品，本页显示 ${payload.items?.length ?? 0} 条。` : '没有找到符合条件的商品，可以放宽筛选条件后重试。';
  if (name === 'tigshop_product_query' && args.operation === 'context') {
    const encoding = payload?.encoding;
    const count = payload?.metrics?.productCount ?? payload?.sampleSize ?? 0;
    const samples = Array.isArray(encoding?.samples) && encoding.samples.length ? `历史编码样本：${encoding.samples.join('、')}。` : '';
    const confidence = Number.isFinite(encoding?.confidence) ? `推断置信度：${Math.round(encoding.confidence * 100)}%。` : '';
    const reason = encoding?.reason ? `依据：${encoding.reason}。` : '';
    if (encoding?.nextCandidate && !encoding?.needsConfirmation) return `已读取 ${count} 个历史商品，用于总结当前商城的商品编码和字段习惯。${samples}推断编码规律：${encoding.pattern}。按历史最大序号顺延的下一个候选编码：${encoding.nextCandidate}。${confidence}${reason}候选编码只是建议，创建商品前必须回显并取得用户明确确认；用户自行提供的编码优先。`;
    return `已读取 ${count} 个历史商品，用于总结当前商城的商品编码和字段习惯。${samples}${confidence}${reason}当前无法可靠自动生成商品编码，请先让用户提供或确认本次编码规则；不能猜测、随机生成或硬编码为 SN 格式。`;
  }
  if (name === 'tigshop_product_prepare' && args.operation === 'validate') {
    if (!payload.valid) {
      const missing = Array.isArray(payload.missingFields) ? payload.missingFields.join('、') : '';
      const hasImages = (args.products || []).some((product) => (Array.isArray(product.images) && product.images.length) || (Array.isArray(product.detailImages) && product.detailImages.length) || (Array.isArray(product.media) && product.media.some((item) => item?.type?.toLowerCase() === 'image')));
      return `校验未通过：${(payload.items || []).filter((item) => !item.valid).length} 个商品存在字段问题。${missing ? `当前阻塞字段：${missing}。` : ''}${hasImages ? '当前商品包含图片，请先使用支持图片识别的模型完成图片分析，或让用户补充文字信息。' : ''}请只补齐阻塞字段后重试。`;
    }
    const warnings = (payload.items || []).flatMap((item) => item.warnings || []);
    const hasImages = (args.products || []).some((product) => (Array.isArray(product.images) && product.images.length) || (Array.isArray(product.detailImages) && product.detailImages.length) || (Array.isArray(product.media) && product.media.some((item) => item?.type?.toLowerCase() === 'image')));
    return `校验通过：共 ${payload.total ?? 0} 个商品。${hasImages ? '当前商品包含图片，请确认当前 AI 使用的是支持图片识别的模型；不支持视觉时请先补充文字信息，不要臆测图片字段。' : ''}${warnings.length ? `请注意：${[...new Set(warnings)].join('；')}。` : ''}请向用户展示最终草稿并取得明确确认后再调用 product_execute。previewId=${payload.previewId ?? '未返回'}`;
  }
  if (name === 'tigshop_product_prepare' && args.operation === 'batch_preview') return `批量操作预览：预计影响 ${(payload.affected || []).length} 个商品，${(payload.blocked || []).length} 个商品会被跳过；请确认后执行。`;
  if (name === 'tigshop_product_execute' && args.operation === 'create') {
    const lines = [`商品创建完成：共 ${payload.total ?? 0} 个，成功 ${payload.successCount ?? 0} 个，失败 ${payload.failureCount ?? 0} 个。`];
    for (const item of payload.items || []) {
      if (!item.success) continue;
      const verification = item.verification;
      const status = verification?.status === 'verified' ? '已核对一致' : verification?.status === 'mismatch' ? `已创建但字段不一致：${(verification.mismatches || []).join('、')}` : '创建已返回成功，但写后核对失败';
      lines.push(`- ${item.productName || `第${item.index}项`}（${item.productSn || '无编码'}，商品ID ${item.productId ?? '未返回'}）：写后核对${status}`);
    }
    if (payload.successCount) {
      lines.push(`后台商品列表：${root}${ADMIN_PRODUCT_LIST}（按商品编码搜索即可定位）`);
      lines.push('我还能帮你：查询商品详情、补充库存、调整上下架、生成推广链接、查询关联素材或分析同类商品表现。你想继续哪一项？');
    }
    return lines.join('\n');
  }
  if (name === 'tigshop_product_execute' && args.operation === 'batch') return `批量操作完成：成功 ${payload.successCount ?? (payload.affected || []).length} 个，失败 ${(payload.failed || []).length} 个。`;
  return null;
}
  function buildRequest(name, args) { let path; let method; let body; if (name === 'tigshop_capabilities') [path, method] = PATHS.capabilities; else if (name === 'tigshop_media_upload') { [path, method] = PATHS.media; body = { files: args.files, groupId: args.groupId, confirmation: true }; } else if (name === 'tigshop_media_query') { [path, method] = PATHS.media_query; } else if (name === 'tigshop_media_prepare') { [path, method] = PATHS.media_prepare; body = { ...args }; } else if (name === 'tigshop_media_execute') { [path, method] = PATHS.media_execute; body = { ...args }; } else if (name === 'tigshop_category_prepare') { [path, method] = PATHS.category_prepare; body = { ...args }; } else if (name === 'tigshop_category_execute') { [path, method] = PATHS.category_execute; body = { ...args }; } else if (name === 'tigshop_product_analysis') { [path, method] = PATHS.analysis; } else if (name === 'tigshop_connection_status') [path, method] = PATHS.connection; else if (name === 'tigshop_category_query') [path, method] = PATHS.categories; else if (name === 'tigshop_product_status') { [path, method] = PATHS.status; path = path.replace('{productId}', encodeURIComponent(args.productId)); body = { onSale: args.onSale }; } else if (name === 'tigshop_product_copy') { [path, method] = PATHS.copy; path = path.replace('{productId}', encodeURIComponent(args.productId)); body = {}; } else if (name === 'tigshop_product_query') { [path, method] = PATHS[args.operation === 'context' ? 'context' : args.operation]; if (!['search', 'context'].includes(args.operation)) path = path.replace('{productId}', encodeURIComponent(args.productId)); else body = undefined; } else if (name === 'tigshop_product_prepare') { [path, method] = PATHS[args.operation === 'requirements' ? 'requirements' : args.operation === 'validate' ? 'validate' : 'batch_preview']; if (args.operation === 'validate') body = { products: args.products }; if (args.operation === 'batch_preview') body = { operation: args.batchOperation, productIds: args.productIds }; } else { const op = args.operation; if (op === 'create') { [path, method] = PATHS.import; body = { products: args.products, previewId: args.previewId, confirmation: true }; } else if (op === 'batch') { [path, method] = PATHS.batch_execute; body = { operation: args.batchOperation, productIds: args.productIds, previewId: args.previewId, confirmation: true }; } else { [path, method] = PATHS[op]; path = path.replace('{productId}', encodeURIComponent(args.productId)); body = op === 'update' ? { field: args.field, value: args.value } : op === 'delete' ? { confirmation: true } : {}; } } return { path, method, body }; }

export function createConnector({ baseUrl, apiKey, fetchImpl = globalThis.fetch } = {}) { if (!baseUrl) throw new Error('TIGSHOP_BASE_URL is required'); if (!apiKey) throw new Error('TIGSHOP_API_KEY is required'); if (typeof fetchImpl !== 'function') throw new Error('fetch is unavailable'); const root = baseUrl.replace(/\/+$/, ''); const REQUEST_TIMEOUT_MS = 90000;
  async function requestOnce(name, args) { const request = buildRequest(name, args); const headers = { Accept: 'application/json', 'X-MCP-Api-Key': apiKey }; const options = { method: request.method, headers }; if (request.method === 'POST') { headers['Content-Type'] = 'application/json'; options.body = JSON.stringify(request.body || {}); } if (request.method === 'GET' && ((name === 'tigshop_product_query' && ['search', 'context'].includes(args.operation)) || name === 'tigshop_category_query' || name === 'tigshop_media_query' || name === 'tigshop_product_analysis')) { const queryArgs = name === 'tigshop_product_analysis' ? args : Object.fromEntries(Object.entries(args).filter(([key]) => key !== 'operation')); const query = new URLSearchParams(Object.entries(queryArgs).filter(([, value]) => value !== undefined && value !== null && value !== '').map(([key, value]) => [key, Array.isArray(value) ? value.join(',') : String(value)])); if (query.toString()) request.path += `?${query}`; } if (args.requestId) headers['Idempotency-Key'] = args.requestId; let response; let timeoutId; try { const timeout = new Promise((_, reject) => { timeoutId = setTimeout(() => reject(Object.assign(new Error('request timeout'), { code: 'ETIMEDOUT' })), REQUEST_TIMEOUT_MS); timeoutId.unref?.(); }); response = await Promise.race([fetchImpl(`${root}${request.path}`, options), timeout]); } catch (error) { const timedOut = error?.code === 'ETIMEDOUT'; return { isError: true, structuredContent: { status: 0, errorCode: timedOut ? 'UPLOAD_TIMEOUT' : 'NETWORK_ERROR', message: timedOut ? '素材上传超时，已停止继续尝试' : '无法连接 Tigshop', retryable: !timedOut, suggestion: timedOut ? '请检查文件大小和存储服务后重新获取 fileToken，再重试一次。' : '请检查网络和 TIGSHOP_BASE_URL 配置后重试。' }, content: [] }; } finally { if (timeoutId) clearTimeout(timeoutId); } let payload; try { payload = await response.json(); } catch { payload = {}; } if (!response.ok) { const failure = sanitizeError(payload, response.status, apiKey); return { isError: true, structuredContent: failure, content: jsonContent(failure) }; } const safePayload = redact(payload, apiKey); return { structuredContent: safePayload, content: jsonContent(safePayload) }; }
  async function callTool(name, args = {}) { if (!TOOL_DEFINITIONS[name]) { const failure = { status: 400, errorCode: 'UNKNOWN_TOOL', message: `未知工具: ${name}`, retryable: false }; return { isError: true, structuredContent: failure, content: jsonContent(failure) }; } const argumentError = validateToolArguments(name, args); if (argumentError) return { isError: true, structuredContent: argumentError, content: jsonContent(argumentError) };
    if (name === 'tigshop_media_upload') {
      const cap = await requestOnce('tigshop_capabilities', {}); if (cap.isError) return cap; const maxBatchBytes = Number(cap.structuredContent?.media?.maxBatchBytes) > 0 ? Number(cap.structuredContent.media.maxBatchBytes) : 40 * 1024 * 1024; const batches = []; let current = []; let bytes = 0; let localTooLarge = 0; for (const file of args.files) { const size = mediaBytes(file); if (size > maxBatchBytes) { localTooLarge += 1; continue; } if (current.length && bytes + size > maxBatchBytes) { batches.push(current); current = []; bytes = 0; } current.push(file); bytes += size; } if (current.length) batches.push(current); const merged = { status: 'success', total: args.files.length, successCount: 0, failedCount: localTooLarge, assets: [], failureSummary: localTooLarge ? { too_large: localTooLarge } : {} }; let aborted = false; for (let index = 0; index < batches.length; index += 1) { const files = batches[index]; if (aborted) { merged.failedCount += files.length; merged.failureSummary.upload_aborted = (merged.failureSummary.upload_aborted || 0) + files.length; continue; } const result = await requestOnce(name, { files, groupId: args.groupId, confirmation: true }); const payload = result.structuredContent || {}; const fatalTransport = result.isError && (payload.status === 401 || payload.status === 0 || payload.errorCode === 'NETWORK_ERROR' || Number(payload.status) >= 500); const reportedFailed = Number(payload.failedCount); merged.successCount += Number(payload.successCount || 0); merged.failedCount += fatalTransport ? files.length : Number.isFinite(reportedFailed) && reportedFailed >= 0 ? reportedFailed : (result.isError ? files.length : 0); merged.assets.push(...(Array.isArray(payload.assets) ? payload.assets : [])); for (const [key, value] of Object.entries(payload.failureSummary || {})) merged.failureSummary[key] = (merged.failureSummary[key] || 0) + Number(value || 0); if (fatalTransport) aborted = true; } merged.status = merged.failedCount === 0 ? 'success' : merged.successCount === 0 ? 'failed' : 'partial'; merged.summary = `本次上传完成：共 ${merged.total} 个文件，成功 ${merged.successCount} 个，失败 ${merged.failedCount} 个。当前任务包含图片或视频；创建商品前请确认使用支持图片识别的模型。`; return { ...(merged.status === 'failed' ? { isError: true } : {}), structuredContent: merged, content: [{ type: 'text', text: merged.summary }] };
    }
    const result = await requestOnce(name, args); if (result.isError) return result; const summary = summarize(name, result.structuredContent, root, args); return { ...result, content: summary ? [{ type: 'text', text: summary }] : result.content }; }
  async function handle(request) { if (!request || request.jsonrpc !== '2.0') return { jsonrpc: '2.0', id: request?.id ?? null, error: { code: -32600, message: 'Invalid Request' } }; const id = request.id ?? null; if (request.method === 'initialize') return { jsonrpc: '2.0', id, result: { protocolVersion: DEFAULT_PROTOCOL_VERSION, capabilities: { tools: {} }, serverInfo: { name: 'tigshop-mcp', version: '1.1.0' }, instructions: GLOBAL_INSTRUCTIONS } }; if (request.method === 'notifications/initialized') return null; if (request.method === 'tools/list') return { jsonrpc: '2.0', id, result: { tools: Object.values(TOOL_DEFINITIONS) } }; if (request.method === 'tools/call') return { jsonrpc: '2.0', id, result: await callTool(request.params?.name, request.params?.arguments || {}) }; return { jsonrpc: '2.0', id, error: { code: -32601, message: `Method not found: ${request.method}` } }; }
  return { handle, callTool };
}
