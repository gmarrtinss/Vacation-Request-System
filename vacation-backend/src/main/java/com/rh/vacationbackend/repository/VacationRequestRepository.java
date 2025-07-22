package com.rh.vacationbackend.repository;

import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.VacationRequest;
import com.rh.vacationbackend.model.VacationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VacationRequestRepository extends JpaRepository<VacationRequest, UUID> {

    List<VacationRequest> findByUser(Users user);
    List<VacationRequest> findByStatus(VacationRequestStatus status);
    List<VacationRequest> findByManager(Users manager);

}