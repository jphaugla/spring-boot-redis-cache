#!/bin/bash
echo "######### Starting to execute SH script... #########"

# If you have credentials for your DB uncomment the following two lines
#USER_NAME='user_name'
#PASSWORD='user_password'

echo "######### Sleeping for 25 seconds #########"
sleep 25

# If you have credentials for your DB use: while ! cqlsh scylla -u "${USER_NAME}" -p "${PASSWORD}" -e 'describe cluster' ; do
while ! cqlsh cassandra1 -e 'describe cluster' ; do
     echo "######### Waiting for main instance to be ready... #########"
     sleep 5
done

for cql_file in ./cassandra_init/*.cql;
do
# If you have credentials on your db use this line cqlsh scylla -u "${USER_NAME}" -p "${PASSWORD}" -f "${cql_file}" ;
  cqlsh cassandra1 -f "${cql_file}" ;
  echo "######### Script ""${cql_file}"" executed!!! #########"
done
echo "######### Execution of SH script is finished! #########"
echo "######### Stopping temporary instance! #########"
exit;

