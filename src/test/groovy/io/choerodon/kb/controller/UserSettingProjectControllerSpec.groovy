package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO
import io.choerodon.kb.api.vo.UserSettingVO
import io.choerodon.kb.api.vo.WorkSpaceInfoVO
import io.choerodon.kb.app.service.WorkSpaceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
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
class UserSettingProjectControllerSpec extends Specification {

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

    def url = '/v1/projects/{project_id}/user_setting'

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

    def "createOrUpdate"() {
        given:
        '准备'
        UserSettingVO create = new UserSettingVO()
        create.organizationId = organizationId
        create.projectId = projectId
        create.type = "edit_mode"
        create.editMode = "markdown"
        when:
        '项目层创建或更新个人设置'
        HttpEntity<UserSettingVO> httpEntity = new HttpEntity<>(create)
        def entity = restTemplate.exchange(url + "?organizationId=" + organizationId, HttpMethod.POST, httpEntity, ResponseEntity, projectId)

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