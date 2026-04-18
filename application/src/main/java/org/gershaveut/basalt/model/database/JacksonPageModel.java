package org.gershaveut.basalt.model.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;

import java.util.List;
import java.util.Map;

public class JacksonPageModel<T> extends PagedModel<T> {
    @JsonCreator
    public JacksonPageModel(@JsonProperty("content") List<T> content, @JsonProperty("page") Map<String, Object> page) {
        super(new PageImpl<>(content, PageRequest.of((int) page.getOrDefault("number", 0), (int) page.getOrDefault("size", 10)), Long.parseLong(page.getOrDefault("totalElements", 0).toString())));
    }
}
