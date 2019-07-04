package io.choerodon.kb.app.service.impl;

import com.vladsch.flexmark.convert.html.FlexmarkHtmlParser;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.kb.api.dao.*;
import io.choerodon.kb.app.service.PageService;
import io.choerodon.kb.app.service.WorkSpaceService;
import io.choerodon.kb.domain.kb.repository.PageContentRepository;
import io.choerodon.kb.domain.kb.repository.PageRepository;
import io.choerodon.kb.infra.common.BaseStage;
import io.choerodon.kb.infra.common.utils.PdfUtil;
import io.choerodon.kb.infra.dataobject.PageContentDO;
import io.choerodon.kb.infra.dataobject.PageDO;
import io.choerodon.kb.infra.mapper.PageContentMapper;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.util.Charsets;
import org.docx4j.Docx4J;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zenger on 2019/4/30.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class PageServiceImpl implements PageService {

    public static final Logger LOGGER = LoggerFactory.getLogger(PageServiceImpl.class);
    public static final String SUFFIX_DOCX = ".docx";
    public static final String FILE_ILLEGAL = "error.importDocx2Md.fileIllegal";
    @Value("${services.attachment.url}")
    private String attachmentUrl;
    @Autowired
    private WorkSpaceService workSpaceService;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private PageContentRepository pageContentRepository;
    @Autowired
    private PageContentMapper pageContentMapper;
    @Autowired
    private RestHighLevelClient highLevelClient;


    @Override
    public Boolean checkPageCreate(Long id) {
        PageDO pageDO = pageRepository.selectById(id);
        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        return pageDO.getCreatedBy().equals(customUserDetails.getUserId());
    }

    @Override
    public void exportMd2Pdf(Long organizationId, Long projectId, Long pageId, HttpServletResponse response) {
        PageInfo pageInfo = pageRepository.queryInfoById(organizationId, projectId, pageId);
        PdfUtil.markdown2Pdf(pageInfo.getTitle(), pageInfo.getContent(), response);
    }

    @Override
    public String importDocx2Md(Long organizationId, Long projectId, MultipartFile file, String type) {
        if (!file.getOriginalFilename().endsWith(SUFFIX_DOCX)) {
            throw new CommonException(FILE_ILLEGAL);
        }
        WordprocessingMLPackage wordMLPackage;
        try {
            wordMLPackage = Docx4J.load(file.getInputStream());
            HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
            htmlSettings.setWmlPackage(wordMLPackage);
            ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
            Docx4J.toHTML(htmlSettings, swapStream, Docx4J.FLAG_EXPORT_PREFER_XSL);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(swapStream.toByteArray());
            String html = IOUtils.toString(inputStream, String.valueOf(Charsets.UTF_8));
            String markdown = FlexmarkHtmlParser.parse(html);
            return markdown;
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
    }

    @Override
    public PageDTO createPage(Long resourceId, PageCreateDTO create, String type) {
        //创建页面及空间("第一次创建版本为空")
        PageUpdateDTO pageUpdateDTO = new PageUpdateDTO();
        pageUpdateDTO.setContent(create.getContent());
        create.setContent("");
        PageDTO pageDTO = workSpaceService.create(resourceId, create, type);
        //更新页面内容
        pageUpdateDTO.setMinorEdit(false);
        pageUpdateDTO.setObjectVersionNumber(pageDTO.getObjectVersionNumber());
        return workSpaceService.update(resourceId, pageDTO.getWorkSpace().getId(), pageUpdateDTO, type);
    }

    @Override
    public void autoSavePage(Long organizationId, Long projectId, Long pageId, PageAutoSaveDTO autoSave) {
        PageContentDO pageContent = queryDraftContent(organizationId, projectId, pageId);
        if (pageContent == null) {
            //创建草稿内容
            pageContent = new PageContentDO();
            pageContent.setPageId(pageId);
            pageContent.setVersionId(0L);
            pageContent.setContent(autoSave.getContent());
            pageContentRepository.create(pageContent);
        } else {
            //修改草稿内容
            pageContent.setContent(autoSave.getContent());
            pageContentRepository.update(pageContent);
        }
    }

    @Override
    public PageContentDO queryDraftContent(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        PageContentDO pageContent = new PageContentDO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(0L);
        pageContent.setCreatedBy(userId);
        List<PageContentDO> contents = pageContentMapper.select(pageContent);
        return contents.isEmpty() ? null : contents.get(0);
    }

    @Override
    public void deleteDraftContent(Long organizationId, Long projectId, Long pageId) {
        pageRepository.checkById(organizationId, projectId, pageId);
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        Long userId = userDetails.getUserId();
        PageContentDO pageContent = new PageContentDO();
        pageContent.setPageId(pageId);
        pageContent.setVersionId(0L);
        pageContent.setCreatedBy(userId);
        pageContentMapper.delete(pageContent);
    }

    @Override
    public List<FullTextSearchResultDTO> fullTextSearch(Long organizationId, Long projectId, String searchStr) {
        List<FullTextSearchResultDTO> results = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest(BaseStage.ES_PAGE_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolBuilder = new BoolQueryBuilder();
        boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_ORGANIZATION_ID, String.valueOf(organizationId)));
        if (projectId != null) {
            boolBuilder.filter(new TermQueryBuilder(BaseStage.ES_PAGE_FIELD_PROJECT_ID, String.valueOf(projectId)));
        } else {
            boolBuilder.mustNot(QueryBuilders.existsQuery(BaseStage.ES_PAGE_FIELD_PROJECT_ID));
        }
        boolBuilder.must(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery(BaseStage.ES_PAGE_FIELD_TITLE, searchStr))
                .should(QueryBuilders.matchQuery(BaseStage.ES_PAGE_FIELD_CONTENT, searchStr)));
        sourceBuilder.query(boolBuilder);
        sourceBuilder.from(0);
        sourceBuilder.size(20);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        // 高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(true).field(BaseStage.ES_PAGE_FIELD_TITLE).field(BaseStage.ES_PAGE_FIELD_CONTENT)
                .preTags("<span style=\"color:#F44336\" >").postTags("</span>")
                .fragmentSize(50)
                .noMatchSize(50);

        sourceBuilder.highlighter(highlightBuilder);
        searchRequest.source(sourceBuilder);
        SearchResponse response;
        try {
            response = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
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
                        FullTextSearchResultDTO resultDTO = new FullTextSearchResultDTO(pageId, title, null, esProjectId, esOrganizationId);
                        //设置评分
                        resultDTO.setScore(hit.getScore());
                        //取高亮结果
                        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                        HighlightField highlight = highlightFields.get(BaseStage.ES_PAGE_FIELD_CONTENT);
                        if (highlight != null) {
                            Text[] fragments = highlight.fragments();
                            if (fragments != null) {
                                String fragmentString = fragments[0].string();
                                resultDTO.setHighlightContent(fragmentString);
                            } else {
                                resultDTO.setHighlightContent("");
                            }
                        } else {
                            resultDTO.setHighlightContent("");
                        }
                        results.add(resultDTO);
                    });
            LOGGER.info("全文搜索结果:组织ID:{},项目ID:{},命中{},搜索内容:{}", organizationId, projectId, response.getHits().getTotalHits(), searchStr);
        } catch (IOException e) {
            throw new CommonException(e.getMessage());
        }
        return results;
    }
}
