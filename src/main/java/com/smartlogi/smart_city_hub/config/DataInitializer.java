package com.smartlogi.smart_city_hub.config;

import com.smartlogi.smart_city_hub.entity.Category;
import com.smartlogi.smart_city_hub.entity.User;
import com.smartlogi.smart_city_hub.entity.enums.Role;
import com.smartlogi.smart_city_hub.repository.CategoryRepository;
import com.smartlogi.smart_city_hub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        initializeCategories();
        initializeDefaultUsers();
    }
    
    private void initializeCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    Category.builder()
                            .name("Voirie")
                            .description("Problèmes de chaussée, nids de poule, trottoirs endommagés")
                            .icon("road")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Éclairage Public")
                            .description("Lampadaires défaillants, zones mal éclairées")
                            .icon("lightbulb")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Propreté")
                            .description("Déchets, graffitis, encombrants")
                            .icon("trash")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Espaces Verts")
                            .description("Entretien des parcs, arbres dangereux, végétation")
                            .icon("tree")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Signalisation")
                            .description("Panneaux endommagés, marquage au sol effacé")
                            .icon("sign")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Mobilier Urbain")
                            .description("Bancs, abribus, poubelles endommagés")
                            .icon("bench")
                            .active(true)
                            .build(),
                    Category.builder()
                            .name("Autre")
                            .description("Autres problèmes urbains")
                            .icon("help-circle")
                            .active(true)
                            .build()
            );
            
            categoryRepository.saveAll(categories);
            log.info("Initialized {} categories", categories.size());
        }
    }
    
    private void initializeDefaultUsers() {
        // Create admin user if not exists
        if (!userRepository.existsByEmail("admin@smartcityhub.com")) {
            User admin = User.builder()
                    .email("admin@smartcityhub.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .firstName("Admin")
                    .lastName("System")
                    .role(Role.ROLE_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Created default admin user: admin@smartcityhub.com");
        }
        
        // Create supervisor user if not exists
        if (!userRepository.existsByEmail("supervisor@smartcityhub.com")) {
            User supervisor = User.builder()
                    .email("supervisor@smartcityhub.com")
                    .password(passwordEncoder.encode("Super@123"))
                    .firstName("Marie")
                    .lastName("Supervisor")
                    .role(Role.ROLE_SUPERVISOR)
                    .active(true)
                    .build();
            userRepository.save(supervisor);
            log.info("Created default supervisor user: supervisor@smartcityhub.com");
        }
        
        // Create agent user if not exists
        if (!userRepository.existsByEmail("agent@smartcityhub.com")) {
            User agent = User.builder()
                    .email("agent@smartcityhub.com")
                    .password(passwordEncoder.encode("Agent@123"))
                    .firstName("Jean")
                    .lastName("Agent")
                    .role(Role.ROLE_AGENT)
                    .active(true)
                    .build();
            userRepository.save(agent);
            log.info("Created default agent user: agent@smartcityhub.com");
        }
        
        // Create test citizen if not exists
        if (!userRepository.existsByEmail("user@smartcityhub.com")) {
            User user = User.builder()
                    .email("user@smartcityhub.com")
                    .password(passwordEncoder.encode("User@123"))
                    .firstName("Pierre")
                    .lastName("Citoyen")
                    .role(Role.ROLE_USER)
                    .active(true)
                    .build();
            userRepository.save(user);
            log.info("Created default citizen user: user@smartcityhub.com");
        }
    }
}
