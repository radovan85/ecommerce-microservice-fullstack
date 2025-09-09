package com.radovan.play.repositories.impl;

import com.radovan.play.entity.OrderAddressEntity;
import com.radovan.play.repositories.OrderAddressRepository;
import com.radovan.play.services.PrometheusService;
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
public class OrderAddressRepositoryImpl implements OrderAddressRepository {

    private SessionFactory sessionFactory;
    private PrometheusService prometheusService;

    @Inject
    private void initialize(SessionFactory sessionFactory, PrometheusService prometheusService) {
        this.sessionFactory = sessionFactory;
        this.prometheusService = prometheusService;
    }

    // Generic method for handling transactions with SessionFactory
    private <T> T withSession(Function<Session, T> function) {
        prometheusService.updateDatabaseQueryCount();
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
    public Optional<OrderAddressEntity> findById(Integer addressId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderAddressEntity> query = cb.createQuery(OrderAddressEntity.class);
            Root<OrderAddressEntity> root = query.from(OrderAddressEntity.class);
            query.where(cb.equal(root.get("orderAddressId"), addressId));
            List<OrderAddressEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    @Override
    public OrderAddressEntity save(OrderAddressEntity addressEntity) {
        return withSession(session -> {
            if (addressEntity.getOrderAddressId() == null) {
                session.persist(addressEntity);
            } else {
                session.merge(addressEntity);
            }
            session.flush();
            return addressEntity;
        });

    }

    @Override
    public List<OrderAddressEntity> findAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderAddressEntity> query = cb.createQuery(OrderAddressEntity.class);
            Root<OrderAddressEntity> root = query.from(OrderAddressEntity.class);
            query.select(root);
            return session.createQuery(query).getResultList();
        });
    }
}
