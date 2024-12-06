package ma.projet.grpc.repositories;

import ma.projet.grpc.stubs.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompteRepository extends JpaRepository<Compte, String> {

    List<Compte> findByType(String type);
}
