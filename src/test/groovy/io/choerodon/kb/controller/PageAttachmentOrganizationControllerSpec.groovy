package io.choerodon.kb.controller

import io.choerodon.kb.IntegrationTestConfiguration
import io.choerodon.kb.api.vo.PageAttachmentVO
import io.choerodon.kb.api.vo.PageCreateWithoutContentVO
import io.choerodon.kb.api.vo.WorkSpaceInfoVO
import io.choerodon.kb.app.service.WorkSpaceService
import io.choerodon.kb.infra.dto.PageAttachmentDTO
import io.choerodon.kb.infra.mapper.PageAttachmentMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.core.io.FileSystemResource
import org.springframework.http.*
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
class PageAttachmentOrganizationControllerSpec extends Specification {

    @Autowired
    TestRestTemplate restTemplate
    @Autowired
    WorkSpaceService workSpaceService
    @Autowired
    PageAttachmentMapper pageAttachmentMapper
    @Shared
    Long organizationId = 1L
    @Shared
    List<PageAttachmentVO> pageAttachments
    @Shared
    WorkSpaceInfoVO workSpaceInfo
    @Shared
    Long pageAttachmentId
    @Shared
    Boolean isFirst = true

    def url = '/v1/organizations/{organization_id}/page_attachment'

    def setup() {
        if (isFirst) {
            isFirst = false
            println "初始化一个空间及文档"
            PageCreateWithoutContentVO pageCreateWithoutContent = new PageCreateWithoutContentVO()
            pageCreateWithoutContent.parentWorkspaceId = 0L
            pageCreateWithoutContent.title = "第一篇文档"
            workSpaceInfo = workSpaceService.createWorkSpaceAndPage(organizationId, null, pageCreateWithoutContent)
            PageAttachmentDTO create = new PageAttachmentDTO()
            create.pageId = workSpaceInfo.pageInfo.id
            create.name = "test"
            pageAttachmentMapper.insert(create)
            pageAttachmentId = create.id
        }
    }

    def "create"() {
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
        '页面上传附件'
        ParameterizedTypeReference<List<PageAttachmentVO>> typeRef = new ParameterizedTypeReference<List<PageAttachmentVO>>() {
        }
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers)
        def entity = restTemplate.exchange(url + "?pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.POST, files, typeRef, organizationId)

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

    def "queryByList"() {
        when:
        '查询页面附件'
        ParameterizedTypeReference<List<PageAttachmentVO>> typeRef = new ParameterizedTypeReference<List<PageAttachmentVO>>() {
        }
        def entity = restTemplate.exchange(url + "/list?pageId=" + workSpaceInfo.pageInfo.id, HttpMethod.GET, null, typeRef, organizationId)

        then:
        '状态码为200，调用成功'
        def actRequest = false
        if (entity != null) {
            if (entity.getStatusCode().is2xxSuccessful()) {
                actRequest = true
                pageAttachments = entity.body
            }
        }
        expect:
        '测试用例：'
        actRequest == true && entity.body.size() > 0
    }

    def "queryByFileName"() {
        when:
        '根据文件名获取附件地址，用于编辑文档中快捷找到附件地址'
        def entity = restTemplate.exchange(url + "/query_by_file_name?fileName=demo.docx", HttpMethod.GET, null, PageAttachmentVO.class, organizationId)
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

    def "delete"() {
        when:
        '页面删除附件'
        def entity = restTemplate.exchange(url + "/{id}", HttpMethod.DELETE, null, ResponseEntity, organizationId, pageAttachments.get(0).id)

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

    def "batchDelete"() {
        given:
        '准备'
        List<Long> ids = new ArrayList<>()
        ids.add(pageAttachmentId)
        when:
        '页面批量删除附件'
        HttpEntity<List<Long>> httpEntity = new HttpEntity<>(ids)
        def entity = restTemplate.exchange(url + "/batch_delete", HttpMethod.POST, httpEntity, ResponseEntity, organizationId)

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

    def "uploadForAddress"() {
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
        '上传附件，直接返回地址'
        ParameterizedTypeReference<List<String>> typeRef = new ParameterizedTypeReference<List<String>>() {
        }
        HttpEntity<MultiValueMap<String, Object>> files = new HttpEntity<>(form, headers)
        def entity = restTemplate.exchange(url + "/upload_for_address", HttpMethod.POST, files, typeRef, organizationId)

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