bash -c "curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/wiener_linien/departures/_bulk?pretty' --data-binary @C:/Users/admin/Desktop/WL/export/realTimeTempFile.json"
EXIT