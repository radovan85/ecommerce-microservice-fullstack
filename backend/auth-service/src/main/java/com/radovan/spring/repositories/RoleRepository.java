package com.radovan.spring.repositories;

import com.radovan.spring.entity.RoleEntity;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<RoleEntity> findByRole(String role);

    List<RoleEntity> findAllByUserId(Integer userId);

    RoleEntity save(RoleEntity roleEntity);

    Optional<RoleEntity> findById(Integer roleId);
}
