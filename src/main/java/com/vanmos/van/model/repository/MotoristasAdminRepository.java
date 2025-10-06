package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.MotoristasAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotoristasAdminRepository extends JpaRepository<MotoristasAdmin, Long> {
    
    Optional<MotoristasAdmin> findByGmail(String gmail);
    
    Optional<MotoristasAdmin> findByCpf(String cpf);
    
    @Query("SELECT m FROM MotoristasAdmin m WHERE m.gmail = :emailOuCpf OR m.cpf = :emailOuCpf")
    Optional<MotoristasAdmin> findByGmailOrCpf(@Param("emailOuCpf") String emailOuCpf);
}