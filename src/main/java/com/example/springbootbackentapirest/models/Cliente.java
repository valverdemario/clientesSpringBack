package com.example.springbootbackentapirest.models;


import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCliente;

    @Column(nullable = false)
    @NotEmpty(message = "El nombre es requerido")
    @Size(min = 4, max = 12, message = "El nombre debe tener entre 4 y 12 caracteres")
    private String nombre;

    @NotEmpty(message = "El apellido es requerido")
    private String apellidos;

    @Column(nullable = false, unique = true)
    @Email(message = "El email no es valido")
    @NotEmpty(message = "El email es requerido")
    private String email;

    @Column(name = "created_at")
    @Temporal(TemporalType.DATE)
    @NotNull(message = "La fecha de creacion es requerida")
    private Date createAt;

    private String foto;



}
