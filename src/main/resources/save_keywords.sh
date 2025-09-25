##!/bin/bash
#
#for i in {1..1000}
#do
#  keyword="검색어_$((RANDOM % 300))"
#  curl -s -X POST http://localhost:8080/api/keywords/save \
#       -H "Content-Type: application/json" \
#       -d "{\"keyword\": \"$keyword\"}" > /dev/null
#done
#
#echo "✅ 1000개의 검색어 데이터를 성공적으로 전송했습니다."
