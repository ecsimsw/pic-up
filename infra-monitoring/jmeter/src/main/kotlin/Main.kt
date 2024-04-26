package org.example

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.entity.ContentType
import us.abstracta.jmeter.javadsl.JmeterDsl.*

fun main() {
    val objectMapper = ObjectMapper()

    objectMapper.writeValueAsString(
        MemberSignInRequestBody("ecsimsw", "password")
    )

    val stats = testPlan(threadGroup(2, 10,
        httpSampler("www.ecsimsw.com/api/member/signin")
            .protocol("https")
            .port(8082)
            .post("{\"name\": \"test\"}", ContentType.APPLICATION_JSON)
    )).run()
}

class MemberSignInRequestBody(
    username:String,
    password:String
)