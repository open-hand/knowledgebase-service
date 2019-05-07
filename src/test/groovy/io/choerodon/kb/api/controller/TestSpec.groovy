package io.choerodon.kb.api.controller

import io.choerodon.kb.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

/**
 * Created by Zenger on 2019/4/30.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class TestSpec extends Specification {

    def '创建用户'() {
        given: '定义请求数据格式'

        and: 'Mock'

        when: '模拟发送消息'

        then: '校验返回数据'

    }
}
