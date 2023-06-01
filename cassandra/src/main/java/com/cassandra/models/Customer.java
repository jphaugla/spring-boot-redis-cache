package com.cassandra.models;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table
public class Customer {

	@PrimaryKey
	private UUID id = UUID.randomUUID();

	private String name;

	private String contactName;

	private String address;

	private String city;

	private String postalCode;

	private String country;

}
