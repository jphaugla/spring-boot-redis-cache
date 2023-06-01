package com.cassandra.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import com.cassandra.models.Customer;
import com.cassandra.repositories.CustomerRepository;
import com.cassandra.services.CustomerService;

@Slf4j
@Observed(name="CusterServiceImpl")
@Service
@CacheConfig(cacheNames = "customers")
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private ObservationRegistry observationRegistry;

	public String sayHello() {
		return Observation
				.createNotStarted("CustomerService", observationRegistry)
				.observe(this::sayHelloNoObserver);
	}

	private String sayHelloNoObserver() {
		return "Hello World!";
	}
	@Cacheable
	@Override
	public List<Customer> getAll() {
		waitSomeTime();
		return this.customerRepository.findAll();
	}

	// @CacheEvict(key = "#id", condition = "#id!=null")
	//  Switching to a CachePut from a CacheEvict
	@CachePut(key = "#customer.id")
	@Override
	public Customer add(Customer customer) {
		log.info(" write to database");
		return this.customerRepository.save(customer);
	}

	//  this causes all the entries to be deleted if any entries are updated
	// @CacheEvict(cacheNames = "customers", allEntries = true)
	//   this works but is kind of complex.  Here customer is the java class object (not customers)
	// @CacheEvict(key="#customer?.id", condition="#customer?.id!=null")
	//  this seems logical, but it doesn't delete the redis cached record
	// @CacheEvict(cacheNames = "customers", key = "#id", condition = "#id!=null")
	@CachePut(key = "#customer.id")
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
	@CacheEvict(allEntries = true)
	@Override
	public void evictCache() {
		log.info("all entries have been evicted");
	}

	@Caching(evict = { @CacheEvict(key = "#id", condition = "#id!=null")})
	@Override
	public void delete(UUID id) {
		if(this.customerRepository.existsById(id)) {
			this.customerRepository.deleteById(id);
		}
	}

	@Cacheable(key = "#id", unless = "#result == null")
	@Override
	public Customer getCustomerById(UUID id) {
		waitSomeTime();
		return this.customerRepository.findById(id).orElse(null);
	}

	private void waitSomeTime() {
		log.info("Long Wait Begin");
		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		log.info("Long Wait End");
	}

}