package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.CartEntity;
import com.radovan.spring.entity.CartItemEntity;
import com.radovan.spring.repositories.CartRepository;
import com.radovan.spring.services.PrometheusService;
import jakarta.persistence.*;
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
public class CartRepositoryImpl implements CartRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;



    @Override
    public Optional<Float> calculateCartPrice(Integer cartId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Float> cq = cb.createQuery(Float.class);
        Root<CartItemEntity> root = cq.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("cart").get("cartId"), cartId);
        cq.select(cb.sum(root.get("price"))).where(predicate);

        try {
            Float result = entityManager.createQuery(cq).getSingleResult();
            return Optional.ofNullable(result);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CartEntity> findById(Integer cartId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CartEntity> query = cb.createQuery(CartEntity.class);
        Root<CartEntity> root = query.from(CartEntity.class);

        Predicate predicate = cb.equal(root.get("cartId"), cartId);
        query.where(predicate);

        List<CartEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public CartEntity save(CartEntity cartEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (cartEntity.getCartId() != null) {
            cartEntity = entityManager.merge(cartEntity);
        } else {
            entityManager.persist(cartEntity);
        }
        entityManager.flush();
        return cartEntity;
    }

    @Override
    public List<CartEntity> findAll() {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CartEntity> cq = cb.createQuery(CartEntity.class);
        Root<CartEntity> root = cq.from(CartEntity.class);
        cq.select(root);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public void deleteById(Integer cartId) {
        prometheusService.updateDatabaseQueryCount();

        CartEntity cartEntity = entityManager.find(CartEntity.class, cartId);
        if (cartEntity != null) {
            entityManager.remove(cartEntity);
        }
    }
}

