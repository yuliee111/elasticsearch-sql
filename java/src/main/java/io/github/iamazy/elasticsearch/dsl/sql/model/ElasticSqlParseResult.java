package io.github.iamazy.elasticsearch.dsl.sql.model;

import io.github.iamazy.elasticsearch.dsl.cons.CoreConstants;
import io.github.iamazy.elasticsearch.dsl.elastic.HighlightBuilders;
import io.github.iamazy.elasticsearch.dsl.sql.enums.SqlOperation;
import io.github.iamazy.elasticsearch.dsl.sql.exception.ElasticSql2DslException;
import io.github.iamazy.elasticsearch.dsl.utils.StringManager;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author iamazy
 * @date 2019/7/26
 * @descrition
 **/
public class ElasticSqlParseResult {

    private static Logger log = LoggerFactory.getLogger(ElasticSqlParseResult.class);

    private int from = 0;
    private int size = 15;

    private List<String> indices;

    private SqlOperation sqlOperation = SqlOperation.SELECT;
    private transient boolean trackTotalHits = false;


    /**
     * 需要高亮显示的字段
     */
    private Set<String> highlighter = new HashSet<>(0);
    private List<String> routingBy = new ArrayList<>(0);
    private List<String> includeFields = new ArrayList<>(0);
    private List<String> excludeFields = new ArrayList<>(0);
    private transient QueryBuilder whereCondition = QueryBuilders.matchAllQuery();
    private transient CollapseBuilder collapseBuilder;
    private transient List<SortBuilder> orderBy = new ArrayList<>(0);
    private transient List<AggregationBuilder> groupBy = new ArrayList<>(0);
    private transient SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    private transient ReindexRequest reindexRequest;

    private GetMappingsRequest mappingsRequest;
    private GetFieldMappingsRequest fieldMappingsRequest;


    //region Getter and Setter
    public ElasticSqlParseResult setSqlOperation(SqlOperation sqlOperation) {
        this.sqlOperation = sqlOperation;
        return this;
    }

    public SqlOperation getSqlOperation() {
        return sqlOperation;
    }

    public int getFrom() {
        return from;
    }

    public ElasticSqlParseResult setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getSize() {
        return size;
    }

    public ElasticSqlParseResult setSize(int size) {
        this.size = size;
        return this;
    }

    public List<String> getIndices() {
        return indices;
    }

    public ElasticSqlParseResult setIndices(List<String> indices) {
        this.indices = indices;
        return this;
    }

    public boolean trackTotalHits() {
        return trackTotalHits;
    }

    public ElasticSqlParseResult trackTotalHits(boolean trackTotalHits) {
        this.trackTotalHits = trackTotalHits;
        return this;
    }

    public Set<String> getHighlighter() {
        return highlighter;
    }

    public List<String> getRoutingBy() {
        return routingBy;
    }

    public List<String> getIncludeFields() {
        return includeFields;
    }

    public List<String> getExcludeFields() {
        return excludeFields;
    }

    public QueryBuilder getWhereCondition() {
        return whereCondition;
    }

    public ElasticSqlParseResult setWhereCondition(QueryBuilder whereCondition) {
        this.whereCondition = whereCondition;
        return this;
    }

    public ReindexRequest getReindexRequest() {
        return reindexRequest;
    }

    public ElasticSqlParseResult setReindexRequest(ReindexRequest reindexRequest) {
        this.reindexRequest = reindexRequest;
        return this;
    }

    public List<SortBuilder> getOrderBy() {
        return orderBy;
    }

    public List<AggregationBuilder> getGroupBy() {
        return groupBy;
    }

    public void setCollapseBuilder(CollapseBuilder collapseBuilder) {
        this.collapseBuilder = collapseBuilder;
    }

    public CollapseBuilder getCollapseBuilder() {
        return collapseBuilder;
    }

    public GetMappingsRequest getMappingsRequest() {
        return mappingsRequest;
    }

    public ElasticSqlParseResult setMappingsRequest(GetMappingsRequest mappingsRequest) {
        this.mappingsRequest = mappingsRequest;
        return this;
    }

    public GetFieldMappingsRequest getFieldMappingsRequest() {
        return fieldMappingsRequest;
    }

    public ElasticSqlParseResult setFieldMappingsRequest(GetFieldMappingsRequest fieldMappingsRequest) {
        this.fieldMappingsRequest = fieldMappingsRequest;
        return this;
    }

    //endregion

    public DeleteByQueryRequest toDelRequest() {
        DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(toRequest().indices());
        deleteByQueryRequest.setQuery(searchSourceBuilder.query());
        if (CollectionUtils.isNotEmpty(routingBy)) {
            deleteByQueryRequest.setRouting(routingBy.get(0));
        }

        if (size < 0) {
            deleteByQueryRequest.setMaxDocs(15);
        } else {
            deleteByQueryRequest.setMaxDocs(size);
        }
        return deleteByQueryRequest;
    }

    public SearchRequest toRequest() {
        SearchRequest searchRequest = new SearchRequest();
        List<String> indexList = indices.parallelStream().map(StringManager::removeStringSymbol).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(indexList)) {
            searchRequest.indices(indexList.toArray(new String[0]));
        } else {
            throw new ElasticSql2DslException("[syntax error] indices name must be set");
        }
        if (from < 0) {
            log.debug("[from] is gte zero, assign 0 to [from(int)] as default value!!!");
            //这里不会修改from的值
            searchSourceBuilder.from(0);
        } else {
            searchSourceBuilder.from(from);
        }
        if (size < 0) {
            log.debug("[size] is gte zero, assign 15 to [size(int)] as default value!!!");
            searchSourceBuilder.size(15);
        } else {
            searchSourceBuilder.size(size);
        }
        searchSourceBuilder.trackTotalHits(this.trackTotalHits);
        if (CollectionUtils.isNotEmpty(highlighter)) {
            HighlightBuilder highlightBuilder = HighlightBuilders.highlighter(highlighter);
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        searchSourceBuilder.query(whereCondition);

        if (collapseBuilder != null) {
            searchSourceBuilder.collapse(collapseBuilder);
        }
        if (CollectionUtils.isNotEmpty(orderBy)) {
            for (SortBuilder sortBuilder : orderBy) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        searchSourceBuilder.fetchSource(includeFields.toArray(new String[0]), excludeFields.toArray(new String[0]));

        if (CollectionUtils.isNotEmpty(routingBy)) {
            searchRequest.routing(routingBy.toArray(new String[0]));
        }

        if (CollectionUtils.isNotEmpty(groupBy)) {
            for (AggregationBuilder aggItem : groupBy) {
                searchSourceBuilder.aggregation(aggItem);
            }
        }

        return searchRequest.source(searchSourceBuilder);
    }

    public String toDsl(SearchRequest searchRequest) {
        return searchRequest.source().toString();
    }

    public String toPrettyDsl(SearchRequest searchRequest) {
        try {
            Object o = CoreConstants.OBJECT_MAPPER.readValue(toDsl(searchRequest), Object.class);
            return CoreConstants.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        } catch (IOException e) {
            throw new RuntimeException("Elasticsearch Dsl解析出错!!!");
        }
    }

}
