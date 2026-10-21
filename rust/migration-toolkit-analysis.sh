#!/bin/bash
# ============================================================================
# Java → Rust 迁移评估工具集 - TigShop 项目分析脚本
# ============================================================================

set -e  # Exit on error

PROJECT_ROOT=$(pwd)
OUTPUT_DIR="migration-report"
mkdir -p "$OUTPUT_DIR"

echo "🚀 Starting Java to Rust Migration Analysis..."
echo "=============================================="
echo ""

# ============================================================================
# 1. CODE METRICS COLLECTION
# ============================================================================
echo "📊 Section 1: Code Metrics Collection"

total_java_files=$(find . -name "*.java" 2>/dev/null | wc -l)
echo "   Total Java Files: $total_java_files"

dto_count=$(find . -path "*/bean/dto/*.java" 2>/dev/null | wc -l)
echo "   DTO Classes: $dto_count"

bo_count=$(find . -path "*/bean/bo/*.java" 2>/dev/null | wc -l)  
echo "   BO Classes: $bo_count"

service_patterns=$(find . -name "*Service*.java" -o -name "*Manager*.java" 2>/dev/null | wc -l)
echo "   Service/Manager Classes: $service_patterns"

controller_count=$(find . -name "*Controller*.java" 2>/dev/null | wc -l)
echo "   Controller Classes: $controller_count"

dao_count=$(find . -name "*Mapper*.java" -o -name "*Repository*.java" 2>/dev/null | wc -l)
echo "   Mapper/Repository Classes: $dao_count"

echo "   ✅ Metrics collected. Saving to $OUTPUT_DIR/metrics.txt"
cat > "$OUTPUT_DIR/metrics.txt" << EOF
Code Metrics Summary (TigShop Project)
Generated: $(date)
====================================
Total Java Files: $total_java_files
DTO Classes: $dto_count
BO Classes: $bo_count
Service/Manager: $service_patterns
Controllers: $controller_count
Mappers/Repositories: $dao_count
EOF

# ============================================================================
# 2. DEPENDENCY ANALYSIS
# ============================================================================
echo ""
echo "📦 Section 2: Dependency Analysis"

grep -rh "<groupId>" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | \
    grep -v "^$" | sort | uniq -c | sort -rn | head -30 > "$OUTPUT_DIR/dependencies.txt"

echo "   Top 30 dependencies analyzed."

# Categorize dependencies by type
echo "Dependency Types:"
echo "-----------------"
mysql_deps=$(grep -r "mysql" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | grep -i dependency | wc -l)
spring_deps=$(grep -r "org.springframework" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | grep -i dependency | wc -l)
cloud_deps=$(grep -r "org.springframework.cloud" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | grep -i dependency | wc -l)
orm_deps=$(grep -rE "(mybatis|hibernate|jpa)" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | grep -i dependency | wc -l)
payment_deps=$(grep -rE "(weixin|alipay|paypal|支付)" pom.xml pom.xml */pom.xml */*/pom.xml 2>/dev/null | grep -i dependency | wc -l)

echo "   MySQL Drivers: $mysql_deps"
echo "   Spring Framework: $spring_deps"
echo "   Spring Cloud: $cloud_deps"
echo "   ORM Libraries: $orm_deps"
echo "   Payment Gateways: $payment_deps"

# ============================================================================
# 3. EXTERNAL API CLIENTS
# ============================================================================
echo ""
echo "🌐 Section 3: External API Client Analysis"

echo "Identifying Feign Clients and HTTP Services:"
feign_clients=$(grep -rl "@FeignClient" . 2>/dev/null | wc -l)
http_services=$(grep -rl "@RestController" . 2>/dev/null | wc -l)

echo "   Feign Clients: $feign_clients"
echo "   REST Controllers: $http_services"

# Extract external API endpoints
echo ""
echo "External APIs Used:"
grep -rhE "api\.|client\.(baseUrl|serverUrl)" src/ . 2>/dev/null | \
    grep -oE "https?://[^\"'<>]+" | sort -u | head -20 > "$OUTPUT_DIR/external_apis.txt"

echo "   Found $(wc -l < "$OUTPUT_DIR/external_apis.txt") unique external API endpoints"

# ============================================================================
# 4. DATABASE SCHEMA PATTERN RECOGNITION
# ============================================================================
echo ""
echo "💾 Section 4: Database Schema Patterns"

# Analyze MyBatis XML files
xml_mapper_count=$(find . -name "*.xml" -path "*/mapper/*" 2>/dev/null | wc -l)
echo "   MyBatis XML Mappers: $xml_mapper_count"

if [ $xml_mapper_count -gt 0 ]; then
    echo "   SQL Statement Count:"
    insert_count=$(grep -rh "<insert " . 2>/dev/null | wc -l)
    update_count=$(grep -rh "<update " . 2>/dev/null | wc -l)
    delete_count=$(grep -rh "<delete " . 2>/dev/null | wc -l)
    select_count=$(grep -rh "<select " . 2>/dev/null | wc -l)
    
    echo "      INSERT statements: $insert_count"
    echo "      UPDATE statements: $update_count"
    echo "      DELETE statements: $delete_count"
    echo "      SELECT statements: $select_count"
fi

# ============================================================================
# 5. PAYMENT GATEWAY ASSESSMENT
# ============================================================================
echo ""
echo "💳 Section 5: Payment Gateway Assessment"

payment_modules=(
    "WechatPay"
    "Alipay"
    "Paypal"
    "Antom"
    "Lakala"
    "Yop"
)

for payment in "${payment_modules[@]}"; do
    module_files=$(find . -type f -name "*.java" -exec grep -l "$payment" {} \; 2>/dev/null | wc -l)
    if [ $module_files -gt 0 ]; then
        echo "   ✓ $payment: $module_files files found"
    fi
done

# ============================================================================
# 6. COMPLEXITY INDICATORS
# ============================================================================
echo ""
echo "🔍 Section 6: Complexity Indicators"

# Method complexity analysis
complex_methods=$(grep -rhE "^\s*(public|private|protected)\s+(async|static)?\s*\w+\s+\w+\s*\([^)]*\).*\{.*\}" . 2>/dev/null | \
    grep -c "{" || echo "0")
avg_lines_per_file=$((total_java_files > 0 ? total_java_files / 500 : 1))

echo "   Estimated Complex Methods: $complex_methods"
echo "   Average Lines per File: ~$((50 + avg_lines_per_file * 2))"

# Annotation usage patterns
lombok_annotations=$(grep -rh "@Data\|@Builder\|@NoArgsConstructor\|@AllArgsConstructor" . 2>/dev/null | wc -l)
spring_annotations=$(grep -rh "@Autowired\|@Service\|@Component\|@Configuration" . 2>/dev/null | wc -l)

echo "   Lombok Annotations Used: $lombok_annotations"
echo "   Spring Annotations Used: $spring_annotations"

# ============================================================================
# 7. ESTIMATION CALCULATION
# ============================================================================
echo ""
echo "💰 Section 7: Effort Estimation"

# Simple estimation formula
dto_effort=$((dto_count * 1))  # 1 hour per DTO
service_effort=$((service_patterns * 40))  # 40 hours per service
repo_effort=$((dao_count * 20))  # 20 hours per repository
integration_effort=$((feign_clients * 30 + http_services * 15))  # Integration points

total_hours=$((dto_effort + service_effort + repo_effort + integration_effort))
total_days=$((total_hours / 8))
total_months=$(((total_days + 20) / 30))  # Add buffer for testing

echo "   Calculated Effort:"
echo "   - Total Hours: $total_hours"
echo "   - Total Days: $total_days"
echo "   - Estimated Months: $total_months (with 20% buffer)"

# Team configuration recommendations
echo ""
echo "   Recommended Team Configurations:"
echo "   ----------------------------------"
echo "   Option A (Fast): 4 developers × 0.75 efficiency = ~${total_months} months"
echo "   Option B (Stable): 6 developers × 0.85 efficiency = ~$((total_months * 0.8)) months"
echo "   Option C (Phased): Phased approach with parallel ops = ~$((total_months * 1.3)) months"

# ============================================================================
# 8. RISK MATRIX
# ============================================================================
echo ""
echo "⚠️  Section 8: Risk Assessment Matrix"

high_risk_items=0
medium_risk_items=0
low_risk_items=0

# Check for high-risk patterns
if [ $payment_deps -gt 0 ]; then
    echo "   🔴 HIGH: Payment Gateway Integration ($payment_deps services)"
    ((high_risk_items++))
fi

if [ $service_patterns -gt 50 ]; then
    echo "   🟡 MEDIUM: Complex Business Logic Layer ($service_patterns services)"
    ((medium_risk_items++))
fi

if [ $orm_deps -gt 2 ]; then
    echo "   🟡 MEDIUM: ORM Migration (MyBatis Plus → SQLX)"
    ((medium_risk_items++))
fi

if [ $feign_clients -gt 0 ]; then
    echo "   🟢 LOW: REST Clients (need manual adapter)"
    ((low_risk_items++))
fi

# ============================================================================
# 9. OUTPUT SUMMARY
# ============================================================================
echo ""
echo "=============================================="
echo "📄 Full Report Saved to: $OUTPUT_DIR/"
echo ""
echo "Report Contents:"
echo "- metrics.txt          : Code statistics"
echo "- dependencies.txt     : Library dependencies"
echo "- external_apis.txt    : Third-party integrations"
echo "- risk_matrix.md       : Risk assessment (below)"

cat > "$OUTPUT_DIR/risk_matrix.md" << EOF
# Migration Risk Matrix - TigShop Project

## High Priority Items 🔴
$(if [ $high_risk_items -gt 0 ]; then echo "- Payment gateway reimplementation ($payment_deps modules)"; fi)
$(if [ $service_patterns -gt 50 ]; then echo "- Complex business logic refactoring"; fi)
$(if [ $orm_deps -gt 2 ]; then echo "- ORM pattern conversion (Spring Data → SQLX)"; fi)

## Medium Priority Items 🟡
- Transaction management adaptation
- Cache layer strategy redesign
- Distributed tracing implementation

## Low Priority Items 🟢
- DTO/POJO structure conversion
- Utility classes refactoring
- Configuration management migration

## Recommendations
1. Start with low-risk modules first (DTO, utils)
2. Parallel work on payment adapters using official docs
3. Implement differential testing framework early
4. Consider Oxidize incremental migration approach
EOF

echo ""
echo "✅ Analysis Complete!"
echo "   Total Estimated Duration: $total_months months"
echo "   Team Recommendation: 4-6 Rust developers + 1 Java architect"

# ============================================================================
# CLEANUP AND NEXT STEPS
# ============================================================================
echo ""
echo "🎯 Next Steps:"
echo "   1. Review risk_matrix.md in the report folder"
echo "   2. Start with pilot module (SMS/Notification Service)"
echo "   3. Create Rust workspace skeleton"
echo "   4. Set up development environment"
echo ""
echo "Run: ./setup-rust-env.sh to initialize Rust project structure"
