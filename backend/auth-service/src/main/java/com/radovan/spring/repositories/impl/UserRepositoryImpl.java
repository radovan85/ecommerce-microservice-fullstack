package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.UserEntity;
import com.radovan.spring.repositories.UserRepository;
import com.radovan.spring.services.PrometheusService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserRepositoryImpl implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;

    @Override
    public List<UserEntity> findAll() {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        query.select(root);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        Predicate predicate = cb.equal(root.get("email"), email);
        query.where(predicate);

        List<UserEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public Optional<UserEntity> findById(Integer userId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        Predicate predicate = cb.equal(root.get("id"), userId);
        query.where(predicate);

        List<UserEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public UserEntity save(UserEntity userEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (userEntity.getId() == null) {
            entityManager.persist(userEntity);
        } else {
            entityManager.merge(userEntity);
        }

        entityManager.flush();
        return userEntity;
    }

    @Override
    public void deleteById(Integer userId) {
        prometheusService.updateDatabaseQueryCount();

        findById(userId).ifPresent(entityManager::remove);
    }
}
