package com.example.demo;

import com.restcountries.GetAll;
import org.openapitools.api.PostalCodeApi;
import org.openapitools.model.Country;
import org.openapitools.model.PostalCode;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@RestController
public class PostalCodeController implements PostalCodeApi {
    private final RestClient restClient;
    private final JdbcTemplate jdbcTemplate;

    public PostalCodeController(RestClient.Builder builder, JdbcTemplate jdbcTemplate) {
        this.restClient = builder.baseUrl("https://restcountries.com/v3.1").build();
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ResponseEntity<Void> addPostalCode(Country country) {
        if (country.getCode() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<GetAll> getAllList = restClient.get().uri("/alpha/{code}", country.getCode()).retrieve().body(new ParameterizedTypeReference<>() {});
        if (getAllList == null || getAllList.size() != 1) {
            return ResponseEntity.badRequest().build();
        }
        GetAll first = getAllList.getFirst();
        jdbcTemplate.update("INSERT INTO POSTAL_CODE VALUES(?,?,?,?)",
                first.getName().getCommon(),
                country.getCode(),
                first.getPostalCode().getFormat(),
                first.getPostalCode().getRegex());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PostalCode> getPostalCode(String countryCode) {
        List<PostalCode> postalCodes = jdbcTemplate.query("SELECT * FROM POSTAL_CODE WHERE COUNTRY_CODE = ?", new Object[]{countryCode}, new int[]{Types.CHAR}, this::mapRow);
        if (postalCodes.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(postalCodes.getFirst());
    }

    public PostalCode mapRow(ResultSet rs, int rowNum) throws SQLException {
        PostalCode postalCode = new PostalCode();
        postalCode.setCountryName(rs.getString(1));
        postalCode.setFormat(rs.getString(3));
        postalCode.setRegex(rs.getString(4));
        return postalCode;
    }

}
