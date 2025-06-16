package com.radovan.spring.services.impl;

import com.radovan.spring.converter.TempConverter;
import com.radovan.spring.dto.UserDto;
import com.radovan.spring.entity.RoleEntity;
import com.radovan.spring.entity.UserEntity;
import com.radovan.spring.exceptions.ExistingInstanceException;
import com.radovan.spring.exceptions.InstanceUndefinedException;
import com.radovan.spring.exceptions.OperationNotAllowedException;
import com.radovan.spring.repositories.RoleRepository;
import com.radovan.spring.repositories.UserRepository;
import com.radovan.spring.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
	private UserRepository userRepository;
	private RoleRepository roleRepository;
	private TempConverter tempConverter;
	private BCryptPasswordEncoder passwordEncoder;
	private AuthenticationManager authenticationManager;

	@Autowired
	private void initialize(UserRepository userRepository, RoleRepository roleRepository, TempConverter tempConverter,
			BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.tempConverter = tempConverter;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserDto> listAll() {
		List<UserEntity> allUsers = userRepository.findAll();
		return allUsers.stream().map(tempConverter::userEntityToDto).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getCurrentUser() {
		UserDto returnValue = null;
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			String currentUsername = authentication.getName();
			Optional<UserEntity> userOptional = userRepository.findByEmail(currentUsername);
			if (userOptional.isPresent()) {
				returnValue = tempConverter.userEntityToDto(userOptional.get());
			} else {
				Error error = new Error("Invalid user!");
				throw new InstanceUndefinedException(error);
			}
		} else {
			Error error = new Error("Invalid user!");
			throw new InstanceUndefinedException(error);
		}

		return returnValue;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getUserById(Integer userId) {
		UserEntity userEntity = userRepository.findById(userId)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The user has not been found!")));
		return tempConverter.userEntityToDto(userEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDto getUserByEmail(String email) {
		UserEntity userEntity = userRepository.findByEmail(email)
				.orElseThrow(() -> new InstanceUndefinedException(new Error("Invalid user!")));
		return tempConverter.userEntityToDto(userEntity);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Authentication> authenticateUser(String username, String password) {
		UsernamePasswordAuthenticationToken authReq = new UsernamePasswordAuthenticationToken(username, password);
		Optional<UserEntity> userOptional = userRepository.findByEmail(username);
		return userOptional.flatMap(user -> {
			try {
				Authentication auth = authenticationManager.authenticate(authReq);
				return Optional.of(auth);
			} catch (AuthenticationException e) {
				// Handle authentication failure
				return Optional.empty();
			}
		});
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean isAdmin() {
		Boolean returnValue = false;
		UserDto currentUser = getCurrentUser();
		RoleEntity roleAdmin = roleRepository.findByRole("ROLE_ADMIN").orElse(null);
		if (roleAdmin != null) {
			List<Integer> rolesIds = currentUser.getRolesIds();
			if (rolesIds.contains(roleAdmin.getId())) {
				returnValue = true;
			}
		}

		return returnValue;
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean isAdmin(Integer userId) {
		Boolean returnValue = false;
		UserDto user = getUserById(userId);
		RoleEntity roleAdmin = roleRepository.findByRole("ROLE_ADMIN").orElse(null);
		if (roleAdmin != null) {
			List<Integer> rolesIds = user.getRolesIds();
			if (rolesIds.contains(roleAdmin.getId())) {
				returnValue = true;
			}
		}

		return returnValue;
	}

	@Override
	@Transactional
	public void suspendUser(Integer userId) {
		UserDto user = getUserById(userId);
		if (isAdmin(user.getId())) {
			throw new OperationNotAllowedException(
					new Error("This operation is not allowed!The user has Admin authority!"));
		}
		user.setEnabled((short) 0);
		userRepository.saveAndFlush(tempConverter.userDtoToEntity(user));
	}

	@Override
	@Transactional
	public void reactivateUser(Integer userId) {
		UserDto user = getUserById(userId);
		if (isAdmin(user.getId())) {
			throw new OperationNotAllowedException(
					new Error("This operation is not allowed!The user has Admin authority!"));
		}
		user.setEnabled((short) 1);
		userRepository.saveAndFlush(tempConverter.userDtoToEntity(user));
	}

	@Override
	@Transactional
	public UserDto addUser(UserDto user) {
		Optional<UserEntity> userOptional = userRepository.findByEmail(user.getEmail());
		if (userOptional.isPresent()) {
			throw new ExistingInstanceException(new Error("This email exists already!"));
		}
		RoleEntity roleEntity = roleRepository.findByRole("ROLE_USER")
				.orElseThrow(() -> new InstanceUndefinedException(new Error("The role has not been found!")));
		List<RoleEntity> roles = new ArrayList<>();
		roles.add(roleEntity);
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		user.setEnabled((short) 1);
		UserEntity userEntity = tempConverter.userDtoToEntity(user);
		userEntity.setRoles(roles);
		UserEntity storedUser = userRepository.save(userEntity);
		List<UserEntity> users = roleEntity.getUsers();
		if (users == null) {
			users = new ArrayList<>();
		}
		users.add(storedUser);
		roleEntity.setUsers(users);
		roleRepository.save(roleEntity);
		return tempConverter.userEntityToDto(storedUser);
	}

	@Override
	@Transactional
	public void deleteUser(Integer userId) {
		getUserById(userId);
		if (isAdmin(userId)) {
			throw new OperationNotAllowedException(
					new Error("This operation is not allowed!The user has Admin authority!"));
		}
		userRepository.deleteById(userId);
		userRepository.flush();

	}

}