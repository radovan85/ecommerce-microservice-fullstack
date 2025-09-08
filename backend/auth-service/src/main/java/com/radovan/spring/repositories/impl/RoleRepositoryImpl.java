package com.radovan.spring.repositories.impl;

import com.radovan.spring.entity.RoleEntity;
import com.radovan.spring.repositories.RoleRepository;
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
public class RoleRepositoryImpl implements RoleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PrometheusService prometheusService;

    @Override
    public Optional<RoleEntity> findByRole(String role) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RoleEntity> query = cb.createQuery(RoleEntity.class);
        Root<RoleEntity> root = query.from(RoleEntity.class);

        Predicate predicate = cb.equal(root.get("role"), role);
        query.where(predicate);

        List<RoleEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }

    @Override
    public List<RoleEntity> findAllByUserId(Integer userId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RoleEntity> query = cb.createQuery(RoleEntity.class);
        Root<RoleEntity> root = query.from(RoleEntity.class);

        var usersJoin = root.join("users");
        Predicate predicate = cb.equal(usersJoin.get("id"), userId);

        query.select(root).where(predicate);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public RoleEntity save(RoleEntity roleEntity) {
        prometheusService.updateDatabaseQueryCount();

        if (roleEntity.getId() == null) {
            entityManager.persist(roleEntity);
        } else {
            entityManager.merge(roleEntity);
        }

        entityManager.flush();
        return roleEntity;
    }

    @Override
    public Optional<RoleEntity> findById(Integer roleId) {
        prometheusService.updateDatabaseQueryCount();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<RoleEntity> query = cb.createQuery(RoleEntity.class);
        Root<RoleEntity> root = query.from(RoleEntity.class);

        Predicate predicate = cb.equal(root.get("id"), roleId);
        query.where(predicate);

        List<RoleEntity> result = entityManager.createQuery(query).getResultList();
        return result.stream().findFirst();
    }
}
