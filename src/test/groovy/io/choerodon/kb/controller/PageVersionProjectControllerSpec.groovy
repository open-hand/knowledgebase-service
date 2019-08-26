package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.*
import io.choerodon.kb.app.service.WorkSpaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
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
class PageVersionProjectControllerSpec extends Specification {

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
    List<PageVersionVO> pageVersions
    @Shared
    Boolean isFirst = true

    def url = '/v1/projects/{project_id}/page_version'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateWithoutContent)
            //更改两次版本
            PageUpdateVO update = new PageUpdateVO()
            update.title = "新标题"
            update.content = "新内容"
            update.minorEdit = true
            update.objectVersionNumber = workSpaceInfo.objectVersionNumber
            workSpaceInfo = workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, workSpaceInfo.id, null, update)
            update = new PageUpdateVO()
            update.title = "新新标题"
            update.content = "新新内容"
            update.minorEdit = true
            update.objectVersionNumber = workSpaceInfo.objectVersionNumber
            workSpaceInfo = workSpaceService.updateWorkSpaceAndPage(organizationId, projectId, workSpaceInfo.id, null, update)
        }
    }

    def "listQuery"() {
        when:
        '查询页面的版本列表'
        ParameterizedTypeReference<List<PageVersionVO>> typeRef = new ParameterizedTypeReference<List<PageVersionVO>>() {
        }
        def entity = restTemplate.exchange(url + "/list?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, typeRef, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                pageVersions = entity.body
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.size() > 0
    }

    def "queryById"() {
        when:
        '查询版本内容'
        def entity = restTemplate.exchange(url + "/{version_id}?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, PageVersionInfoVO.class, projectId, pageVersions.get(0).id)

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

    def "compareVersion"() {
        when:
        '版本比较'
        def entity = restTemplate.exchange(url + "/compare?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id + "&&firstVersionId=" + pageVersions.get(0).id + "&&secondVersionId=" + pageVersions.get(0).id, HttpMethod.GET, null, PageVersionCompareVO.class, projectId)

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
        actRequest == true && entity.body.diffContent != null
    }

    def "rollbackVersion"() {
        when:
        '版本回退'
        def entity = restTemplate.getForEntity(url + "/rollback?organizationId=" + organizationId + "&&pageId=" + workSpaceInfo.pageInfo.id + "&&versionId=" + pageVersions.get(0).id, null, projectId)

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