package com.radovan.play.repositories.impl;

import com.radovan.play.entity.ProductImageEntity;
import com.radovan.play.repositories.ProductImageRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class ProductImageRepositoryImpl implements ProductImageRepository {

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
    public ProductImageEntity save(ProductImageEntity imageEntity) {
        return withSession(session -> {
            if (imageEntity.getId() == null) {
                session.persist(imageEntity);
            } else {
                session.merge(imageEntity);
            }
            session.flush();
            return imageEntity;
        });
    }

    @Override
    public Optional<ProductImageEntity> findByProductId(Integer productId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductImageEntity> cq = cb.createQuery(ProductImageEntity.class);
            Root<ProductImageEntity> root = cq.from(ProductImageEntity.class);

            // Add predicate for productId
            cq.where(cb.equal(root.get("product").get("productId"), productId));

            cq.select(root);

            List<ProductImageEntity> results = session.createQuery(cq).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    @Override
    public List<ProductImageEntity> listAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductImageEntity> query = cb.createQuery(ProductImageEntity.class);
            Root<ProductImageEntity> root = query.from(ProductImageEntity.class);
            query.select(root);
            return session.createQuery(query).getResultList();
        });
    }

    @Override
    public void deleteById(Integer imageId) {
        withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaDelete<ProductImageEntity> deleteCriteria = cb.createCriteriaDelete(ProductImageEntity.class);
            Root<ProductImageEntity> root = deleteCriteria.from(ProductImageEntity.class);
            deleteCriteria.where(cb.equal(root.get("id"), imageId));
            session.createQuery(deleteCriteria).executeUpdate();
            return null;
        });
    }

    @Override
    public Optional<ProductImageEntity> findById(Integer imageId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<ProductImageEntity> query = cb.createQuery(ProductImageEntity.class);
            Root<ProductImageEntity> root = query.from(ProductImageEntity.class);
            query.where(cb.equal(root.get("id"), imageId));
            List<ProductImageEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }
}
