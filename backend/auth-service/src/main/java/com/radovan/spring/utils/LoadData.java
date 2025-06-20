package com.radovan.spring.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.radovan.spring.entity.RoleEntity;
import com.radovan.spring.entity.UserEntity;
import com.radovan.spring.repositories.RoleRepository;
import com.radovan.spring.repositories.UserRepository;

@Component
public class LoadData {

	private RoleRepository roleRepository;

	private UserRepository userRepository;

	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	public void initialize(RoleRepository roleRepository, UserRepository userRepository,
			BCryptPasswordEncoder passwordEncoder) {

		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		addRolesData();
		addAdminData();
	}

	public void addRolesData() {
		RoleEntity roleAdmin = roleRepository.findByRole("ROLE_ADMIN").orElse(null);
		RoleEntity roleUser = roleRepository.findByRole("ROLE_USER").orElse(null);

		if (roleAdmin == null) {
			roleRepository.save(new RoleEntity("ROLE_ADMIN"));
		}

		if (roleUser == null) {
			roleRepository.save(new RoleEntity("ROLE_USER"));
		}

	}

	public void addAdminData() {

		Optional<RoleEntity> roleOptional = roleRepository.findByRole("ROLE_ADMIN");
		if (roleOptional.isPresent()) {
			RoleEntity roleEntity = roleOptional.get();
			List<RoleEntity> roles = new ArrayList<RoleEntity>();
			roles.add(roleEntity);
			UserEntity adminEntity = new UserEntity("John", "Doe", "doe@luv2code.com", "admin123", (byte) 1);
			adminEntity.setPassword(passwordEncoder.encode(adminEntity.getPassword()));

			adminEntity.setRoles(roles);
			try {
				UserEntity storedAdmin = userRepository.save(adminEntity);

				List<UserEntity> users = new ArrayList<UserEntity>();
				users.add(storedAdmin);
				roleEntity.setUsers(users);
				roleRepository.saveAndFlush(roleEntity);
			} catch (Exception exc) {
				System.out.println("Admin already added");
			}
		}
	}
}