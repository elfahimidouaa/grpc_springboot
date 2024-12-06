package ma.projet.grpc.controllers;


import io.grpc.stub.StreamObserver;
import ma.projet.grpc.services.CompteService;
import ma.projet.grpc.stubs.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.*;
import java.util.stream.Collectors;

@GrpcService
public class CompteServiceImpl extends CompteServiceGrpc.CompteServiceImplBase {

    // Simuler une base de données en mémoire
    private final CompteService compteService;
    public CompteServiceImpl(CompteService compteService) {
        this.compteService = compteService;
    }
    @Override
    public void allComptes(GetAllComptesRequest request, StreamObserver<GetAllComptesResponse> responseObserver) {
        var comptes=compteService.findAllComptes().stream()
                .map(compte -> Compte.newBuilder()
                        .setId(compte.getId())
                        .setSolde(compte.getSolde())
                        .setDateCreation(compte.getDateCreation())
                        .setType(TypeCompte.valueOf(compte.getType()))
                        .build())
                .collect(Collectors.toList());

        responseObserver.onNext(GetAllComptesResponse.newBuilder().addAllComptes(comptes).build());
        responseObserver.onCompleted();
    }

    @Override
    public void saveCompte(SaveCompteRequest request,StreamObserver<SaveCompteResponse> responseObserver)
    {
        var compteReq=request.getCompte();
        var compte = new ma.projet.grpc.entities.Compte();
        compte.setSolde(compteReq.getSolde());
        compte.setDateCreation(compteReq.getDateCreation());
        compte.setType(compteReq.getType().name());

        var savedCompte=compteService.saveCompte(compte);
        var grpcCompte=Compte.newBuilder()
                .setId(savedCompte.getId())
                .setSolde(savedCompte.getSolde())
                .setDateCreation(savedCompte.getDateCreation())
                .setType(TypeCompte.valueOf(savedCompte.getType()))
                .build();
        responseObserver.onNext(SaveCompteResponse.newBuilder().setCompte(grpcCompte).build());
        responseObserver.onCompleted();


    }


    @Override
    public void compteById(GetCompteByIdRequest request, StreamObserver<GetCompteByIdResponse> responseObserver) {
        // Simulating getting the Compte by ID from the service
        Compte compte = compteService.findCompteById(request.getId());

        if (compte != null) {
            // Building the response object
            Compte grpcCompte = Compte.newBuilder()
                    .setId(compte.getId())
                    .setSolde(compte.getSolde())
                    .setDateCreation(compte.getDateCreation())
                    .setType(TypeCompte.valueOf(compte.getType()))
                    .build();

            responseObserver.onNext(GetCompteByIdResponse.newBuilder().setCompte(grpcCompte).build());
        } else {
            // If the compte is not found, return an error
            responseObserver.onError(new Throwable("Compte non trouvé"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void totalSolde(GetTotalSoldeRequest request, StreamObserver<GetTotalSoldeResponse> responseObserver) {
        // Calculate the total balance, count, and average of all comptes
        List<Compte> comptes = compteService.findAllComptes();
        int count = comptes.size();
        float sum = 0;

        for (Compte compte : comptes) {
            sum += compte.getSolde();
        }

        float average = count > 0 ? sum / count : 0;

        // Build SoldeStats object
        SoldeStats stats = SoldeStats.newBuilder()
                .setCount(count)
                .setSum(sum)
                .setAverage(average)
                .build();

        responseObserver.onNext(GetTotalSoldeResponse.newBuilder().setStats(stats).build());
        responseObserver.onCompleted();
    }
    // Handle deleteCompte method using CompteService
    @Override
    public void DeleteCompte(DeleteCompteRequest request, StreamObserver<DeleteCompteResponse> responseObserver) {
        String compteId = request.getId();
        try {
            compteService.deleteCompteById(compteId);
            DeleteCompteResponse response = DeleteCompteResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Compte supprimé avec succès")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            DeleteCompteResponse response = DeleteCompteResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Erreur lors de la suppression du compte")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    // Handle findByType method using CompteService
    @Override
    public void FindByType(GetComptesByTypeRequest request, StreamObserver<GetComptesByTypeResponse> responseObserver) {
        TypeCompte type = request.getType();
        List<Compte> comptes = compteService.findComptesByType(type.name());

        List<Compte> grpcComptes = comptes.stream()
                .map(compte -> Compte.newBuilder()
                        .setId(compte.getId())
                        .setSolde(compte.getSolde())
                        .setDateCreation(compte.getDateCreation())
                        .setType(TypeCompte.valueOf(compte.getType()))
                        .build())
                .collect(Collectors.toList());

        GetComptesByTypeResponse response = GetComptesByTypeResponse.newBuilder()
                .addAllComptes(grpcComptes)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}