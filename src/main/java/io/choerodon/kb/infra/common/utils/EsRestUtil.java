package io.choerodon.kb.infra.common.utils;

import io.choerodon.kb.api.dao.PageSyncDTO;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.mapper.PageMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class EsRestUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(EsRestUtil.class);
    public static final String ALIAS_PAGE = "knowledge_page";
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private PageMapper pageMapper;

    public Boolean indexExist(String index) {
        GetIndexRequest request;
        Boolean exists = false;
        try {
            request = new GetIndexRequest(index);
            exists = highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            LOGGER.error("elasticsearch indexExist, error:{}", e.getMessage());
        }
        return exists;
    }

    public void createIndex(String index) {
        CreateIndexRequest request = new CreateIndexRequest(index);
        //设置索引的settings，设置默认分词
        request.settings(Settings.builder().put("analysis.analyzer.default.tokenizer", "ik_smart"));
//        request.alias(new Alias(ALIAS_PAGE));
        try {
            CreateIndexResponse createIndexResponse = highLevelClient.indices()
                    .create(request, RequestOptions.DEFAULT);
            if (!createIndexResponse.isAcknowledged()) {
                LOGGER.error("elasticsearch createIndex the response is acknowledged");
            } else {
                LOGGER.info("elasticsearch createIndex successful");
            }
        } catch (Exception e) {
            LOGGER.error("elasticsearch createIndex, error:{}", e.getMessage());
        }
    }

    public void batchCreatePage(String index, List<PageSyncDTO> pages) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                LOGGER.info("elasticsearch batchCreatePage {} time, starting...", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                LOGGER.info("elasticsearch batchCreatePage {} time, successful", request.numberOfActions());
                pageMapper.updateSyncEs();
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                LOGGER.error("elasticsearch batchCreatePage {} time, error:{}", request.numberOfActions(), failure.getMessage());
            }
        };

        BulkProcessor bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) ->
                        highLevelClient.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener)
                .setBulkActions(500)
                .setBulkSize(new ByteSizeValue(5L, ByteSizeUnit.MB))
                .setConcurrentRequests(0)
                .setFlushInterval(TimeValue.timeValueSeconds(10L))
                .setBackoffPolicy(BackoffPolicy
                        .constantBackoff(TimeValue.timeValueSeconds(1L), 3))
                .build();

        for (PageSyncDTO page : pages) {
            Map<String, Object> jsonMap = new HashMap<>(5);
            jsonMap.put(BaseStage.ES_PAGE_FIELD_PAGE_ID, page.getId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_TITLE, page.getTitle());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_CONTENT, page.getContent());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_PROJECT_ID, page.getProjectId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, page.getOrganizationId());
            IndexRequest request = new IndexRequest(index).id(String.valueOf(page.getId()))
                    .source(jsonMap);
            bulkProcessor.add(request);
        }
    }

    public void deletePage(String index, Long id) {
        DeleteRequest request = new DeleteRequest(index, String.valueOf(id));
        ActionListener<DeleteResponse> listener = new ActionListener<DeleteResponse>() {
            @Override
            public void onResponse(DeleteResponse deleteResponse) {
                LOGGER.info("elasticsearch deletePage successful, pageId:{}", id);
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.error("elasticsearch deletePage failure, pageId:{}, error:{}", id, e.getMessage());
            }
        };
        highLevelClient.deleteAsync(request, RequestOptions.DEFAULT, listener);
    }

    public void createOrUpdatePage(String index, Long id, PageSyncDTO page) {
        IndexRequest request = new IndexRequest(index);
        request.id(String.valueOf(id));
        Map<String, Object> jsonMap = new HashMap<>(5);
        jsonMap.put(BaseStage.ES_PAGE_FIELD_PAGE_ID, page.getId());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_TITLE, page.getTitle());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_CONTENT, page.getContent());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_PROJECT_ID, page.getProjectId());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, page.getOrganizationId());
        request.source(jsonMap);
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                LOGGER.info("elasticsearch createOrUpdatePage successful, pageId:{}", id);
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.error("elasticsearch createOrUpdatePage failure, pageId:{}, error:{}", id, e.getMessage());
                pageMapper.updateSyncEsByPageId(id, false);
            }
        };
        highLevelClient.indexAsync(request, RequestOptions.DEFAULT, listener);
    }

    public String searchById(Long organizationId, Long projectId, String index, Long id, String searchStr, Integer contentLength) {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, String.valueOf(organizationId)));
        if (projectId != null) {
            boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_PROJECT_ID, String.valueOf(projectId)));
        } else {
            boolBuilder.mustNot(QueryBuilders.existsQuery(BaseStage.ES_PAGE_FIELD_PROJECT_ID));
        }
        boolBuilder.filter(QueryBuilders.termQuery(BaseStage.ES_PAGE_FIELD_PAGE_ID, String.valueOf(id)));
        boolBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(BaseStage.ES_PAGE_FIELD_TITLE, searchStr))
                .should(QueryBuilders.matchQuery(BaseStage.ES_PAGE_FIELD_CONTENT, searchStr)));
        sourceBuilder.query(boolBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(1);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(true).field(BaseStage.ES_PAGE_FIELD_TITLE).field(BaseStage.ES_PAGE_FIELD_CONTENT)
                .preTags("<span style=\"color:#F44336\" >").postTags("</span>")
                .numOfFragments(1)
                .fragmentSize(contentLength)
                .noMatchSize(contentLength);
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        String fragmentString = null;
        SearchResponse response;
        try {
            response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits().getHits()) {
                //取高亮结果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField highlight = highlightFields.get(BaseStage.ES_PAGE_FIELD_CONTENT);
                if (highlight != null) {
                    Text[] fragments = highlight.fragments();
                    if (fragments != null) {
                        fragmentString = fragments[0].string();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("elasticsearch searchById failure, pageId:{}, error:{}", id, e.getMessage());
        }
        return fragmentString;
    }
}
