package com.radovan.play.repositories.impl;

import com.radovan.play.entity.OrderItemEntity;
import com.radovan.play.repositories.OrderItemRepository;
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
public class OrderItemRepositoryImpl implements OrderItemRepository {

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
    public Optional<OrderItemEntity> findById(Integer itemId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderItemEntity> query = cb.createQuery(OrderItemEntity.class);
            Root<OrderItemEntity> root = query.from(OrderItemEntity.class);
            query.where(cb.equal(root.get("orderItemId"), itemId));
            List<OrderItemEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    @Override
    public OrderItemEntity save(OrderItemEntity orderItemEntity) {
        return withSession(session -> {
            if (orderItemEntity.getOrderItemId() == null) {
                session.persist(orderItemEntity);
            } else {
                session.merge(orderItemEntity);
            }
            session.flush();
            return orderItemEntity;
        });
    }

    @Override
    public List<OrderItemEntity> findAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderItemEntity> query = cb.createQuery(OrderItemEntity.class);
            Root<OrderItemEntity> root = query.from(OrderItemEntity.class);
            query.select(root);
            return session.createQuery(query).getResultList();
        });
    }

    @Override
    public List<OrderItemEntity> findAllByOrderId(Integer orderId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderItemEntity> cq = cb.createQuery(OrderItemEntity.class);
            Root<OrderItemEntity> root = cq.from(OrderItemEntity.class);
            root.join("order");
            cq.where(cb.equal(root.get("order").get("orderId"), orderId));

            cq.select(root);
            return session.createQuery(cq).getResultList();
        });
    }
}