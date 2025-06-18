package com.radovan.spring.converter;

import com.radovan.spring.dto.RoleDto;
import com.radovan.spring.dto.UserDto;
import com.radovan.spring.entity.RoleEntity;
import com.radovan.spring.entity.UserEntity;
import com.radovan.spring.repositories.RoleRepository;
import com.radovan.spring.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class TempConverter {

    private RoleRepository roleRepository;
    private ModelMapper mapper;
    private UserRepository userRepository;

    @Autowired
    private void initialize(RoleRepository roleRepository, ModelMapper mapper, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public RoleDto roleEntityToDto(RoleEntity role) {
        RoleDto returnValue = mapper.map(role, RoleDto.class);
        List<Integer> usersIds = new ArrayList<Integer>();
        Optional<List<UserEntity>> usersOptional = Optional.ofNullable(role.getUsers());
        if (!usersOptional.isEmpty()) {
            usersOptional.get().forEach((userEntity) -> {
                usersIds.add(userEntity.getId());
            });
        }
        returnValue.setUsersIds(usersIds);
        return returnValue;
    }


    public RoleEntity roleDtoToEntity(RoleDto role) {
        RoleEntity returnValue = mapper.map(role, RoleEntity.class);
        List<UserEntity> users = new ArrayList<UserEntity>();
        Optional<List<Integer>> usersIdsOptional = Optional.ofNullable(role.getUsersIds());
        if (!usersIdsOptional.isEmpty()) {
            usersIdsOptional.get().forEach((userId) -> {
                UserEntity userEntity = userRepository.findById(userId).orElse(null);
                if (userEntity != null) {
                    users.add(userEntity);
                }
            });
        }

        returnValue.setUsers(users);
        return returnValue;
    }

    public UserDto userEntityToDto(UserEntity user) {
        UserDto returnValue = mapper.map(user, UserDto.class);
        List<Integer> rolesIds = new ArrayList<Integer>();
        Optional<List<RoleEntity>> rolesOptional = Optional.ofNullable(user.getRoles());
        if (rolesOptional.isPresent()) {
            rolesIds = rolesOptional.get().stream().map(RoleEntity::getId).collect(Collectors.toList());
        }

        returnValue.setRolesIds(rolesIds);

        Optional<Byte> enabledOptional = Optional.ofNullable(user.getEnabled());
        if (enabledOptional.isPresent()) {
            returnValue.setEnabled(enabledOptional.get().shortValue());
        }
        return returnValue;
    }


    public UserEntity userDtoToEntity(UserDto user) {
        UserEntity returnValue = mapper.map(user, UserEntity.class);
        List<RoleEntity> roles = new ArrayList<RoleEntity>();
        Optional<List<Integer>> rolesIdsOptional = Optional.ofNullable(user.getRolesIds());
        if (!rolesIdsOptional.isEmpty()) {
            rolesIdsOptional.get().forEach((roleId) -> {
                RoleEntity roleEntity = roleRepository.findById(roleId).orElse(null);
                if (roleEntity != null) {
                    roles.add(roleEntity);
                }
            });
        }

        returnValue.setRoles(roles);

        Optional<Short> enabledOptional = Optional.ofNullable(user.getEnabled());
        if (enabledOptional.isPresent()) {
            returnValue.setEnabled(enabledOptional.get().byteValue());
        }
        return returnValue;
    }
}
