package com.radovan.spring.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.radovan.spring.entity.CustomerEntity;

@Repository
public interface CustomerRepository  {

	Optional<CustomerEntity> findByUserId(Integer userId);

	Optional<CustomerEntity> findById(Integer customerId);

	List<CustomerEntity> findAll();

	void deleteById(Integer customerId);

	CustomerEntity save(CustomerEntity customerEntity);

}