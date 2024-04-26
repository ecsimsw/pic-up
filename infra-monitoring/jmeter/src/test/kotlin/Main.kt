import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.entity.ContentType
import org.junit.jupiter.api.Test
import us.abstracta.jmeter.javadsl.JmeterDsl.*

class CalculatorTest {

    val objectMapper = ObjectMapper()

    @Test
    fun signIn() {
        val stats = testPlan(threadGroup(2, 10,
            httpSampler("http://my.service")
                .post("{\"name\": \"test\"}", ContentType.APPLICATION_JSON)
        )).run();

    }
}

