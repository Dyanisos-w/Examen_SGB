package be.ephec.padel_backend.controller;
import be.ephec.padel_backend.model.Utilisateur;
import be.ephec.padel_backend.repository.UtilisateurRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {
    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @GetMapping
    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurRepository.findAll();
    }

    @GetMapping("/{matricule}")
    public Utilisateur getUtilisateur(@PathVariable String matricule) {
        return utilisateurRepository.findById(matricule)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé")); // créé une exeption qui hérite de runtime, il est déconseiller de faire un trhow ici, c'est mon service qui devrait avoir l'exception.
    }

    @PostMapping
    public Utilisateur createUtilisateur(@RequestBody Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }
}
