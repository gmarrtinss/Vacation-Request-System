package com.rh.vacationbackend.repository;

import com.rh.vacationbackend.model.Users;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface UsersRepository extends JpaRepository<Users, UUID> {

    Optional<Users> findByCpf(String cpf);
    Optional<Users> findByEmail(String email);
    List<Users> findByManager(Users manager);
    List<Users> findEmployeesByManager(Users manager);
}