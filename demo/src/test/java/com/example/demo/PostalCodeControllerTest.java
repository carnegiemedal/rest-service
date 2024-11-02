package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.model.Country;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureMockRestServiceServer
@Sql(scripts = "/script.sql")
class PostalCodeControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Test
    void addPostalCode() throws Exception {
        mockRestServiceServer.expect(MockRestRequestMatchers.requestTo("https://restcountries.com/v3.1/alpha/nl"))
                        .andRespond(MockRestResponseCreators.withSuccess(new ClassPathResource("/response.json"), MediaType.APPLICATION_JSON));
        mvc.perform(MockMvcRequestBuilders
                        .post("/postal-code")
                        .content(asJsonString(new Country("nl")))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getPostalCode() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/postal-code").queryParam("country_code", "NL")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.country_name").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.country_name").value("Nederland"));

    }

    @Test
    void getPostalCodeMissingCountryCode() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/postal-code")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}