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

CREATE TABLE if not exists pageviews (
    userid text,
    pageid text,
    viewtime timestamp,
    PRIMARY KEY ((userid),pageid)
); 
