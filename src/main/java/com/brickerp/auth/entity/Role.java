package com.brickerp.auth.entity;

import com.brickerp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private RoleName name;

    @Column(name = "description", length = 255)
    private String description;

    public enum RoleName {
        ROLE_SUPER_ADMIN,
        ROLE_ADMIN,
        ROLE_MANAGER,
        ROLE_SALES,
        ROLE_PURCHASE,
        ROLE_WAREHOUSE,
        ROLE_ACCOUNTS,
        ROLE_VIEWER
    }
}