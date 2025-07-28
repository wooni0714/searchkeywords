ARG VERSION
FROM docker.elastic.co/elasticsearch/elasticsearch:${VERSION}
RUN elasticsearch-plugin install --batch analysis-nori
