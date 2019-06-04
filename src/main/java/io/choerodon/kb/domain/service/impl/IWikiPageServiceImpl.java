package io.choerodon.kb.domain.service.impl;

import java.io.IOException;

import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import io.choerodon.core.exception.CommonException;
import io.choerodon.kb.domain.service.IWikiPageService;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.feign.WikiClient;

/**
 * Created by Zenger on 2019/5/31.
 */
@Service
public class IWikiPageServiceImpl implements IWikiPageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IWikiPageServiceImpl.class);

    private WikiClient wikiClient;

    public IWikiPageServiceImpl(WikiClient wikiClient) {
        this.wikiClient = wikiClient;
    }

    @Override
    public String getWikiOrganizationPage(String data) {

        LOGGER.info("get wiki organization page info by data: {}", data);
        try {
            Response<ResponseBody> response = wikiClient.getWikiOrganizationPage(BaseStage.USERNAME, data).execute();
            LOGGER.info("get wiki organization page info resource code:{} ", response.code());
            return response.body().string();
        } catch (IOException e) {
            throw new CommonException("error.wiki.organization.page.info.get", e);
        }
    }
}
