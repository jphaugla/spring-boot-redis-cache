package com.cassandra.services;

import java.util.List;
import java.util.UUID;

import com.cassandra.models.Customer;
import org.springframework.data.cassandra.repository.AllowFiltering;

public interface CustomerService {
	public List<Customer> getAll();

	public Customer add(Customer customer);

	public Customer update(Customer customer);

	void evictCache();

	public void delete(UUID id);

	public Customer getCustomerById(UUID id);
}
