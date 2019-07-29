package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.PageAttachmentVO
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO
import io.choerodon.kb.api.vo.WorkSpaceInfoVO
import io.choerodon.kb.api.vo.WorkSpaceShareVO
import io.choerodon.kb.app.service.WorkSpaceService
import io.choerodon.kb.app.service.WorkSpaceShareService
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
class WorkSpaceShareControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Autowired
    WorkSpaceShareService workSpaceShareService
    @Shared
    Long organizationId = 1L
    @Shared
    Long projectId = 1L
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    WorkSpaceShareVO workSpaceShareVO
    @Shared
    Boolean isFirst = true

    def url = '/v1/work_space_share'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, projectId, pageCreateWithoutContent)
            workSpaceShareVO = workSpaceShareService.queryShare(organizationId, projectId, workSpaceInfo.id)
        }
    }

    def "queryTree"() {
        when:
        '查询分享链接的树形结构'
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
        }
        def entity = restTemplate.exchange(url + "/tree?token=" + workSpaceShareVO.token, HttpMethod.GET, null, typeRef)

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

    def "queryPage"() {
        when:
        '查询分享链接的页面信息'
        def entity = restTemplate.exchange(url + "/page?token=" + workSpaceShareVO.token + "&&work_space_id=" + workSpaceInfo.id, HttpMethod.GET, null, WorkSpaceInfoVO.class)

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

    def "queryPageAttachment"() {
        when:
        '查询分享链接的页面附件'
        ParameterizedTypeReference<List<PageAttachmentVO>> typeRef = new ParameterizedTypeReference<List<PageAttachmentVO>>() {
        }
        def entity = restTemplate.exchange(url + "/page_attachment?token=" + workSpaceShareVO.token + "&&page_id=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, typeRef)

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
        actRequest == true && entity.body.size() == 0
    }

    def "exportMd2Pdf"() {
        given:
        '准备'
        PageCreateWithoutContentVO create = new PageCreateWithoutContentVO()
        create.parentWorkspaceId = 0L
        create.title = "新文档"
        when:
        '分享链接的文章导出为pdf'
        def entity = restTemplate.getForEntity(url + "/export_pdf?token=" + workSpaceShareVO.token + "&&pageId=" + workSpaceInfo.pageInfo.id, null, projectId)

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