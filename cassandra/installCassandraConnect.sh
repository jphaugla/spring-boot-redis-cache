curl -X GET "https://downloads.datastax.com/kafka/kafka-connect-cassandra-sink-1.4.0.tar.gz" -o temp/kafka-connect-cassandra-sink-1.4.0.tar.gz
tar -xvzf temp/kafka-connect-cassandra-sink-1.4.0.tar.gz -C temp
docker cp temp/kafka-connect-cassandra-sink-1.4.0/kafka-connect-cassandra-sink-1.4.0.jar connect:/usr/share/confluent-hub-components
