package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO
import io.choerodon.kb.api.vo.WorkSpaceInfoVO
import io.choerodon.kb.api.vo.WorkSpaceShareUpdateVO
import io.choerodon.kb.api.vo.WorkSpaceShareVO
import io.choerodon.kb.app.service.WorkSpaceService
import io.choerodon.kb.infra.enums.ShareType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.test.context.ActiveProfiles
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

/**
 * @author shinan.chen
 * @since 2019/7/26
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfiguration)
@ActiveProfiles("test")
@Stepwise
class WorkSpaceShareOrganizationControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Shared
    Long organizationId = 1L
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    WorkSpaceShareVO workSpaceShareVO
    @Shared
    Boolean isFirst = true

    def url = '/v1/organizations/{organization_id}/work_space_share'

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

    def "queryShare"() {
        when:
        '查询分享链接（不存在则创建）'
        def entity = restTemplate.exchange(url + "?work_space_id=" + workSpaceInfo.id, HttpMethod.GET, null, WorkSpaceShareVO.class, organizationId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                workSpaceShareVO = entity.body
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.id != null
    }

    def "updateShare"() {
        given:
        '准备'
        WorkSpaceShareUpdateVO update = new WorkSpaceShareUpdateVO()
        update.type = ShareType.CURRENT
        update.objectVersionNumber = workSpaceShareVO.objectVersionNumber
        when:
        '修改分享链接类型'
        HttpEntity<WorkSpaceShareUpdateVO> httpEntity = new HttpEntity<>(update)
        def entity = restTemplate.exchange(url + "/{id}", HttpMethod.PUT, httpEntity, WorkSpaceShareVO.class, organizationId, workSpaceShareVO.id)

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
}