package com.rh.vacationbackend.repository;

import com.rh.vacationbackend.model.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByCpf(String cpf);
    Optional<Employee> findByEmail(String email);
    List<Employee> findByManager(Employee manager);

}
