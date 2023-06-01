package com.cassandra.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cassandra.models.Customer;
import com.cassandra.services.CustomerService;



@RestController
@RequestMapping("/api")
public class CustomerController {
	@Autowired
	private CustomerService customerService;

	@GetMapping(value = "/customers", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> getAllCustomers() {
		List<Customer> customers = this.customerService.getAll();
		return ResponseEntity.ok(customers);
	}

	@GetMapping(value = "/customers/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<Object> getCustomerById(@PathVariable("id") String id) {
		UUID _id = UUID.fromString(id);
		Customer customer = this.customerService.getCustomerById(_id);
		return ResponseEntity.ok(customer);
	}
	@GetMapping(value = "/customers/evict-all")
	public void evictCache() {
		this.customerService.evictCache();
	}

	@PostMapping(value = "/customers")
	//  could have a separate bulk customer option not using the cache
	public ResponseEntity<Object> addCustomer(@RequestBody Customer customer) {
		Customer created = this.customerService.add(customer);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping(value = "/customers")
	public ResponseEntity<Object> updateCustomer(@RequestBody Customer customer) {
		Customer updated = this.customerService.update(customer);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping(value = "/customers/{id}")
	public ResponseEntity<Object> deleteCustomerById(@PathVariable("id") String id) {
		UUID _id = UUID.fromString(id);
		this.customerService.delete(_id);
		return ResponseEntity.ok().build();
	}
}
