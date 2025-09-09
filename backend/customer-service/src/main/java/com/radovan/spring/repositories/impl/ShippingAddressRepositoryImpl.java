package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.ShippingAddressEntity;
import com.radovan.spring.repositories.ShippingAddressRepository;
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
public class ShippingAddressRepositoryImpl implements ShippingAddressRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;


    @Override
    public Optional<ShippingAddressEntity> findById(Integer addressId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ShippingAddressEntity> query = cb.createQuery(ShippingAddressEntity.class);
        Root<ShippingAddressEntity> root = query.from(ShippingAddressEntity.class);

        Predicate predicate = cb.equal(root.get("shippingAddressId"), addressId);
        query.where(predicate);

        List<ShippingAddressEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public ShippingAddressEntity save(ShippingAddressEntity addressEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (addressEntity.getShippingAddressId() == null) {
            entityManager.persist(addressEntity);
        } else {
            entityManager.merge(addressEntity);
        }

        entityManager.flush();
        return addressEntity;
    }

    @Override
    public List<ShippingAddressEntity> findAll() {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ShippingAddressEntity> query = cb.createQuery(ShippingAddressEntity.class);
        Root<ShippingAddressEntity> root = query.from(ShippingAddressEntity.class);

        query.select(root);

        return entityManager.createQuery(query).getResultList();
    }
}
