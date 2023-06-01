package com.cassandra.repositories;

import org.springframework.data.cassandra.repository.CassandraRepository;

import com.cassandra.models.Customer;

import java.util.UUID;

public interface CustomerRepository extends CassandraRepository<Customer, UUID> {

}

