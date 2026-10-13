#!/bin/bash
# ============================================================================
# Rust Migration Environment Setup Script - TigShop Project
# ============================================================================

set -e  # Exit on error

echo "🔧 Setting up Rust development environment for Java to Rust migration..."
echo "================================================"

# ============================================================================
# 1. INSTALL RUST TOOLCHAIN
# ============================================================================
echo ""
echo "Step 1: Installing Rust toolchain"

rustup component add rustfmt
rustup component add clippy
rustup component add rust-src

echo "   ✅ Toolchain configured"

# ============================================================================
# 2. INSTALL DEVELOPMENT TOOLS
# ============================================================================
echo ""
echo "Step 2: Installing cargo utilities"

cargo install cargo-watch          # Auto-rebuild for development
cargo install cargo-expand         # Macro expansion debugging
cargo install cargo-outdated       # Dependency updates check
cargo install cargo-audit          # Security audit
cargo install cargo-edit           # Manage dependencies with CLI
cargo install cargo-deny           # License and dependency policy checks

echo "   ✅ Cargo tools installed"

# ============================================================================
# 3. CREATE PROJECT STRUCTURE
# ============================================================================
echo ""
echo "Step 3: Creating workspace structure"

PROJECT_NAME="tigshop-rs"
mkdir -p "$PROJECT_NAME"
cd "$PROJECT_NAME"

# Initialize workspace
cat > Cargo.toml << 'EOF'
[workspace]
members = [
    "tigshop-core",
    "tigshop-bean", 
    "tigshop-adminapi",
    "tigshop-api",
    "tigshop-common",
    "tigshop-service",
    "tigshop-security",
]
resolver = "2"

[workspace.package]
version = "0.1.0"
edition = "2021"
authors = ["TigShop Team"]

[workspace.dependencies]
# Core libraries
tokio = { version = "1.37", features = ["full"] }
actix-web = "4.5"
actix-rt = "2.9"

# Serialization
serde = { version = "1.0", features = ["derive", "rc"] }
serde_json = "1.0"
serde_yaml = "0.9"

# Database
sqlx = { version = "0.8", features = [
    "runtime-tokio-rustls",
    "mysql",
    "postgres",
    "chrono",
    "uuid",
    "time",
    "json",
] }

# Async
async-trait = "0.1"
futures = "0.3"

# Error handling
thiserror = "1.0"
anyhow = "1.0"

# Logging
tracing = "0.1"
tracing-subscriber = { version = "0.3", features = ["env-filter"] }

# Utilities
uuid = { version = "1.8", features = ["v4", "serde"] }
chrono = { version = "0.4", features = ["serde"] }
url = "2.5"
regex = "1.10"

# HTTP client
reqwest = { version = "0.12", features = ["json", "rustls-tls"] }
http = "1.0"

# Validation
validator = { version = "0.18", features = ["derive"] }

# Cryptography
jsonwebtoken = "9.3"
argon2 = "0.5"
bcrypt = "0.15"
aes-gcm = "0.10"
hex = "0.4"

# Configuration
config = "0.14"
dotenvy = "0.15"

# UUID generation
ulid = "1.0"

# Time operations
time = { version = "0.3", features = ["serde", "formatting", "parsing"] }

# Random
rand = "0.8"

# Collections utils
itertools = "0.12"

# File handling
walkdir = "2.5"
mime_guess = "2.0"

# Email
 lettre = { version = "0.11", features = ["tokio1-rustls-tls"] }

# SMS
# No native Rust SDK for Chinese providers, use reqwest wrappers

# OSS
aws-sdk-s3 = "1.34"
aws-config = "1.3"

# Redis
redis = { version = "0.25", features = ["tokio-comp", "connection-manager"] }

# Cache
moka = { version = "0.12", features = ["future"] }

# Elasticsearch
elastic = "8.0"

# Metrics
prometheus = "0.13"

# Testing
mockall = "0.12"
mockito = "1.3"

[profile.dev]
opt-level = 0
debug = true

[profile.release]
opt-level = 3
lto = true
codegen-units = 1
panic = "abort"
EOF

echo "   ✓ Workspace manifest created"

# Create workspace members
for module in core bean adminapi api common service security; do
    mkdir -p "$PROJECT_NAME/tigshop-$module/src"
    cat > "$PROJECT_NAME/tigshop-$module/Cargo.toml" << EOF
[package]
name = "tigshop-$module"
version.workspace = true
edition.workspace = true
authors.workspace = true

[dependencies]
tigshop-core = { path = "../tigshop-core", optional = true }
EOF
    # Add workspace dependencies by default
    echo "# Add specific dependencies here later" >> "$PROJECT_NAME/tigshop-$module/Cargo.toml"
done

# Create source skeleton
for module in core bean adminapi api common service security; do
    touch "$PROJECT_NAME/tigshop-$module/src/lib.rs"
    touch "$PROJECT_NAME/tigshop-$module/src/main.rs"
done

# Create config directory
mkdir -p "$PROJECT_NAME/config"
cat > "$PROJECT_NAME/config/development.toml" << 'EOF'
[database]
host = "localhost"
port = 3306
username = "root"
password = "password"
database = "tigshop_dev"

[redis]
host = "localhost"
port = 6379
password = ""
db = 0

[payment]
wechat_app_id = ""
wechat_mch_id = ""
wechat_api_key = ""
alipay_app_id = ""
alipay_private_key = ""

[sms]
aliyun_access_key = ""
aliyun_secret_key = ""
tencent_app_id = ""

[email]
smtp_host = "smtp.example.com"
smtp_port = 587
smtp_username = ""
smtp_password = ""

[oss]
aliyun_endpoint = "oss-cn-hangzhou.aliyuncs.com"
aliyun_access_key = ""
aliyun_secret_key = ""
aliyun_bucket = "tigshop-assets"
EOF

cat > "$PROJECT_NAME/config/production.toml" << 'EOF'
[database]
host = "mysql.tigshop.internal"
port = 3306
username = "tigshop_user"
password_env = "DATABASE_PASSWORD"
database = "tigshop_prod"
pool_size = 20

[redis]
host = "redis.tigshop.internal"
port = 6379
password_env = "REDIS_PASSWORD"
db = 0
pool_size = 10

[payment]
wechat_app_id_env = "WECHAT_APP_ID"
wechat_mch_id_env = "WECHAT_MCH_ID"
wechat_api_key_env = "WECHAT_API_KEY"
alipay_app_id_env = "ALIPAY_APP_ID"
alipay_private_key_env = "ALIPAY_PRIVATE_KEY"

[sms]
aliyun_access_key_env = "ALIYUN_ACCESS_KEY"
aliyun_secret_key_env = "ALIYUN_SECRET_KEY"
tencent_app_id_env = "TENCENT_APP_ID"

[email]
smtp_host = "smtp.tigshop.internal"
smtp_port = 587
smtp_username_env = "EMAIL_USERNAME"
smtp_password_env = "EMAIL_PASSWORD"

[oss]
aliyun_endpoint_env = "ALIYUN_ENDPOINT"
aliyun_access_key_env = "ALIYUN_ACCESS_KEY"
aliyun_secret_key_env = "ALIYUN_SECRET_KEY"
aliyun_bucket = "tigshop-prod-assets"
EOF

echo "   ✓ Config templates created"

# Create migrations directory
mkdir -p "$PROJECT_NAME/migrations"
cat > "$PROJECT_NAME/migrations/README.md" << 'EOF'
# Database Migrations

This directory contains SQL migration files for the database schema.

## Naming Convention
YYYYMMDDHHMMSS_description.sql

Example:
- 20250101120000_create_users_table.sql
- 20250101120001_add_indexes.sql

## Using SQLX Migrations
Run migrations with:
```bash
sqlx migrate run
```

Reset database:
```bash
sqlx migrate revert
```
EOF

# Create tests directory
mkdir -p "$PROJECT_NAME/tests/integration"
touch "$PROJECT_NAME/tests/integration/mod.rs"

# Create documentation
cat > "$PROJECT_NAME/README.md" << 'EOF'
# TigShop Rust Implementation

## Overview
Rust rewrite of the TigShop B2B2B e-commerce platform.

## Architecture
- **tigerho-core**: Core utilities and shared types
- **tigshop-bean**: Data models (DTOs, BOs)
- **tigshop-service**: Business logic layer
- **tigshop-adminapi**: Admin API endpoints
- **tigshop-security**: Authentication & Authorization

## Quick Start
```bash
# Install dependencies
cargo install sqlx-cli --features mysql

# Set up database
sqlx migrate run

# Run development server
cargo run --bin tigshop-admin

# Run tests
cargo test
```

## Migration Status
### Phase 1: Infrastructure (Complete)
- [x] Workspace setup
- [x] Configuration management
- [ ] Database migrations

### Phase 2: Core Modules (In Progress)
- [ ] User management service
- [ ] Authentication service  
- [ ] Notification services (SMS/Email)

### Phase 3: Business Logic
- [ ] Order processing
- [ ] Cart management
- [ ] Payment integration

## Contributing
See CONTRIBUTING.md for guidelines.

## License
Proprietary - TigShop Internal Use Only
EOF

echo "   ✓ Project skeleton created"

# ============================================================================
# 4. VERIFY INSTALLATION
# ============================================================================
echo ""
echo "Step 4: Verifying installation"

rustc --version
cargo --version
sqlx --version || echo "sqlx-cli not yet available (install manually if needed)"

echo ""
echo "=============================================="
echo "✅ Rust Migration Environment Ready!"
echo ""
echo "Project Structure:"
echo "- $PROJECT_NAME/"
echo "  ├── Cargo.toml              (Workspace configuration)"
echo "  ├── config/"
echo "  │   ├── development.toml   (Dev environment settings)"
echo "  │   └── production.toml    (Prod configuration)"
echo "  ├── migrations/            (Database changes)"
echo "  ├── tests/                 (Integration tests)"
echo "  └── tigshop-*/             (Workspace modules)"
echo ""
echo "Next Steps:"
echo "1. Configure environment variables (.env file)"
echo "2. Set up MySQL database with migrations"
echo "3. Start with DTO conversion (tigshop-bean module)"
echo "4. Begin incremental migration strategy"
echo ""
echo "Commands:"
echo "  cd tigshop-$PROJECT_NAME"
echo "  cargo watch -x 'check'     # Watch for code changes"
echo "  cargo fmt                  # Format code"
echo "  cargo clippy               # Lint checks"
echo "  cargo build --release      # Production build"
echo ""
echo "Documentation:"
echo "- Rust Book: https://doc.rust-lang.org/book/"
echo "- Actix Web Guide: https://actix.rs/docs/getting-started/"
echo "- SQLX Documentation: https://docs.rs/sqlx/"
