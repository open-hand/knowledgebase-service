package io.choerodon.kb.infra.common.utils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.api.dao.PageSyncDTO;
import io.choerodon.kb.infra.common.BaseStage;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.ByteSizeUnit;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.elasticsearch.common.unit.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Boolean indexExist(String index) {
        GetIndexRequest request;
        Boolean exists = false;
        try {
            request = new GetIndexRequest(index);
            exists = highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return exists;
    }

    public void createIndex(String index) {
        CreateIndexRequest request = new CreateIndexRequest(index);
        //设置索引的settings，设置默认分词
        request.settings(Settings.builder().put("analysis.analyzer.default.tokenizer", "ik_smart"));
//        request.alias(new Alias(ALIAS_PAGE));
        CreateIndexResponse createIndexResponse = null;
        try {
            createIndexResponse = highLevelClient.indices()
                    .create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!createIndexResponse.isAcknowledged()) {
            throw new CommonException("error.elasticsearch.createIndex");
        }
    }

    public void batchCreatePage(String index, List<PageSyncDTO> pages) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {
                LOGGER.info("batchCreatePage {} time, starting...", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  BulkResponse response) {
                LOGGER.info("batchCreatePage {} time, complete", request.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request,
                                  Throwable failure) {
                LOGGER.error("batchCreatePage {} time, error:{}", request.numberOfActions(), failure.getMessage());
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
            Map<String, Object> jsonMap = new HashMap<>(2);
            jsonMap.put(BaseStage.ES_PAGE_FIELD_TITLE, page.getTitle());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_CONTENT, page.getContent());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_PROJECT_ID, page.getProjectId());
            jsonMap.put(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, page.getOrganizationId());
            IndexRequest request = new IndexRequest(index).id(String.valueOf(page.getId()))
                    .source(jsonMap);
            bulkProcessor.add(request);
        }
    }
}
