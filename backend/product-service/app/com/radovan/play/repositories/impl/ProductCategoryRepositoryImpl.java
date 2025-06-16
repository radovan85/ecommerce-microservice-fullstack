package com.radovan.play.repositories.impl;

import com.radovan.play.entity.ProductCategoryEntity;
import com.radovan.play.repositories.ProductCategoryRepository;
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
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

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
    public List<ProductCategoryEntity> listAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductCategoryEntity> query = cb.createQuery(ProductCategoryEntity.class);
            Root<ProductCategoryEntity> root = query.from(ProductCategoryEntity.class);
            query.select(root);
            return session.createQuery(query).getResultList();
        });
    }

    @Override
    public void deleteById(Integer categoryId) {
        withSession(session -> {
            ProductCategoryEntity category = session.get(ProductCategoryEntity.class, categoryId);
            if (category != null) {
                session.remove(category);
            }
            return null;
        });
    }

    @Override
    public Optional<ProductCategoryEntity> findById(Integer categoryId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductCategoryEntity> query = cb.createQuery(ProductCategoryEntity.class);
            Root<ProductCategoryEntity> root = query.from(ProductCategoryEntity.class);
            query.where(cb.equal(root.get("productCategoryId"), categoryId));
            List<ProductCategoryEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    @Override
    public ProductCategoryEntity save(ProductCategoryEntity categoryEntity) {
        return withSession(session -> {
            if (categoryEntity.getProductCategoryId() == null) {
                session.persist(categoryEntity);
            } else {
                session.merge(categoryEntity);
            }
            session.flush();
            return categoryEntity;
        });
    }

    @Override
    public ProductCategoryEntity saveAndFlush(ProductCategoryEntity categoryEntity) {
        return withSession(session -> {
            ProductCategoryEntity mergedEntity = session.merge(categoryEntity);
            session.flush();
            return mergedEntity;
        });
    }

    @Override
    public Optional<ProductCategoryEntity> findByName(String name) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductCategoryEntity> query = cb.createQuery(ProductCategoryEntity.class);
            Root<ProductCategoryEntity> root = query.from(ProductCategoryEntity.class);

            // Add predicate for name
            query.where(cb.equal(root.get("name"), name));

            query.select(root);

            List<ProductCategoryEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
}
