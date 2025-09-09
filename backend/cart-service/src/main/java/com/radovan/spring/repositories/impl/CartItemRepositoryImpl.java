package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.CartItemEntity;
import com.radovan.spring.repositories.CartItemRepository;
import com.radovan.spring.services.PrometheusService;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CartItemRepositoryImpl implements CartItemRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;



    @Override
    public void deleteAllByCartId(Integer cartId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<CartItemEntity> delete = cb.createCriteriaDelete(CartItemEntity.class);
        Root<CartItemEntity> root = delete.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("cart").get("cartId"), cartId);
        delete.where(predicate);

        entityManager.createQuery(delete).executeUpdate();
    }

    @Override
    public void deleteAllByProductId(Integer productId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<CartItemEntity> delete = cb.createCriteriaDelete(CartItemEntity.class);
        Root<CartItemEntity> root = delete.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("productId"), productId);
        delete.where(predicate);

        entityManager.createQuery(delete).executeUpdate();
    }

    @Override
    public List<CartItemEntity> findAllByCartId(Integer cartId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CartItemEntity> cq = cb.createQuery(CartItemEntity.class);
        Root<CartItemEntity> root = cq.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("cart").get("cartId"), cartId);
        cq.where(predicate);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<CartItemEntity> findAllByProductId(Integer productId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CartItemEntity> cq = cb.createQuery(CartItemEntity.class);
        Root<CartItemEntity> root = cq.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("productId"), productId);
        cq.where(predicate);

        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Optional<CartItemEntity> findById(Integer itemId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CartItemEntity> cq = cb.createQuery(CartItemEntity.class);
        Root<CartItemEntity> root = cq.from(CartItemEntity.class);

        Predicate predicate = cb.equal(root.get("cartItemId"), itemId);
        cq.where(predicate);

        List<CartItemEntity> results = entityManager.createQuery(cq).getResultList();
        return results.stream().findFirst();
    }

    @Override
    public void deleteById(Integer itemId) {
        prometheusService.updateDatabaseQueryCount();

        CartItemEntity itemEntity = entityManager.find(CartItemEntity.class, itemId);
        if (itemEntity != null) {
            entityManager.remove(itemEntity);
        }
    }

    @Override
    public CartItemEntity save(CartItemEntity itemEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (itemEntity.getCartItemId() != null) {
            itemEntity = entityManager.merge(itemEntity);
        } else {
            entityManager.persist(itemEntity);
        }
        entityManager.flush();
        return itemEntity;
    }
}

