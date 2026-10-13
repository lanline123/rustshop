//! 品牌域服务：品牌 CRUD、字段更新、审核流、首字母维护。
//! 对应原 Java `BrandServiceImpl`（`com.tigshop.service.product.impl`）。
//!
//! 迁移要点（逐条对照 Java 源）：
//! - `checkBrand`：同店铺内品牌名唯一，更新时排除自身
//! - `create` / `update`：店铺账号（`shop_id > 0`）创建后进入「待审核」，
//!   平台账号（`shop_id = 0`）创建直接「审核通过」
//! - `getFirstWord`：品牌名缺省时自动推导首字母
//!   （ASCII 字母/数字直取；中文取拼音首字母大写，无拼音库时回退 `#`）
//! - `audit`：仅 允许 1 通过 / 2 拒绝；拒绝必须填写理由
//! - `auditList`：与 `list` 共用查询，`isAudit=1` 时只看店铺品牌
//!   （`shop_id <> 0`）
//! - `updateField`：白名单校验（`brand_name`/`first_word`/`brand_is_hot`/
//!   `is_show`/`sort_order`），店铺品牌改动后重新进入待审核
//!
//! 未迁移（与 Java 行为有差距，已列入 `uncertain_points`）：
//! - 品牌列表按**当前登录管理员**的 `shop_id` 强制过滤（Java
//!   `HeaderUtils.getShopId()`），Rust 侧由 handler 显式传入，见
//!   `slice005::BrandListQuery#shop_id` 的调用方式
//! - `updateFirstWord` 的管理操作日志（`adminLogService`，日志域未迁移）

use std::sync::Arc;

use tigshop_core::entities::Brand;
use tigshop_core::models::slice005::{
    BrandAuditDTO, BrandDTO, BrandListQuery, BrandSearchItem, BrandSearchVO, BrandShopVO,
    BrandStatus, BrandVO,
};
use tigshop_core::types::{PageQuery, PageResult};
use tigshop_core::{Error, Result};
use tigshop_infrastructure::db::Repositories;

/// 品牌名长度上限，对应 Java `@Size(min = 1, max = 30)` 与
/// `BrandConstants.BRAND_LENGTH_ERROR`（“品牌名称长度为1-30位”）。
pub const BRAND_NAME_MAX_LEN: usize = 30;

/// `updateField` 允许更新的列白名单，对应 Java
/// `CheckFieldConstants.BRAND_FIELDS`。
pub const BRAND_FIELDS: [&str; 5] = [
    "brand_name",
    "first_word",
    "brand_is_hot",
    "is_show",
    "sort_order",
];

/// 审核状态码，对应 Java `CheckStatus`（与 `BrandStatusEnum` 数值一致）。
pub const CHECK_STATUS_PENDING: i32 = BrandStatus::Pending as i32;
pub const CHECK_STATUS_APPROVED: i32 = BrandStatus::Approved as i32;
pub const CHECK_STATUS_REJECTED: i32 = BrandStatus::Rejected as i32;

pub struct Slice005Service {
    repos: Arc<Repositories>,
}

impl Slice005Service {
    pub fn new(repos: Arc<Repositories>) -> Self {
        Self { repos }
    }

    // ————————————————————— 查询 —————————————————————

    /// 品牌分页列表。
    ///
    /// 对应 Java `BrandServiceImpl#list`：
    /// - `shop_id = Some(0)` 且请求带 `shopId` → 按请求店铺过滤（平台管理员）
    /// - `shop_id = Some(n>0)` → 强制只看本店（店铺/门店/收银账号）
    /// - `shop_id = None` → 不做店铺过滤（语义与 Java `getShopId()==null` 一致）
    /// - `status`：`-1` / `None` 表示不过滤
    /// - `is_audit = 1`（审核列表）：强制 `shop_id <> 0`
    pub async fn list(&self, q: &BrandListQuery) -> Result<PageResult<BrandVO>> {
        let page = PageQuery {
            page: q.page.unwrap_or(1),
            // Java `BasePage.getSize()` 默认 15，上限 100
            page_size: q.page_size.unwrap_or(15).min(100),
        };

        let (total, rows) = self
            .repos
            .slice005
            .paginate(
                q.keyword.as_deref(),
                q.is_show,
                q.brand_is_hot,
                q.first_word.as_deref(),
                q.status.filter(|s| *s > -1),
                q.shop_id,
                q.is_audit == Some(1),
                q.sort_field.as_deref(),
                q.sort_order.as_deref(),
                &page,
            )
            .await?;

        // 批量取店铺名（Java `shopMapper.selectBatchIds`），缺失时回退空对象
        let shop_ids: Vec<i32> = rows
            .iter()
            .map(|b| b.shop_id)
            .filter(|id| *id > 0)
            .collect();
        let mut shops = std::collections::HashMap::new();
        if !shop_ids.is_empty() {
            for s in self.repos.slice005.find_shops(&shop_ids).await? {
                shops.insert(s.shop_id, s.shop_title.clone().unwrap_or_default());
            }
        }

        let list: Vec<BrandVO> = rows
            .iter()
            .map(|b| {
                let title = shops.get(&b.shop_id).cloned().unwrap_or_default();
                to_vo(b, title)
            })
            .collect();
        Ok(PageResult::new(total, list, &page))
    }

    /// 品牌详情（返回与 Java `BrandDTO` 同构的对象）。
    pub async fn detail(&self, brand_id: i32) -> Result<BrandDTO> {
        let b = self
            .repos
            .slice005
            .find_by_id(brand_id)
            .await?
            .ok_or_else(|| Error::NotFound("品牌不存在".into()))?;
        Ok(to_dto(&b))
    }

    /// 品牌搜索（商品选择器的下拉数据）。
    ///
    /// 对应 Java `BrandServiceImpl#search`：
    /// - 品牌项只取 `brand_id` / `brand_name` / `first_word`
    /// - 固定条件 `status = 1（审核通过）AND is_show = 1`
    /// - 另返回去重的 `first_word` 列表
    pub async fn search(&self) -> Result<BrandSearchVO> {
        let brands = self.repos.slice005.find_searchable().await?;
        let first_words = self.repos.slice005.distinct_first_words().await?;
        Ok(BrandSearchVO {
            brand_list: brands
                .iter()
                .map(|b| BrandSearchItem {
                    brand_id: b.brand_id,
                    brand_name: b.brand_name.clone(),
                    first_word: b.first_word.clone(),
                })
                .collect(),
            first_word_list: first_words,
        })
    }

    /// 待审核数量（`status = 0`）。
    pub async fn audit_wait_num(&self) -> Result<i64> {
        self.repos.slice005.count_by_status(CHECK_STATUS_PENDING).await
    }

    // ————————————————————— 写操作 —————————————————————

    /// 创建品牌，返回 `brand_id`。
    ///
    /// 对应 Java `BrandServiceImpl#create`：
    /// - 名称必填且 1-30 字
    /// - 同店铺内名称唯一
    /// - 平台账号（`shop_id = 0`）→ 直接审核通过；店铺账号 → 待审核
    pub async fn create(&self, dto: &BrandDTO, shop_id: i32) -> Result<i32> {
        let name = validate_name(dto.brand_name.as_deref())?;
        self.check_name_unique(&name, shop_id, None).await?;

        let brand = Brand {
            brand_name: name.clone(),
            brand_en_name: dto.brand_en_name.clone(),
            brand_logo: dto.brand_logo.clone(),
            brand_desc: dto.brand_desc.clone(),
            site_url: dto.site_url.clone().unwrap_or_default(),
            sort_order: dto.sort_order.unwrap_or(0),
            is_show: dto.is_show.unwrap_or(0),
            brand_type: dto.brand_type.clone(),
            brand_is_hot: dto.brand_is_hot.unwrap_or(0),
            first_word: Some(first_word_or_auto(dto.first_word.as_deref(), &name)),
            is_store_brand: dto.is_store_brand.unwrap_or(0),
            check_status: dto.check_status.unwrap_or(0),
            shop_id,
            status: if shop_id == 0 {
                CHECK_STATUS_APPROVED
            } else {
                CHECK_STATUS_PENDING
            },
            reject_remark: dto.reject_remark.clone(),
        };
        let id = self.repos.slice005.insert(&brand).await?;
        tracing::info!("创建品牌成功: id={id}, name={name}, shop_id={shop_id}");
        Ok(id)
    }

    /// 更新品牌。
    ///
    /// 对应 Java `BrandServiceImpl#update`：同名校验后整行更新；
    /// 店铺账号修改后重新进入「待审核」。
    pub async fn update(&self, dto: &BrandDTO, shop_id: i32) -> Result<()> {
        let brand_id = dto
            .brand_id
            .ok_or_else(|| Error::Validation("品牌ID不能为空".into()))?;
        let name = validate_name(dto.brand_name.as_deref())?;
        self.check_name_unique(&name, shop_id, Some(brand_id)).await?;

        // 必须已存在
        let existing = self
            .repos
            .slice005
            .find_by_id(brand_id)
            .await?
            .ok_or_else(|| Error::NotFound("品牌不存在".into()))?;

        let status = if shop_id > 0 {
            // 店铺账号修改后等待管理员重新审核
            CHECK_STATUS_PENDING
        } else {
            // 平台账号保留原审核状态（Java updateById 不会覆盖未设置的列，
            // 而 BeanUtils.copyProperties 会把 DTO 的 null status 拷成 null，
            // MyBatis Plus 忽略 null 字段 —— 等价于保留原值）
            existing.status
        };

        let brand = Brand {
            brand_id,
            brand_name: name,
            brand_en_name: dto.brand_en_name.clone(),
            brand_logo: dto.brand_logo.clone(),
            brand_desc: dto.brand_desc.clone(),
            site_url: dto.site_url.clone().unwrap_or_default(),
            sort_order: dto.sort_order.unwrap_or(existing.sort_order),
            is_show: dto.is_show.unwrap_or(existing.is_show),
            brand_type: dto.brand_type.clone(),
            brand_is_hot: dto.brand_is_hot.unwrap_or(existing.brand_is_hot),
            first_word: Some(first_word_or_auto(dto.first_word.as_deref(), &dto.brand_name.clone().unwrap_or_default())),
            is_store_brand: dto.is_store_brand.unwrap_or(existing.is_store_brand),
            check_status: existing.check_status,
            shop_id: existing.shop_id,
            status,
            reject_remark: dto.reject_remark.clone(),
        };
        self.repos.slice005.update(&brand).await?;
        tracing::info!("更新品牌成功: id={brand_id}, shop_id={shop_id}");
        Ok(())
    }

    /// 删除品牌。
    ///
    /// 对应 Java `BrandServiceImpl#del`（`BaseServiceImpl#del` →
    /// `removeById`）：物理 DELETE（`brand` 表无删除标记列）。
    pub async fn delete(&self, brand_id: i32) -> Result<()> {
        let deleted = self.repos.slice005.delete(brand_id).await?;
        if !deleted {
            return Err(Error::NotFound("品牌不存在".into()));
        }
        tracing::info!("删除品牌: id={brand_id}");
        Ok(())
    }

    /// 批量操作。
    ///
    /// 对应 Java `BaseServiceImpl#batch`：仅支持 `type = "del"`，
    /// 其余类型 Java 直接返回 `false`（这里报参数错误）。
    pub async fn batch(&self, ids: &[i32], op_type: Option<&str>) -> Result<u64> {
        if !op_type.is_some_and(|t| t == "del") {
            return Err(Error::Validation("不支持的操作类型".into()));
        }
        if ids.is_empty() {
            return Err(Error::Validation("未选中任何条目".into()));
        }
        let n = self.repos.slice005.delete_batch(ids).await?;
        tracing::info!("批量删除品牌: {n} 条");
        Ok(n)
    }

    /// 更新单个字段。
    ///
    /// 对应 Java `BrandServiceImpl#updateField`：
    /// - 字段必须在 `BRAND_FIELDS` 白名单内
    /// - `brand_name` 需在同店铺内唯一
    /// - 店铺品牌（`shop_id > 0`）改动后进入待审核
    pub async fn update_field(&self, brand_id: i32, field: &str, val: &str) -> Result<bool> {
        let field = normalize_field(field)?;
        let brand = self
            .repos
            .slice005
            .find_by_id(brand_id)
            .await?
            .ok_or_else(|| Error::NotFound("品牌不存在".into()))?;

        if field == "brand_name" {
            let name = validate_name(Some(val))?;
            self.check_name_unique(&name, brand.shop_id, Some(brand_id))
                .await?;
        }

        let updated = self
            .repos
            .slice005
            .update_field(brand_id, field, val)
            .await?;
        if updated && brand.shop_id > 0 {
            self.repos
                .slice005
                .set_status(brand_id, CHECK_STATUS_PENDING)
                .await?;
        }
        Ok(updated)
    }

    /// 批量重算首字母。
    ///
    /// 对应 Java `BrandServiceImpl#updateFirstWord`：遍历全部品牌，
    /// 首字母取品牌名推导值（推导不出则为 `#`）。
    /// 返回更新条数（Java 不返回值）。
    pub async fn update_first_word(&self) -> Result<u64> {
        let brands = self.repos.slice005.find_all().await?;
        let mut n = 0u64;
        for b in &brands {
            let fw = first_pinyin_or_hash(&b.brand_name);
            n += self
                .repos
                .slice005
                .update_first_word(b.brand_id, &fw)
                .await?;
        }
        tracing::info!("批量更新品牌首字母: {n} 条");
        Ok(n)
    }

    /// 品牌审核。
    ///
    /// 对应 Java `BrandServiceImpl#audit`：
    /// - 状态只允许 1（通过）/ 2（拒绝）
    /// - 拒绝必须填写理由
    /// - 平台/店铺品牌（`shop_id >= 0`）都走该入口（Java 按当前登录上下文），
    ///   拒绝理由写入 `reject_remark`
    pub async fn audit(&self, dto: &BrandAuditDTO) -> Result<()> {
        let status = dto.status;
        if status != CHECK_STATUS_APPROVED && status != CHECK_STATUS_REJECTED {
            return Err(Error::biz(500, "审核状态错误"));
        }
        if status == CHECK_STATUS_REJECTED
            && dto
                .reject_remark
                .as_deref()
                .map(str::trim)
                .is_none_or(str::is_empty)
        {
            return Err(Error::biz(500, "请填写拒绝理由"));
        }

        let brand = self
            .repos
            .slice005
            .find_by_id(dto.brand_id)
            .await?
            .ok_or_else(|| Error::NotFound("品牌不存在".into()))?;

        self.repos
            .slice005
            .audit(
                dto.brand_id,
                status,
                dto.reject_remark.as_deref().filter(|s| !s.trim().is_empty()),
            )
            .await?;
        tracing::info!("审核品牌: id={} name={} status={status}", dto.brand_id, brand.brand_name);
        Ok(())
    }

    // ————————————————————— 内部 —————————————————————

    /// 品牌名同店铺唯一性校验。
    ///
    /// 对应 Java `checkBrand`：`brand_name` + `shop_id` 组合唯一，
    /// 更新时排除自身。
    async fn check_name_unique(
        &self,
        name: &str,
        shop_id: i32,
        exclude_id: Option<i32>,
    ) -> Result<()> {
        if self
            .repos
            .slice005
            .exists_same_name(name, shop_id, exclude_id)
            .await?
        {
            return Err(Error::biz(500, "品牌名称已存在"));
        }
        Ok(())
    }
}

/// 品牌名基础校验：非空且 1-30 字（按字符数，对齐 Java `@Size` 语义）。
fn validate_name(raw: Option<&str>) -> Result<String> {
    let name = raw
        .map(str::trim)
        .filter(|s| !s.is_empty())
        .ok_or_else(|| Error::Validation("品牌名称不能为空".into()))?;
    let len = name.chars().count();
    if len == 0 || len > BRAND_NAME_MAX_LEN {
        return Err(Error::Validation("品牌名称长度为1-30位".into()));
    }
    Ok(name.to_string())
}

/// `updateField` 字段白名单校验（对应 Java `getColumnByPropertyName` +
/// `BRAND_FIELDS`；Java 未命中时回退主键静默更新，这里更严格直接报错）。
fn normalize_field(field: &str) -> Result<&'static str> {
    let snake = to_snake(field);
    BRAND_FIELDS
        .iter()
        .copied()
        .find(|f| *f == snake)
        .ok_or_else(|| Error::Validation(format!("不允许更新的字段: {field}")))
}

/// 驼峰 → 下划线（允许前端传驼峰或数据库列名）。
fn to_snake(s: &str) -> String {
    let mut out = String::with_capacity(s.len() + 4);
    for ch in s.chars() {
        if ch.is_ascii_uppercase() {
            out.push('_');
            out.push(ch.to_ascii_lowercase());
        } else {
            out.push(ch);
        }
    }
    out
}

/// 首字母推导（对应 Java `getFirstPinyin`）：
/// - ASCII 字母 → 大写
/// - ASCII 数字 → 原字符
/// - 其他（中文等）→ 拼音首字母大写；无拼音库时回退 `#`
///
/// Java 用 Hutool `PinyinUtil`；Rust 工作区无拼音依赖（禁止改 Cargo.toml），
/// 中文按「无拼音」处理，与 Java 返回 `null` → `#` 的兜底一致。
fn first_pinyin_or_hash(name: &str) -> String {
    match first_pinyin(name) {
        Some(c) => c,
        None => "#".to_string(),
    }
}

/// 单字符首字母推导；ASCII 字母/数字直接命中，其余返回 `None`。
pub fn first_pinyin(name: &str) -> Option<String> {
    let c = name.chars().next()?;
    if c.is_ascii_alphabetic() {
        return Some(c.to_ascii_uppercase().to_string());
    }
    if c.is_ascii_digit() {
        return Some(c.to_string());
    }
    None
}

/// 品牌首字母兜底逻辑（对应 Java `getFirstWord`）：
/// 未填写时按名称推导，推导不出为 `#`。
fn first_word_or_auto(provided: Option<&str>, brand_name: &str) -> String {
    match provided.map(str::trim).filter(|s| !s.is_empty()) {
        Some(w) => w.to_string(),
        None => first_pinyin_or_hash(brand_name),
    }
}

/// 实体 → 管理端 VO（对应 Java `BrandVO` + `BrandStatusEnum.getStatusName`）。
fn to_vo(b: &Brand, shop_title: String) -> BrandVO {
    let status = BrandStatus::try_from(b.status).unwrap_or(BrandStatus::Pending);
    BrandVO {
        base: to_dto(b),
        status_text: status.name().to_string(),
        shop: Some(BrandShopVO {
            shop_id: b.shop_id,
            shop_title,
        }),
    }
}

/// 实体 → DTO（字段与 Java `BrandDTO` 同构，camelCase 序列化）。
fn to_dto(b: &Brand) -> BrandDTO {
    BrandDTO {
        brand_id: Some(b.brand_id),
        brand_name: Some(b.brand_name.clone()),
        brand_en_name: b.brand_en_name.clone(),
        brand_logo: b.brand_logo.clone(),
        brand_desc: b.brand_desc.clone(),
        site_url: Some(b.site_url.clone()),
        sort_order: Some(b.sort_order),
        is_show: Some(b.is_show),
        brand_type: b.brand_type.clone(),
        brand_is_hot: Some(b.brand_is_hot),
        first_word: b.first_word.clone(),
        is_store_brand: Some(b.is_store_brand),
        check_status: Some(b.check_status),
        shop_id: Some(b.shop_id),
        status: Some(b.status),
        reject_remark: b.reject_remark.clone(),
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    // ———————— 名称校验 ————————

    #[test]
    fn name_must_be_non_blank() {
        assert!(validate_name(None).is_err());
        assert!(validate_name(Some("   ")).is_err());
        assert_eq!(
            validate_name(Some(" 苹果 ")).unwrap(),
            "苹果".to_string()
        );
    }

    #[test]
    fn name_length_is_one_to_thirty_chars() {
        let ok = "a".repeat(30);
        assert!(validate_name(Some(&ok)).is_ok());
        let too_long = "a".repeat(31);
        let err = validate_name(Some(&too_long)).unwrap_err();
        assert!(err.to_string().contains("1-30位"));
        // 按字符数而非字节数：15 个中文 = 30 字符，合法
        let cjk = "品".repeat(15);
        assert!(validate_name(Some(&cjk)).is_ok());
        let cjk_long = "品".repeat(16);
        assert!(validate_name(Some(&cjk_long)).is_err());
    }

    // ———————— 状态流转 ————————

    /// 复刻 Java `create` 的初始状态分支。
    fn create_initial_status(shop_id: i32) -> i32 {
        if shop_id == 0 {
            CHECK_STATUS_APPROVED
        } else {
            CHECK_STATUS_PENDING
        }
    }

    #[test]
    fn create_status_depends_on_shop_id() {
        // 平台账号（shop_id=0）直接通过；店铺账号待审核（Java create 分支）
        assert_eq!(create_initial_status(0), CHECK_STATUS_APPROVED);
        assert_eq!(create_initial_status(5), CHECK_STATUS_PENDING);
        assert_eq!(create_initial_status(1), CHECK_STATUS_PENDING);
    }

    /// 复刻 Java `audit` 的状态白名单。
    fn audit_status_ok(status: i32) -> bool {
        status == CHECK_STATUS_APPROVED || status == CHECK_STATUS_REJECTED
    }

    #[test]
    fn audit_only_accepts_approved_or_rejected() {
        assert!(audit_status_ok(1));
        assert!(audit_status_ok(2));
        assert!(!audit_status_ok(0)); // 待审核不是合法审核目标
        assert!(!audit_status_ok(3));
    }

    /// 复刻 Java audit 的拒绝理由判定（trim 后为空即视为未填）。
    fn reject_reason_blank(remark: Option<&str>) -> bool {
        remark.map(str::trim).is_none_or(str::is_empty)
    }

    #[test]
    fn audit_rejected_requires_remark() {
        assert!(reject_reason_blank(None));
        assert!(reject_reason_blank(Some("  ")), "空白拒绝理由必须触发『请填写拒绝理由』");
        assert!(!reject_reason_blank(Some("质量不达标")));
    }

    /// 复刻 Java `update` 的目标状态分支。
    fn update_target_status(shop_id: i32, existing_status: i32) -> i32 {
        if shop_id > 0 {
            CHECK_STATUS_PENDING
        } else {
            existing_status
        }
    }

    #[test]
    fn update_status_keeps_existing_for_platform_and_pends_for_shop() {
        // 店铺账号修改 → 重新待审核；平台账号 → 保留原状态
        assert_eq!(update_target_status(5, CHECK_STATUS_APPROVED), CHECK_STATUS_PENDING);
        assert_eq!(update_target_status(0, CHECK_STATUS_APPROVED), CHECK_STATUS_APPROVED);
        assert_eq!(update_target_status(0, CHECK_STATUS_PENDING), CHECK_STATUS_PENDING);
    }

    // ———————— 首字母 ————————

    #[test]
    fn first_word_ascii_rules_match_java() {
        // Java: A-Z / a-z → 大写；0-9 → 原字符；其他 → 拼音或 '#'
        assert_eq!(first_pinyin("apple"), Some("A".into()));
        assert_eq!(first_pinyin("banana"), Some("B".into()));
        assert_eq!(first_pinyin("1号店"), Some("1".into()));
        // 无拼音库时中文回退 '#'（与 Java PinyinUtil 失败路径一致）
        assert_eq!(first_pinyin("苹果"), None);
        assert_eq!(first_pinyin_or_hash("苹果"), "#");
        assert_eq!(first_pinyin(""), None);
    }

    #[test]
    fn first_word_auto_fills_when_blank() {
        assert_eq!(
            first_word_or_auto(None, "Apple"),
            "A".to_string()
        );
        assert_eq!(first_word_or_auto(Some("X"), "Apple"), "X".to_string());
        assert_eq!(first_word_or_auto(Some(" "), "Apple"), "A".to_string());
        assert_eq!(first_word_or_auto(None, "苹果"), "#".to_string());
    }

    // ———————— updateField 白名单 ————————

    #[test]
    fn update_field_whitelist_matches_java_constant() {
        assert_eq!(
            BRAND_FIELDS,
            ["brand_name", "first_word", "brand_is_hot", "is_show", "sort_order"]
        );
    }

    #[test]
    fn update_field_accepts_camel_and_snake() {
        assert_eq!(normalize_field("brandName").unwrap(), "brand_name");
        assert_eq!(normalize_field("brand_name").unwrap(), "brand_name");
        assert_eq!(normalize_field("isShow").unwrap(), "is_show");
        assert_eq!(normalize_field("sortOrder").unwrap(), "sort_order");
        assert!(normalize_field("1;DROP TABLE brand").is_err());
        assert!(normalize_field("brand_logo").is_err(), "不在白名单内必须拒绝");
    }

    // ———————— VO 映射 ————————

    fn brand(status: i32) -> Brand {
        Brand {
            brand_id: 7,
            brand_name: "Test".into(),
            status,
            shop_id: 3,
            ..Default::default()
        }
    }

    #[test]
    fn vo_maps_status_text_and_shop() {
        let vo = to_vo(&brand(1), "自营店铺".into());
        assert_eq!(vo.status_text, "审核通过");
        assert_eq!(vo.shop.as_ref().unwrap().shop_title, "自营店铺");
        assert_eq!(vo.base.brand_id, Some(7));
    }

    #[test]
    fn vo_tolerates_unknown_status() {
        let vo = to_vo(&brand(99), String::new());
        assert_eq!(vo.status_text, "待审核", "未知状态回退待审核文案");
    }
}
