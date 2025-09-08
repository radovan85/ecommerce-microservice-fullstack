package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.CustomerEntity;
import com.radovan.spring.repositories.CustomerRepository;
import com.radovan.spring.services.PrometheusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CustomerRepositoryImpl implements CustomerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;

    @Override
    public Optional<CustomerEntity> findByUserId(Integer userId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CustomerEntity> query = cb.createQuery(CustomerEntity.class);
        Root<CustomerEntity> root = query.from(CustomerEntity.class);

        Predicate predicate = cb.equal(root.get("userId"), userId);
        query.where(predicate);

        List<CustomerEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<CustomerEntity> findById(Integer customerId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CustomerEntity> query = cb.createQuery(CustomerEntity.class);
        Root<CustomerEntity> root = query.from(CustomerEntity.class);

        Predicate predicate = cb.equal(root.get("customerId"), customerId);
        query.where(predicate);

        List<CustomerEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<CustomerEntity> findAll() {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CustomerEntity> query = cb.createQuery(CustomerEntity.class);
        Root<CustomerEntity> root = query.from(CustomerEntity.class);

        query.select(root);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void deleteById(Integer customerId) {
        prometheusService.updateDatabaseQueryCount();
        findById(customerId).ifPresent(entityManager::remove);
    }

    @Override
    public CustomerEntity save(CustomerEntity customerEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (customerEntity.getCustomerId() == null) {
            entityManager.persist(customerEntity);
        } else {
            entityManager.merge(customerEntity);
        }

        entityManager.flush();
        return customerEntity;
    }
}
