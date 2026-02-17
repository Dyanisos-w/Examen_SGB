package com.example.padelbackend.service;

import com.example.padelbackend.dto.LoginRequestDTO;
import com.example.padelbackend.dto.LoginResponseDTO;
import com.example.padelbackend.model.Utilisateur;
import com.example.padelbackend.repository.UtilisateurRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RoleService roleService;

    public AuthService(UtilisateurRepository utilisateurRepository,
                      BCryptPasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      RoleService roleService) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.roleService = roleService;
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        // Find user by matricule
        Utilisateur utilisateur = utilisateurRepository.findByMatricule(loginRequest.getMatricule());
        
        if (utilisateur == null) {
            throw new BadCredentialsException("Invalid matricule or password");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), utilisateur.getPassword())) {
            throw new BadCredentialsException("Invalid matricule or password");
        }

        // Derive role from matricule
        String role = roleService.deriveRoleFromMatricule(utilisateur.getMatricule());

        // Generate JWT token
        String token = jwtService.generateToken(utilisateur.getMatricule(), role);

        // Return response
        return new LoginResponseDTO(token, role);
    }
}
