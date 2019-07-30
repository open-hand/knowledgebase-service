package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.*
import io.choerodon.kb.app.service.PageCommentService
import io.choerodon.kb.app.service.WorkSpaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
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
class PageCommentOrganizationControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Autowired
    PageCommentService pageCommentService
    @Shared
    Long organizationId = 1L
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    PageCommentVO pageComment
    @Shared
    Long createPageCommentId
    @Shared
    Boolean isFirst = true

    def url = '/v1/organizations/{organization_id}/page_comment'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, null, pageCreateWithoutContent)
            PageCreateCommentVO create = new PageCreateCommentVO()
            create.pageId = workSpaceInfo.pageInfo.id
            create.comment = "评论内容"
            createPageCommentId = pageCommentService.create(organizationId, null, create).id
        }
    }

    def "create"() {
        given:
        '准备'
        PageCreateCommentVO create = new PageCreateCommentVO()
        create.pageId = workSpaceInfo.pageInfo.id
        create.comment = "评论内容"
        when:
        '创建page评论'
        HttpEntity<PageCreateCommentVO> httpEntity = new HttpEntity<>(create)
        def entity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, PageCommentVO.class, organizationId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.body.id != null) {
                    pageComment = entity.body
                }
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.id != null
    }

    def "queryByPageId"() {
        when:
        '查询页面评论'
        ParameterizedTypeReference<List<PageCommentVO>> typeRef = new ParameterizedTypeReference<List<PageCommentVO>>() {
        }
        def entity = restTemplate.exchange(url + "/list?pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, typeRef, organizationId)

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

    def "update"() {
        given:
        '准备'
        PageUpdateCommentVO update = new PageUpdateCommentVO()
        update.pageId = workSpaceInfo.pageInfo.id
        update.comment = "更新内容"
        update.objectVersionNumber = pageComment.objectVersionNumber
        when:
        '更新page评论'
        HttpEntity<PageUpdateCommentVO> httpEntity = new HttpEntity<>(update)
        def entity = restTemplate.exchange(url + "/{id}", HttpMethod.PUT, httpEntity, PageCommentVO.class, organizationId, pageComment.id)

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

    def "deleteComment"() {
        when:
        '通过id删除评论（管理员权限）'
        def entity = restTemplate.exchange(url + "/{id}", HttpMethod.DELETE, null, ResponseEntity, organizationId, createPageCommentId)

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

    def "deleteMyComment"() {
        when:
        '通过id删除评论（删除自己的评论）'
        def entity = restTemplate.exchange(url + "/delete_my/{id}", HttpMethod.DELETE, null, ResponseEntity, organizationId, pageComment.id)

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