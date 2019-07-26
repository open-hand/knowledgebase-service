package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.*
import io.choerodon.kb.app.service.WorkSpaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * @author shinan.chen
 * @since 2019/7/23
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@ActiveProfiles("test")
@Stepwise
class PageProjectControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Shared
    Long organizationId = 1L
    @Shared
    Long projectId = 1L
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    Boolean isFirst = true

    def url = '/v1/projects/{project_id}/page'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateWithoutContent)
        }
    }

    def "exportMd2Pdf"() {
        given:
        '准备'
        PageCreateWithoutContentVO create = new PageCreateWithoutContentVO()
        create.parentWorkspaceId = 0L
        create.title = "新文档"
        when:
        '导出文章为pdf'
        def entity = restTemplate.getForEntity(url + "/export_pdf?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id, null, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true
    }

    def "importDocx2Md"() {
        given:
        '准备'
        HttpHeaders headers = new HttpHeaders()
        MediaType type = MediaType.parseMediaType("multipart/form-data")
        // 设置请求的格式类型
        headers.setContentType(type)
        FileSystemResource fileSystemResource = new FileSystemResource(this.getClass().getResource("/file/demo.docx").getPath())
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>()
        form.add("file", fileSystemResource)
        when:
        '导入word文档为markdown数据（目前只支持docx）'
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers);
        def entity = restTemplate.exchange(url + "/import_word?organizationId=" + organizationId, HttpMethod.POST, files, String.class, projectId)
        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true&&entity.getBody()!=null
    }

    def "createPageByImport"() {
        given:
        '准备'
        PageCreateVO create = new PageCreateVO()
        create.title = '新标题'
        create.content = '新内容'
        create.parentWorkspaceId = 0L
        when:
        '"创建页面（带有内容）'
        HttpEntity<PageCreateVO> httpEntity = new HttpEntity<>(create)
        def entity = restTemplate.exchange(url + "?organizationId=" + organizationId, HttpMethod.POST, httpEntity, WorkSpaceInfoVO.class, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.id != null
    }

    def "autoSavePage"() {
        given:
        '准备'
        PageAutoSaveVO saveVO = new PageAutoSaveVO()
        saveVO.content = '新内容'
        when:
        '"文章自动保存'
        HttpEntity<PageAutoSaveVO> httpEntity = new HttpEntity<>(saveVO)
        def entity = restTemplate.exchange(url + "/auto_save?organizationId=" + organizationId+"&&pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.PUT, httpEntity, ResponseEntity.class, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true
    }


    def "queryDraftPage"() {
        when:
        '页面恢复草稿'
        def entity = restTemplate.exchange(url + "/draft_page?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, String.class, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body != null
    }

    def "deleteDraftContent"() {
        when:
        '删除草稿'
        def entity = restTemplate.exchange(url + "/delete_draft?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.DELETE, null, ResponseEntity, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true
    }

    def "fullTextSearch"() {
        when:
        '全文搜索'
        ParameterizedTypeReference<List<FullTextSearchResultVO>> typeRef = new ParameterizedTypeReference<List<FullTextSearchResultVO>>() {
        }
        def entity = restTemplate.exchange(url + "/full_text_search?organizationId=" + organizationId + "&&searchStr=文档", HttpMethod.GET, null, typeRef, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
            }
        }
        expect:
        '测试用例：'
        actRequest == true
    }

}