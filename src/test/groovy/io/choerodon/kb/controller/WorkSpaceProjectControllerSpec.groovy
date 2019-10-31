package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.*
import io.choerodon.kb.app.service.WorkSpaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
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
class WorkSpaceProjectControllerSpec extends Specification {

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
    Long createWorkSpaceId
    @Shared
    Boolean isFirst = true

    def url = '/v1/projects/{project_id}/work_space'

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

    def "createWorkSpaceAndPage"() {
        given:
        '准备'
        PageCreateWithoutContentVO create = new PageCreateWithoutContentVO()
        create.parentWorkspaceId = 0L
        create.title = "新文档"
        when:
        '项目下创建页面和空页面'
        HttpEntity<PageCreateWithoutContentVO> httpEntity = new HttpEntity<>(create)
        def entity = restTemplate.exchange(url + "?organizationId=" + organizationId, HttpMethod.POST, httpEntity, WorkSpaceInfoVO.class, projectId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                if (entity.body.id != null) {
                    createWorkSpaceId = entity.body.id
                }
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.id != null
    }

    def "queryWorkSpaceInfo"() {
        when:
        '查询项目下工作空间节点页面'
        def entity = restTemplate.exchange(url + "/{id}?organizationId=" + organizationId + "&&searchStr=test", HttpMethod.GET, null, WorkSpaceInfoVO.class, projectId, workSpaceInfo.id)

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

    def "updateWorkSpaceAndPage"() {
        given:
        '准备'
        PageUpdateVO update = new PageUpdateVO()
        update.title = '新标题'
        update.content = '新内容'
        update.minorEdit = true
        update.objectVersionNumber = workSpaceInfo.pageInfo.objectVersionNumber
        when:
        '更新项目下工作空间节点页面'
        HttpEntity<PageUpdateVO> httpEntity = new HttpEntity<>(update)
        def entity = restTemplate.exchange(url + "/{id}?organizationId=" + organizationId, HttpMethod.PUT, httpEntity, WorkSpaceInfoVO.class, projectId, workSpaceInfo.id)

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

    def "moveWorkSpace"() {
        given:
        '准备'
        MoveWorkSpaceVO move = new MoveWorkSpaceVO()
        move.id = workSpaceInfo.id
        move.before = true
        move.targetId = createWorkSpaceId
        when:
        '移动文章'
        HttpEntity<MoveWorkSpaceVO> httpEntity = new HttpEntity<>(move)
        def entity = restTemplate.exchange(url + "/to_move/{id}?organizationId=" + organizationId, HttpMethod.POST, httpEntity, Object, projectId, workSpaceInfo.id)

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

    def "queryAllTreeList"() {
        when:
        '查询空间树形结构'
        ParameterizedTypeReference<Map<String, Map<String, Object>>> typeRef = new ParameterizedTypeReference<Map<String, Map<String, Object>>>() {
        }
        def entity = restTemplate.exchange(url + "/all_tree?organizationId=" + organizationId + "&&expandWorkSpaceId=" + workSpaceInfo.id, HttpMethod.GET, null, typeRef, projectId)

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
        actRequest == true && entity.body.entrySet().getAt(0).getValue().get("data").get("items").size() > 0
    }

    def "queryAllSpaceByOptions"() {
        when:
        '查询项目下的所有空间'
        ParameterizedTypeReference<List<WorkSpaceVO>> typeRef = new ParameterizedTypeReference<List<WorkSpaceVO>>() {
        }
        def entity = restTemplate.exchange(url + "?organizationId=" + organizationId, HttpMethod.GET, null, typeRef, projectId)

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

    def "querySpaceByIds"() {
        given:
        '准备'
        List<Long> spaceIds = new ArrayList<>()
        spaceIds.add(createWorkSpaceId)
        spaceIds.add(workSpaceInfo.id)
        when:
        '根据spaceIds查询空间列表'
        ParameterizedTypeReference<List<WorkSpaceVO>> typeRef = new ParameterizedTypeReference<List<WorkSpaceVO>>() {
        }
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(spaceIds)
        def entity = restTemplate.exchange(url + "/query_by_space_ids", HttpMethod.POST, httpEntity, typeRef, projectId)
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

    def "recentUpdateList"() {
        given:
        '准备'
        when:
        '查询最近更新的空间列表'
        ParameterizedTypeReference<List<WorkSpaceRecentInfoVO>> typeRef = new ParameterizedTypeReference<List<WorkSpaceRecentInfoVO>>() {
        }
        def entity = restTemplate.exchange(url + "/recent_update_list?organizationId=" + organizationId, HttpMethod.GET, null, typeRef, projectId)
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

    def "recycleWorkspaceTree"() {
        given:
        '准备'
        when:
        '查询回收站的空间列表'
        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<Map<String, Object>>() {
        }
        def entity = restTemplate.exchange(url + "/recycle_workspace_tree?organizationId=" + organizationId, HttpMethod.GET, null, typeRef, projectId)
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

    def "removeWorkSpaceAndPage"() {
        when:
        '移除项目下工作空间及页面（管理员权限）'
        def entity = restTemplate.exchange(url + "/remove/{id}?organizationId=" + organizationId, HttpMethod.PUT, null, WorkSpaceInfoVO.class, projectId, workSpaceInfo.id)

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

    def "removeWorkSpaceAndPageByMyWorkSpace"() {
        given:
        '准备'

        when:
        '移除项目下工作空间及页面（移除自己的空间）'
        def entity = restTemplate.exchange(url + "/remove_my/{id}?organizationId=" + organizationId, HttpMethod.PUT, null, WorkSpaceInfoVO.class, projectId, createWorkSpaceId)

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

    def "deleteWorkSpaceAndPage"() {
        given:
        '准备'

        when:
        '删除项目下工作空间及页面（管理员）'
        def entity = restTemplate.exchange(url + "/delete/{id}?organizationId=" + organizationId, HttpMethod.DELETE, null, WorkSpaceInfoVO.class, projectId, createWorkSpaceId)

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

    def "restoreWorkSpaceAndPage"() {
        given:
        '准备'

        when:
        '还原项目下工作空间及页面'
        def entity = restTemplate.exchange(url + "/restore/{id}?organizationId=" + organizationId, HttpMethod.PUT, null, WorkSpaceInfoVO.class, projectId, workSpaceInfo.id)

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
