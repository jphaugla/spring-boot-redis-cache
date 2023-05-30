package com.coderkan.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.coderkan.models.Customer;
import com.coderkan.repositories.CustomerRepository;
import com.coderkan.services.CustomerService;

@Service
@CacheConfig(cacheNames = "customerCache")
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;

	@Cacheable(cacheNames = "customers")
	@Override
	public List<Customer> getAll() {
		waitSomeTime();
		return this.customerRepository.findAll();
	}

	@CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")
	@Override
	public Customer add(Customer customer) {
		return this.customerRepository.save(customer);
	}

	//  this causes all the entries to be deleted if any entries are updated
	// @CacheEvict(cacheNames = "customers", allEntries = true)
	//   this works but is kind of complex.  Here customer is the java class object (not customers)
	@CacheEvict(cacheNames = "customers", key="#customer?.id", condition="#customer?.id!=null")
	//  this seems logical, but it doesn't delete the redis cached record
	// @CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")
	@Override
	public Customer update(Customer customer) {
		Optional<Customer> optCustomer = this.customerRepository.findById(customer.getId());
		if (!optCustomer.isPresent())
			return null;
		Customer repCustomer = optCustomer.get();
		repCustomer.setName(customer.getName());
		repCustomer.setContactName(customer.getContactName());
		repCustomer.setAddress(customer.getAddress());
		repCustomer.setCity(customer.getCity());
		repCustomer.setPostalCode(customer.getPostalCode());
		repCustomer.setCountry(customer.getCountry());
		return this.customerRepository.save(repCustomer);
	}

	@Caching(evict = { @CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")})
	@Override
	public void delete(long id) {
		if(this.customerRepository.existsById(id)) {
			this.customerRepository.deleteById(id);
		}
	}

	@Cacheable(cacheNames = "customers", key = "#id", unless = "#result == null")
	@Override
	public Customer getCustomerById(long id) {
		waitSomeTime();
		return this.customerRepository.findById(id).orElse(null);
	}

	private void waitSomeTime() {
		System.out.println("Long Wait Begin");
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Long Wait End");
	}

}