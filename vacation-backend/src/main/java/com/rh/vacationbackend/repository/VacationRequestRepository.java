package com.rh.vacationbackend.repository;

import com.rh.vacationbackend.model.Employee;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.model.VacationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, UUID> {

    List<VacationRequest> findByEmployee(Employee employee);
    List<VacationRequest> findByStatus(VacationRequestStatus status);
    List<VacationRequest> findByManager(Employee manager);
}