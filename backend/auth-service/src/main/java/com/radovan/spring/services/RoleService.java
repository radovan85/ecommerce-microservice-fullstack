package com.radovan.spring.services;

import java.util.List;

import com.radovan.spring.dto.RoleDto;

public interface RoleService {

    List<RoleDto> listAllByUserId(Integer userId);
}
