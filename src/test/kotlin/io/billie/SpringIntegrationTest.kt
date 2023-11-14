package io.billie

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest


@AutoConfigureMockMvc
// DN: Actually for better coverage I would test with random port and real http client But out of scope
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
open class SpringIntegrationTest {
    // DN: There are 2 options of independent test - clean scheme after every test OR write independent tests. I decided to write independent
    // Every test creates their environment
}