package com.radovan.play.repositories.impl;

import com.radovan.play.entity.OrderEntity;
import com.radovan.play.repositories.OrderRepository;
import com.radovan.play.services.PrometheusService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Singleton
public class OrderRepositoryImpl implements OrderRepository {

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
    public Optional<OrderEntity> findById(Integer orderId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderEntity> query = cb.createQuery(OrderEntity.class);
            Root<OrderEntity> root = query.from(OrderEntity.class);
            query.where(cb.equal(root.get("orderId"), orderId));
            List<OrderEntity> results = session.createQuery(query).getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        });
    }

    @Override
    public OrderEntity save(OrderEntity orderEntity) {
        return withSession(session -> {
            if(orderEntity.getOrderId()==null){
                session.persist(orderEntity);
            }else{
                session.merge(orderEntity);
            }
            session.flush();
            return orderEntity;
        });
    }

    @Override
    public List<OrderEntity> findAll() {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderEntity> query = cb.createQuery(OrderEntity.class);
            Root<OrderEntity> root = query.from(OrderEntity.class);
            query.select(root);
            return session.createQuery(query).getResultList();
        });
    }

    @Override
    public List<OrderEntity> findAllByCartId(Integer cartId) {
        return withSession(session -> {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<OrderEntity> cq = cb.createQuery(OrderEntity.class);
            Root<OrderEntity> root = cq.from(OrderEntity.class);

            Predicate byCartId = cb.equal(root.get("cartId"), cartId);
            cq.where(byCartId);
            cq.select(root);

            return session.createQuery(cq).getResultList();
        });
    }




    @Override
    public void deleteById(Integer orderId) {
        withSession(session -> {
            OrderEntity orderEntity = session.get(OrderEntity.class, orderId);
            if (orderEntity != null) {
                session.remove(orderEntity);
            }
            return null;
        });
    }




}