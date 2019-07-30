package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO
import io.choerodon.kb.api.vo.PageLogVO
import io.choerodon.kb.api.vo.WorkSpaceInfoVO
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
 * @since 2019/7/22
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@ActiveProfiles("test")
@Stepwise
class PageLogOrganizationControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Shared
    Long organizationId = 1L
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    Boolean isFirst = true

    def url = '/v1/organizations/{organization_id}/page_log'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, null, pageCreateWithoutContent)
        }
    }

    def "listByPageId"() {
        when:
        '查询页面操作日志'
        ParameterizedTypeReference<List<PageLogVO>> typeRef = new ParameterizedTypeReference<List<PageLogVO>>() {
        }
        def entity = restTemplate.exchange(url + "/{page_id}", HttpMethod.GET, null, typeRef, organizationId, workSpaceInfo.pageInfo.id)

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
        actRequest == true && entity.body.size() > 0
    }
}