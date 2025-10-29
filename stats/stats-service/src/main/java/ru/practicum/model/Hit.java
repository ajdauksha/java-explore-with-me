package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "hits")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false, length = 512)
    private String uri;

    @Column(nullable = false, length = 45)
    private String ip;

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
