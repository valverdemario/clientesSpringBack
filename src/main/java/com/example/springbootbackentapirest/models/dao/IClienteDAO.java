package com.example.springbootbackentapirest.models.dao;

import com.example.springbootbackentapirest.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IClienteDAO  extends JpaRepository<Cliente, Long> {



}
