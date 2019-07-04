package io.choerodon.kb.infra.common.utils;

import io.choerodon.core.exception.CommonException;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author shinan.chen
 * @since 2019/7/4
 */
@Component
public class EsRestUtil {
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
        request.alias(new Alias(ALIAS_PAGE));
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
}
