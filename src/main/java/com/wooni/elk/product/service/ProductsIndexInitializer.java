package com.wooni.elk.product.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.wooni.elk.common.Const;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class ProductsIndexInitializer {

    @Value("${elasticsearch.products.settings}")
    private Resource settingsRes;

    @Value("${elasticsearch.products.mapping}")
    private Resource mappingRes;

    private final ElasticsearchClient client;

    @PostConstruct
    public void init() throws IOException {
        if (client.indices().exists(e -> e.index(Const.PRODUCTS)).value()) return;

        String settingsJson = read(settingsRes);
        String mappingJson  = read(mappingRes);

        String payload = "{"
                + trimBraces(settingsJson) + ","
                + trimBraces(mappingJson)
                + "}";

        try (InputStream is = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8))) {
            client.indices().create(CreateIndexRequest.of(b -> b.index(Const.PRODUCTS).withJson(is)));
        }
    }

    private static String read(Resource r) throws IOException {
        try (InputStream in = r.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static String trimBraces(String json) {
        int len = json.length();
        return json.substring(1, len - 1).trim();
    }
}

