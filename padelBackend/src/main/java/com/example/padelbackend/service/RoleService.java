package com.example.padelbackend.service;

import org.springframework.stereotype.Service;

@Service
public class RoleService {

    public String deriveRoleFromMatricule(String matricule) {
        if (matricule == null || matricule.isEmpty()) {
            throw new IllegalArgumentException("Matricule cannot be null or empty");
        }

        if (matricule.startsWith("GA")) {
            return "ROLE_GLOBAL_ADMIN";
        } else if (matricule.startsWith("LA")) {
            return "ROLE_LOCAL_ADMIN";
        } else if (matricule.startsWith("G")) {
            return "ROLE_GLOBAL_USER";
        } else if (matricule.startsWith("L")) {
            return "ROLE_LOCAL_USER";
        } else if (matricule.startsWith("S")) {
            return "ROLE_FREE_USER";
        } else {
            throw new IllegalArgumentException("Invalid matricule prefix: " + matricule);
        }
    }
}
