package com.wooni.elk.keywords.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.wooni.elk.common.Const.INDEX_CREATE_SUCCESS;
import static com.wooni.elk.common.Const.INDEX_NAME;

@Service
@RequiredArgsConstructor
public class IndexInitializerService {
    private static final Logger log = LogManager.getLogger(IndexInitializerService.class);

    private final ElasticsearchClient client;

    @PostConstruct
    public void createIndex() throws IOException {
        boolean exists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        if (exists) {
            return;
        }

        String json = """
        {
          "settings": {
            "number_of_shards": 3,
            "number_of_replicas": 1,
            "analysis": {
              "tokenizer": {
                "nori_tokenizer": {
                  "type": "nori_tokenizer",
                  "decompound_mode": "mixed"
                }
              },
              "filter": {
                "nori_pos_filter": {
                  "type": "nori_part_of_speech",
                  "stoptags": [
                    "E","IC","J","MAG","MAJ","MM","NA","NR","NP","SC","SE",
                    "SF","SH","SL","SN","VV","VA","VX","EF","EC","ETM","ETN"
                  ]
                }
              },
              "analyzer": {
                "nori_analyzer": {
                  "type": "custom",
                  "tokenizer": "nori_tokenizer",
                  "filter": ["lowercase","nori_pos_filter"]
                }
              }
            }
          },
          "mappings": {
            "properties": {
              "searchKeyword": {
                "type": "text",
                "analyzer": "nori_analyzer",
                "fielddata": true
              },
              "searchKeywordNori": {
                "type": "keyword"
              },
              "searchDate": {
                "type": "date",
                "format": "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'||yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis"
              }
            }
          }
        }
        """;

        InputStream jsonStream = new ByteArrayInputStream(
                json.getBytes(StandardCharsets.UTF_8)
        );

        CreateIndexRequest request = CreateIndexRequest.of(builder -> builder
                .index(INDEX_NAME)
                .withJson(jsonStream)
        );

        CreateIndexResponse response = client.indices().create(request);
        if (response.acknowledged()) {
            log.info(INDEX_CREATE_SUCCESS);
        }
    }
}