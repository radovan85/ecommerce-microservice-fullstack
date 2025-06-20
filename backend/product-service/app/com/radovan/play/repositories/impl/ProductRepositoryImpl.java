package com.radovan.play.repositories.impl;

import com.radovan.play.entity.ProductEntity;
import com.radovan.play.repositories.ProductRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ProductRepositoryImpl implements ProductRepository {

    private SessionFactory sessionFactory;

    @Inject
    private void initialize(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    // Generic method for handling transactions with SessionFactory
    private <T> T withSession(Function<Session, T> function) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T result = function.apply(session);
                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    @Override
    public List<ProductEntity> listAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
            Root<ProductEntity> root = cq.from(ProductEntity.class);
            cq.select(root);
            return session.createQuery(cq).getResultList();
        });
    }

    @Override
    public ProductEntity save(ProductEntity productEntity) {
        return withSession(session -> {
            if (productEntity.getProductId() == null) {
                session.persist(productEntity);
            } else {
                session.merge(productEntity);
            }
            session.flush();
            return productEntity;
        });
    }

    @Override
    public void deleteById(Integer productId) {
        withSession(session -> {
            ProductEntity productEntity = session.get(ProductEntity.class, productId);
            if (productEntity != null) {
                session.remove(productEntity);
            }
            return null;
        });
    }

    @Override
    public List<ProductEntity> listAllByCategoryId(Integer categoryId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
            Root<ProductEntity> root = cq.from(ProductEntity.class);

            // Join with productCategory entity
            root.join("productCategory");

            // Add predicate for categoryId
            cq.where(cb.equal(root.get("productCategory").get("productCategoryId"), categoryId));

            cq.select(root);
            return session.createQuery(cq).getResultList();
        });
    }

    @Override
    public Optional<ProductEntity> findById(Integer productId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
            Root<ProductEntity> root = cq.from(ProductEntity.class);
            cq.where(cb.equal(root.get("productId"), productId));
            List<ProductEntity> results = session.createQuery(cq).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
}
