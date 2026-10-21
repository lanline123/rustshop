// **---------------------------------------------------------------------+
// ** 文件 --
// **---------------------------------------------------------------------+
// ** 版权所有：江西佰商科技有限公司. 官网：https://www.tigshop.com
// **---------------------------------------------------------------------+
// ** 作者：Tigshop团队，yq@tigshop.com
// **---------------------------------------------------------------------+
// ** 提示：Tigshop商城系统为非免费商用系统，未经授权，严禁使用、修改、发布
// **---------------------------------------------------------------------+

package com.tigshop.common.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.elasticsearch._types.mapping.KeywordProperty;
import co.elastic.clients.elasticsearch._types.mapping.LongNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.FloatNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.IntegerNumberProperty;
import co.elastic.clients.elasticsearch._types.mapping.DateProperty;
import co.elastic.clients.elasticsearch._types.mapping.NestedProperty;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch工具类
 *
 * @author Tigshop团队
 * @create 2025年02月12日 15:00
 */
@Slf4j
@Component
public class ElasticsearchUtil {

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    /**
     * 创建索引
     *
     * @param indexName 索引名称
     * @param settings 索引设置
     * @param mapping 索引映射
     * @return 操作结果
     */
    public ApiResponse<Object> createIndex(String indexName, Map<String, Object> settings, Map<String, Object> mapping) {
        try {
            CreateIndexRequest.Builder builder = new CreateIndexRequest.Builder();
            builder.index(indexName);

            // 设置索引配置
            if (settings != null && !settings.isEmpty()) {
                IndexSettings indexSettings = IndexSettings.of(s -> s
                        .numberOfShards(settings.getOrDefault("number_of_shards", "1").toString())
                        .numberOfReplicas(settings.getOrDefault("number_of_replicas", "0").toString())
                        .analysis(a -> a
                                .analyzer("ik_smart", analyzer -> analyzer
                                        .custom(custom -> custom
                                                .tokenizer("ik_smart")
                                        )
                                )
                                .analyzer("ik_max_word", analyzer -> analyzer
                                        .custom(custom -> custom
                                                .tokenizer("ik_max_word")
                                        )
                                )
                        )
                );
                builder.settings(indexSettings);
            }

            // 设置映射
            if (mapping != null && !mapping.isEmpty()) {
                TypeMapping typeMapping = buildTypeMapping(mapping);
                builder.mappings(typeMapping);
            }

            CreateIndexResponse response = elasticsearchClient.indices().create(builder.build());
            
            if (response.acknowledged()) {
                return ApiResponse.success("索引创建成功", null);
            } else {
                return ApiResponse.error("索引创建失败");
            }
        } catch (ElasticsearchException e) {
            log.error("创建索引失败: {}", e.getMessage(), e);
            return ApiResponse.error("创建索引失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("创建索引IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("创建索引IO异常: " + e.getMessage());
        }
    }

    /**
     * 为现有索引增量更新字段映射。
     *
     * <p>该操作只用于增加兼容字段或 multi-field，不改变已有字段类型。</p>
     *
     * @param indexName 索引名称
     * @param mapping 索引字段映射
     * @return 操作结果
     */
    public ApiResponse<Object> updateIndexMapping(String indexName, Map<String, Object> mapping) {
        try {
            PutMappingRequest request = PutMappingRequest.of(p -> p
                    .index(indexName)
                    .properties(buildProperties(mapping))
            );
            PutMappingResponse response = elasticsearchClient.indices().putMapping(request);
            if (response.acknowledged()) {
                return ApiResponse.success("索引映射更新成功", null);
            }
            return ApiResponse.error("索引映射更新失败");
        } catch (ElasticsearchException e) {
            log.error("更新索引映射失败: {}", e.getMessage(), e);
            return ApiResponse.error("更新索引映射失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("更新索引映射IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("更新索引映射IO异常: " + e.getMessage());
        }
    }

    /**
     * 删除索引
     *
     * @param indexName 索引名称
     * @return 操作结果
     */
    public ApiResponse<Object> deleteIndex(String indexName) {
        try {
            DeleteIndexRequest request = DeleteIndexRequest.of(d -> d.index(indexName));
            DeleteIndexResponse response = elasticsearchClient.indices().delete(request);
            
            if (response.acknowledged()) {
                return ApiResponse.success("索引删除成功", null);
            } else {
                return ApiResponse.error("索引删除失败");
            }
        } catch (ElasticsearchException e) {
            log.error("删除索引失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除索引失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("删除索引IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("删除索引IO异常: " + e.getMessage());
        }
    }

    /**
     * 检查索引是否存在
     *
     * @param indexName 索引名称
     * @return 是否存在
     */
    public boolean indexExists(String indexName) {
        try {
            ExistsRequest request = ExistsRequest.of(e -> e.index(indexName));
            return elasticsearchClient.indices().exists(request).value();
        } catch (ElasticsearchException e) {
            log.error("检查索引存在性失败: {}", e.getMessage(), e);
            return false;
        } catch (IOException e) {
            log.error("检查索引存在性IO异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 刷新索引
     *
     * @param indexName 索引名称
     * @return 操作结果
     */
    public ApiResponse<Object> refreshIndex(String indexName) {
        try {
            RefreshRequest request = RefreshRequest.of(r -> r.index(indexName));
            RefreshResponse response = elasticsearchClient.indices().refresh(request);
            
            if (response.shards().failed().intValue() == 0) {
                return ApiResponse.success("索引刷新成功", null);
            } else {
                return ApiResponse.error("索引刷新失败");
            }
        } catch (ElasticsearchException e) {
            log.error("刷新索引失败: {}", e.getMessage(), e);
            return ApiResponse.error("刷新索引失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("刷新索引IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("刷新索引IO异常: " + e.getMessage());
        }
    }

    /**
     * 索引文档
     *
     * @param indexName 索引名称
     * @param id 文档ID
     * @param document 文档内容
     * @return 操作结果
     */
    public ApiResponse<Object> indexDocument(String indexName, String id, Map<String, Object> document) {
        try {
            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index(indexName)
                    .id(id)
                    .document(document)
            );
            
            IndexResponse response = elasticsearchClient.index(request);
            
            if (response.result() == Result.Created || response.result() == Result.Updated) {
                return ApiResponse.success("文档索引成功", response);
            } else {
                return ApiResponse.error("文档索引失败");
            }
        } catch (ElasticsearchException e) {
            log.error("索引文档失败: {}", e.getMessage(), e);
            return ApiResponse.error("索引文档失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("索引文档IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("索引文档IO异常: " + e.getMessage());
        }
    }

    /**
     * 获取文档
     *
     * @param indexName 索引名称
     * @param id 文档ID
     * @return 文档内容
     */
    public ApiResponse<Map<String, Object>> getDocument(String indexName, String id) {
        try {
            GetRequest request = GetRequest.of(g -> g
                    .index(indexName)
                    .id(id)
            );
            
            GetResponse<Map> response = elasticsearchClient.get(request, Map.class);
            
            if (response.found()) {
                Map<String, Object> source = (Map<String, Object>) response.source();
                return ApiResponse.success("获取文档成功", source);
            } else {
                return ApiResponse.error("文档不存在");
            }
        } catch (ElasticsearchException e) {
            log.error("获取文档失败: {}", e.getMessage(), e);
            return ApiResponse.error("获取文档失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("获取文档IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("获取文档IO异常: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     *
     * @param indexName 索引名称
     * @param id 文档ID
     * @return 操作结果
     */
    public ApiResponse<Object> deleteDocument(String indexName, String id) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(indexName)
                    .id(id)
            );
            
            DeleteResponse response = elasticsearchClient.delete(request);
            
            if (response.result() == Result.Deleted || response.result() == Result.NotFound) {
                return ApiResponse.success(
                        response.result() == Result.Deleted ? "文档删除成功" : "文档已不存在",
                        null
                );
            } else {
                return ApiResponse.error("文档删除失败");
            }
        } catch (ElasticsearchException e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            return ApiResponse.error("删除文档失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("删除文档IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("删除文档IO异常: " + e.getMessage());
        }
    }

    /**
     * 批量索引文档
     *
     * @param indexName 索引名称
     * @param documents 文档列表，格式：[{"id": "1", "data": {...}}, ...]
     * @return 操作结果
     */
    public ApiResponse<Object> bulkIndex(String indexName, List<Map<String, Object>> documents) {
        try {
            List<BulkOperation> operations = new ArrayList<>();
            
            for (Map<String, Object> doc : documents) {
                String id = doc.get("id").toString();
                Map<String, Object> data = (Map<String, Object>) doc.get("data");
                
                BulkOperation operation = BulkOperation.of(op -> op
                        .index(idx -> idx
                                .index(indexName)
                                .id(id)
                                .document(data)
                        )
                );
                operations.add(operation);
            }
            
            BulkRequest request = BulkRequest.of(b -> b.operations(operations));
            BulkResponse response = elasticsearchClient.bulk(request);
            
            if (response.errors()) {
                List<String> errors = response.items().stream()
                        .filter(item -> item.error() != null)
                        .map(item -> item.error().reason())
                        .collect(Collectors.toList());
                return ApiResponse.error("批量索引部分失败: " + String.join(", ", errors));
            } else {
                return ApiResponse.success("批量索引成功", response);
            }
        } catch (ElasticsearchException e) {
            log.error("批量索引失败: {}", e.getMessage(), e);
            return ApiResponse.error("批量索引失败: " + e.getMessage());
        } catch (IOException e) {
            log.error("批量索引IO异常: {}", e.getMessage(), e);
            return ApiResponse.error("批量索引IO异常: " + e.getMessage());
        }
    }

    /**
     * 搜索文档
     *
     * @param indexName 索引名称
     * @param query 查询条件
     * @param options 搜索选项
     * @return 搜索结果
     */
    public Map<String, Object> search(String indexName, Map<String, Object> query, Map<String, Object> options) {
        try {
            SearchRequest.Builder builder = new SearchRequest.Builder();
            builder.index(indexName);
            
            // 构建查询
            Query esQuery = buildQuery(query);
            builder.query(esQuery);
            
            // 设置分页
            if (options != null) {
                Integer from = (Integer) options.get("from");
                Integer size = (Integer) options.get("size");
                Boolean trackTotalHits = (Boolean) options.get("track_total_hits");
                
                if (from != null) {
                    builder.from(from);
                }
                if (size != null) {
                    builder.size(size);
                }
                if (trackTotalHits != null && trackTotalHits) {
                    builder.trackTotalHits(t -> t.enabled(true));
                }
                
                // 设置排序
                List<Map<String, Object>> sortList = (List<Map<String, Object>>) options.get("sort");
                if (sortList != null && !sortList.isEmpty()) {
                    List<SortOptions> sortOptions = new ArrayList<>();
                    for (Map<String, Object> sort : sortList) {
                        String field = (String) sort.get("field");
                        String order = (String) sort.get("order");
                        SortOrder sortOrder = "desc".equalsIgnoreCase(order) ? SortOrder.Desc : SortOrder.Asc;
                        sortOptions.add(SortOptions.of(s -> s
                                .field(FieldSort.of(f -> f
                                        .field(field)
                                        .order(sortOrder)
                                ))
                        ));
                    }
                    builder.sort(sortOptions);
                }
                
                // 设置高亮
                Map<String, Object> highlight = (Map<String, Object>) options.get("highlight");
                if (highlight != null) {
                    List<String> fields = (List<String>) highlight.get("fields");
                    if (fields != null && !fields.isEmpty()) {
                        Map<String, HighlightField> highlightFields = new HashMap<>();
                        for (String field : fields) {
                            highlightFields.put(field, HighlightField.of(hf -> hf));
                        }
                        builder.highlight(Highlight.of(h -> h.fields(highlightFields)));
                    }
                }
            }
            
            SearchResponse<Map> response = elasticsearchClient.search(builder.build(), Map.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("hits", response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList()));
            result.put("total", response.hits().total().value());
            result.put("max_score", response.hits().maxScore());
            
            return result;
        } catch (ElasticsearchException e) {
            log.error("搜索失败: {}", e.getMessage(), e);
            return new HashMap<>();
        } catch (IOException e) {
            log.error("搜索IO异常: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 简单关键词搜索
     *
     * @param indexName 索引名称
     * @param keyword 关键词
     * @param fields 搜索字段
     * @param options 搜索选项
     * @return 搜索结果
     */
    public Map<String, Object> simpleSearch(String indexName, String keyword, List<String> fields, Map<String, Object> options) {
        Map<String, Object> query = new HashMap<>();
        Map<String, Object> multiMatch = new HashMap<>();
        multiMatch.put("query", keyword);
        multiMatch.put("fields", fields);
        multiMatch.put("fuzziness", "AUTO");
        query.put("multi_match", multiMatch);
        
        return search(indexName, query, options);
    }

    /**
     * 构建类型映射
     */
    private TypeMapping buildTypeMapping(Map<String, Object> mapping) {
        return TypeMapping.of(tm -> tm.properties(buildProperties(mapping)));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Property> buildProperties(Map<String, Object> mapping) {
        Map<String, Property> properties = new HashMap<>();
        for (Map.Entry<String, Object> entry : mapping.entrySet()) {
            Map<String, Object> fieldConfig = (Map<String, Object>) entry.getValue();
            properties.put(entry.getKey(), buildProperty((String) fieldConfig.get("type"), fieldConfig));
        }
        return properties;
    }

    /**
     * 构建属性
     */
    private Property buildProperty(String type, Map<String, Object> config) {
        return switch (type) {
            case "text" -> {
                String analyzer = (String) config.getOrDefault("analyzer", "ik_smart");
                TextProperty.Builder builder = new TextProperty.Builder().analyzer(analyzer);
                Map<String, Object> fieldConfigs = (Map<String, Object>) config.get("fields");
                if (fieldConfigs != null && !fieldConfigs.isEmpty()) {
                    builder.fields(buildProperties(fieldConfigs));
                }
                yield Property.of(p -> p.text(builder.build()));
            }
            case "keyword" -> Property.of(p -> p.keyword(KeywordProperty.of(k -> k)));
            case "long" -> Property.of(p -> p.long_(LongNumberProperty.of(l -> l)));
            case "float" -> Property.of(p -> p.float_(FloatNumberProperty.of(f -> f)));
            case "integer" -> Property.of(p -> p.integer(IntegerNumberProperty.of(i -> i)));
            case "date" -> Property.of(p -> p.date(DateProperty.of(d -> d)));
            case "nested" -> {
                Map<String, Object> nestedProperties = (Map<String, Object>) config.get("properties");
                Map<String, Property> properties = new HashMap<>();
                if (nestedProperties != null) {
                    for (Map.Entry<String, Object> entry : nestedProperties.entrySet()) {
                        String fieldName = entry.getKey();
                        Map<String, Object> fieldConfig = (Map<String, Object>) entry.getValue();
                        String fieldType = (String) fieldConfig.get("type");
                        properties.put(fieldName, buildProperty(fieldType, fieldConfig));
                    }
                }
                yield Property.of(p -> p.nested(NestedProperty.of(n -> n.properties(properties))));
            }
            default -> Property.of(p -> p.text(TextProperty.of(t -> t.analyzer("ik_smart"))));
        };
    }

    /**
     * 构建查询
     */
    private Query buildQuery(Map<String, Object> queryMap) {
        if (queryMap.containsKey("bool")) {
            return buildBoolQuery((Map<String, Object>) queryMap.get("bool"));
        } else if (queryMap.containsKey("multi_match")) {
            return buildMultiMatchQuery((Map<String, Object>) queryMap.get("multi_match"));
        } else if (queryMap.containsKey("term")) {
            return buildTermQuery((Map<String, Object>) queryMap.get("term"));
        } else if (queryMap.containsKey("terms")) {
            return buildTermsQuery((Map<String, Object>) queryMap.get("terms"));
        } else if (queryMap.containsKey("range")) {
            return buildRangeQuery((Map<String, Object>) queryMap.get("range"));
        } else if (queryMap.containsKey("nested")) {
            return buildNestedQuery((Map<String, Object>) queryMap.get("nested"));
        } else if (queryMap.containsKey("exists")) {
            return buildExistsQuery((Map<String, Object>) queryMap.get("exists"));
        } else if (queryMap.containsKey("match")) {
            return buildMatchQuery((Map<String, Object>) queryMap.get("match"));
        } else if (queryMap.containsKey("wildcard")) {
            return buildWildcardQuery((Map<String, Object>) queryMap.get("wildcard"));
        }
        return Query.of(q -> q.matchAll(m -> m));
    }

    /**
     * 构建布尔查询
     */
    private Query buildBoolQuery(Map<String, Object> boolMap) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();
        
        List<Map<String, Object>> must = (List<Map<String, Object>>) boolMap.get("must");
        if (must != null) {
            for (Map<String, Object> mustQuery : must) {
                boolBuilder.must(buildQuery(mustQuery));
            }
        }

        List<Map<String, Object>> filter = (List<Map<String, Object>>) boolMap.get("filter");
        if (filter != null) {
            for (Map<String, Object> filterQuery : filter) {
                boolBuilder.filter(buildQuery(filterQuery));
            }
        }
        
        List<Map<String, Object>> should = (List<Map<String, Object>>) boolMap.get("should");
        if (should != null) {
            for (Map<String, Object> shouldQuery : should) {
                boolBuilder.should(buildQuery(shouldQuery));
            }
        }
        
        List<Map<String, Object>> mustNot = (List<Map<String, Object>>) boolMap.get("must_not");
        if (mustNot != null) {
            for (Map<String, Object> mustNotQuery : mustNot) {
                boolBuilder.mustNot(buildQuery(mustNotQuery));
            }
        }
        
        // 设置minimum_should_match
        Integer minimumShouldMatch = (Integer) boolMap.get("minimum_should_match");
        if (minimumShouldMatch != null) {
            boolBuilder.minimumShouldMatch(String.valueOf(minimumShouldMatch));
        }
        
        return Query.of(q -> q.bool(boolBuilder.build()));
    }

    /**
     * 构建多字段匹配查询
     */
    private Query buildMultiMatchQuery(Map<String, Object> multiMatchMap) {
        String query = (String) multiMatchMap.get("query");
        List<String> fields = (List<String>) multiMatchMap.get("fields");
        String fuzziness = (String) multiMatchMap.getOrDefault("fuzziness", "AUTO");
        
        MultiMatchQuery.Builder builder = new MultiMatchQuery.Builder()
                .query(query)
                .fields(fields)
                .fuzziness(fuzziness);
        
        return Query.of(q -> q.multiMatch(builder.build()));
    }

    /**
     * 构建精确匹配查询
     */
    private Query buildTermQuery(Map<String, Object> termMap) {
        // 处理两种格式：
        // 1. {"field": "product_status", "value": 1}
        // 2. {"product_status": 1} - 来自嵌套结构
        String field;
        Object value;
        
        if (termMap.containsKey("field") && termMap.containsKey("value")) {
            field = (String) termMap.get("field");
            value = termMap.get("value");
        } else {
            // 处理嵌套格式，取第一个键值对
            Map.Entry<String, Object> entry = termMap.entrySet().iterator().next();
            field = entry.getKey();
            value = entry.getValue();
        }
        
        return Query.of(q -> q.term(TermQuery.of(t -> t
                .field(field)
                .value(createFieldValue(value))
        )));
    }

    /**
     * 构建多值匹配查询
     */
    private Query buildTermsQuery(Map<String, Object> termsMap) {
        // 处理两种格式：
        // 1. {"field": "brand_id", "value": [1,2,3]}
        // 2. {"brand_id": [1,2,3]} - 来自嵌套结构
        String field;
        List<Object> values;
        
        if (termsMap.containsKey("field") && termsMap.containsKey("value")) {
            field = (String) termsMap.get("field");
            values = (List<Object>) termsMap.get("value");
        } else {
            // 处理嵌套格式，取第一个键值对
            Map.Entry<String, Object> entry = termsMap.entrySet().iterator().next();
            field = entry.getKey();
            values = (List<Object>) entry.getValue();
        }
        
        // 添加空值检查
        if (values == null || values.isEmpty()) {
            // 如果值为空，返回匹配所有文档的查询或空查询
            // 这里返回一个不会匹配任何文档的查询比较合理
            return Query.of(q -> q.term(t -> t.field(field).value(FieldValue.of(""))));
        }
        return Query.of(q -> q.terms(TermsQuery.of(t -> t
                .field(field)
                .terms(terms -> terms.value(values.stream()
                        .map(this::createFieldValue)
                        .collect(Collectors.toList())))
        )));
    }

    /**
     * 构建范围查询
     */
    private Query buildRangeQuery(Map<String, Object> rangeMap) {
        // 期望输入: { "price": { "gte": 10, "lte": 100 } }
        Map.Entry<String, Object> entry = rangeMap.entrySet().iterator().next();
        String field = entry.getKey();
        @SuppressWarnings("unchecked")
        Map<String, Object> ops = (Map<String, Object>) entry.getValue();

        return Query.of(q -> q.range(r -> r
            .untyped(u -> {
                u.field(field);
                if (ops.containsKey("gte")) {
                    u.gte(co.elastic.clients.json.JsonData.of(ops.get("gte")));
                }
                if (ops.containsKey("gt")) {
                    u.gt(co.elastic.clients.json.JsonData.of(ops.get("gt")));
                }
                if (ops.containsKey("lte")) {
                    u.lte(co.elastic.clients.json.JsonData.of(ops.get("lte")));
                }
                if (ops.containsKey("lt")) {
                    u.lt(co.elastic.clients.json.JsonData.of(ops.get("lt")));
                }
                return u;
            })
        ));
    }

    /**
     * 构建嵌套查询
     */
    private Query buildNestedQuery(Map<String, Object> nestedMap) {
        String path = (String) nestedMap.get("path");
        Map<String, Object> query = (Map<String, Object>) nestedMap.get("query");
        
        return Query.of(q -> q.nested(NestedQuery.of(n -> n
                .path(path)
                .query(buildQuery(query))
        )));
    }

    private Query buildExistsQuery(Map<String, Object> existsMap) {
        String field = Objects.toString(existsMap.get("field"), "");
        return Query.of(q -> q.exists(e -> e.field(field)));
    }

    private Query buildMatchQuery(Map<String, Object> matchMap) {
        Map.Entry<String, Object> entry = matchMap.entrySet().iterator().next();
        return Query.of(q -> q.match(m -> m
                .field(entry.getKey())
                .query(createFieldValue(entry.getValue()))
        ));
    }

    private Query buildWildcardQuery(Map<String, Object> wildcardMap) {
        String field = Objects.toString(wildcardMap.get("field"), "");
        String value = Objects.toString(wildcardMap.get("value"), "");
        boolean caseInsensitive = Boolean.TRUE.equals(wildcardMap.get("case_insensitive"));
        return Query.of(q -> q.wildcard(w -> w
                .field(field)
                .value(value)
                .caseInsensitive(caseInsensitive)
        ));
    }


    /**
     * 根据值的类型创建FieldValue
     */
    private FieldValue createFieldValue(Object value) {
        return switch (value) {
            case null -> FieldValue.of("");
            case String s -> FieldValue.of(s);
            case Integer i -> FieldValue.of(i.longValue());
            case Long l -> FieldValue.of(value);
            case Double v -> FieldValue.of(value);
            case Float v -> FieldValue.of(v.doubleValue());
            case Boolean b -> FieldValue.of(value);
            default ->
                // 对于其他类型，转换为字符串
                    FieldValue.of(value.toString());
        };

    }
}
