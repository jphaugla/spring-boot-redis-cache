-- Here, you can execute any CQL commands, e.g.

CREATE KEYSPACE if not exists customer  WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};

use customer;

CREATE TABLE if not exists customer (
    id UUID,
    name text,
    contactName text,
    address text,
    city text,
    postalCode text,
    country text,
    PRIMARY KEY (id)
); 
