package io.choerodon.kb.infra.utils;

import io.choerodon.kb.api.vo.FullTextSearchResultVO;
import io.choerodon.kb.api.vo.PageSyncVO;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.dto.WorkSpacePageDTO;
import io.choerodon.kb.infra.mapper.PageMapper;
import io.choerodon.kb.infra.mapper.WorkSpacePageMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
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
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class EsRestUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(EsRestUtil.class);
    public static final String HIGHLIGHT_TAG_BIGIN = "<span style=\'color:rgb(244,67,54)\' >";
    public static final String HIGHLIGHT_TAG_END = "</span>";
    public static final String ALIAS_PAGE = "knowledge_page";
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private WorkSpacePageMapper workSpacePageMapper;

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
        //设置索引的settings，设置默认分词，不设置，因为搜索不按词搜索
//        request.settings(Settings.builder().put("analysis.analyzer.default.tokenizer", "ik_max_word"));
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

    public void deleteIndex(String index) {
        DeleteIndexRequest request;
        try {
            request = new DeleteIndexRequest(index);
            request.timeout(TimeValue.timeValueMinutes(2));
            AcknowledgedResponse deleteIndexResponse = highLevelClient.indices().delete(request, RequestOptions.DEFAULT);
            if (!deleteIndexResponse.isAcknowledged()) {
                LOGGER.error("elasticsearch deleteIndex the response is acknowledged");
            } else {
                LOGGER.info("elasticsearch deleteIndex successful");
            }
        } catch (Exception e) {
            LOGGER.error("elasticsearch indexExist, error:{}", e.getMessage());
        }
    }

    public void batchCreatePage(String index, List<PageSyncVO> pages) {
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

        for (PageSyncVO page : pages) {
            Map<String, Object> jsonMap = new HashMap<>(6);
            jsonMap.put(BaseStage.ES_PAGE_FIELD_PAGE_ID, page.getId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_TITLE, page.getTitle());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_CONTENT, page.getContent());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_PROJECT_ID, page.getProjectId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, page.getOrganizationId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_BASE_ID, page.getBaseId());
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

    public void createOrUpdatePage(String index, Long id, PageSyncVO page) {
        IndexRequest request = new IndexRequest(index);
        request.id(String.valueOf(id));
        Map<String, Object> jsonMap = new HashMap<>(6);
        jsonMap.put(BaseStage.ES_PAGE_FIELD_PAGE_ID, page.getId());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_TITLE, page.getTitle());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_CONTENT, page.getContent());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_PROJECT_ID, page.getProjectId());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, page.getOrganizationId());
        jsonMap.put(BaseStage.ES_PAGE_FIELD_BASE_ID, page.getBaseId());
        request.source(jsonMap);
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                LOGGER.info("elasticsearch createOrUpdatePage successful, pageId:{}", id);
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.error("elasticsearch createOrUpdatePage failure, pageId:{}, error:{}", id, e.getMessage());
//                pageMapper.updateSyncEsByPageId(id, false);
            }
        };
        highLevelClient.indexAsync(request, RequestOptions.DEFAULT, listener);
    }

    public List<FullTextSearchResultVO> fullTextSearch(Long organizationId, Long projectId, String index, String searchStr,Long baseId) {
        List<FullTextSearchResultVO> results = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_BASE_ID, String.valueOf(baseId)));
        if (organizationId != null) {
            boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, String.valueOf(organizationId)));
        }
        if (projectId != null) {
            //项目层可以查到组织层的数据
            boolBuilder.must(QueryBuilders.boolQuery()
                    .should(QueryBuilders.boolQuery().filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_PROJECT_ID, String.valueOf(projectId))))
                    .should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery(BaseStage.ES_PAGE_FIELD_PROJECT_ID))));
        } else {
            boolBuilder.mustNot(QueryBuilders.existsQuery(BaseStage.ES_PAGE_FIELD_PROJECT_ID));
        }
        boolBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhrasePrefixQuery(BaseStage.ES_PAGE_FIELD_TITLE, searchStr))
                .should(QueryBuilders.matchPhrasePrefixQuery(BaseStage.ES_PAGE_FIELD_CONTENT, searchStr)));
        sourceBuilder.query(boolBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(20);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false).field(BaseStage.ES_PAGE_FIELD_TITLE).field(BaseStage.ES_PAGE_FIELD_CONTENT)
                .preTags("").postTags("")
                .fragmentSize(50)
                .noMatchSize(50);
        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response;
        try {
            response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            List<Long> pageIds = new ArrayList<>();
            Arrays.stream(response.getHits().getHits())
                    .forEach(hit -> {
                        Map<String, Object> map = hit.getSourceAsMap();
                        Object proIdObj = map.get(BaseStage.ES_PAGE_FIELD_PROJECT_ID);
                        Object orgIdObj = map.get(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID);
                        Object titleObj = map.get(BaseStage.ES_PAGE_FIELD_TITLE);
                        Long pageId = Long.parseLong(hit.getId());
                        Long esProjectId = proIdObj != null ? Long.parseLong(String.valueOf(proIdObj)) : null;
                        Long esOrganizationId = orgIdObj != null ? Long.parseLong(String.valueOf(orgIdObj)) : null;
                        String title = titleObj != null ? String.valueOf(titleObj) : "";
                        FullTextSearchResultVO resultVO = new FullTextSearchResultVO(pageId, title, null, esProjectId, esOrganizationId);
                        pageIds.add(pageId);
                        //设置评分
                        resultVO.setScore(hit.getScore());
                        //取高亮结果
                        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                        HighlightField highlight = highlightFields.get(BaseStage.ES_PAGE_FIELD_CONTENT);
                        if (highlight != null) {
                            Text[] fragments = highlight.fragments();
                            if (fragments != null) {
                                String fragmentString = fragments[0].string();
                                resultVO.setHighlightContent(fragmentString.replaceAll(searchStr, HIGHLIGHT_TAG_BIGIN + searchStr + HIGHLIGHT_TAG_END));
                            } else {
                                resultVO.setHighlightContent("");
                            }
                        } else {
                            resultVO.setHighlightContent("");
                        }
                        results.add(resultVO);
                    });
            if (!pageIds.isEmpty()) {
                List<WorkSpacePageDTO> workSpacePageDTOs = workSpacePageMapper.queryByPageIds(pageIds);
                Map<Long, Long> map = workSpacePageDTOs.stream().collect(Collectors.toMap(WorkSpacePageDTO::getPageId, WorkSpacePageDTO::getWorkspaceId, (str1, str2) -> str2));
                results.stream().forEach(x -> x.setWorkSpaceId(map.get(x.getPageId())));
            }
            LOGGER.info("全文搜索结果:组织ID:{},项目ID:{},命中{},搜索内容:{}", organizationId, projectId, response.getHits().getTotalHits(), searchStr);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return results;
    }

    public String highlightContent(String searchStr, String content) {
        return content.replaceAll(searchStr, HIGHLIGHT_TAG_BIGIN + searchStr + HIGHLIGHT_TAG_END);
    }

    /**
     * 批量同步mysql数据到es中，同步所有数据
     */
    public void manualSyncPageData2Es() {
        this.deleteIndex(BaseStage.ES_PAGE_INDEX);
        if (!this.indexExist(BaseStage.ES_PAGE_INDEX)) {
            this.createIndex(BaseStage.ES_PAGE_INDEX);
        }
        List<PageSyncVO> pages = pageMapper.querySync2EsPage(null);
        LOGGER.info("EsRestUtil manualSyncPageData2Es,sync page count:{}", pages.size());
        this.batchCreatePage(BaseStage.ES_PAGE_INDEX, pages);
    }
}
