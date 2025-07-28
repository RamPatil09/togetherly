package com.socialmedia.togetherly.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role {
    @Id
    private String id;
    private String name;
    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}