package com.coderkan.services;

import java.util.List;

import com.coderkan.models.Customer;
import org.springframework.cache.annotation.CacheEvict;

public interface CustomerService {

	public List<Customer> getAll();

	public Customer add(Customer customer);

	public Customer update(Customer customer);

	void evictCache();

	public void delete(long id);

	public Customer getCustomerById(long id);
}
