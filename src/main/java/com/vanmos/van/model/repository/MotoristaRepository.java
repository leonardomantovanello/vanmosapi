package com.vanmos.van.model.repository;

import com.vanmos.van.model.entity.Motorista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotoristaRepository extends JpaRepository<Motorista, Long> {

    Optional<Motorista> findByEmailIgnoreCase(String email);

    @Query("SELECT m FROM Motorista m WHERE REPLACE(REPLACE(REPLACE(m.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Motorista> findByCpfDigits(@Param("cpfDigits") String cpfDigits);

    @Query("SELECT m FROM Motorista m WHERE LOWER(m.email) = LOWER(:email) " +
           "OR REPLACE(REPLACE(REPLACE(m.cpf, '.', ''), '-', ''), ' ', '') = :cpfDigits")
    Optional<Motorista> findByEmailIgnoreCaseOrCpfDigits(
            @Param("email") String email,
            @Param("cpfDigits") String cpfDigits
    );
}
