package com.example.springbootbackentapirest.controllers;


import com.example.springbootbackentapirest.models.Cliente;
import com.example.springbootbackentapirest.models.services.IClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController()
@RequestMapping("api/cliente")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ClienteController {
    @Autowired
    private IClienteService clienteService;

    @GetMapping
    public List<Cliente> findAll() {
        return clienteService.findAll();
    }

    @GetMapping("/page/{page}")
    public Page<Cliente> findAll(@PathVariable Integer page) {
        return clienteService.findAll(PageRequest.of(page, 5));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> findById(@PathVariable Long id) {
        Cliente cliente = clienteService.findById(id);
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("cliente", cliente);
        } catch (Exception e) {
            response.put("mensaje", "Error al procesar su peticion");
            response.put("error", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (cliente == null) {
            response.put("mensaje", "El cliente no existe");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("cliente", cliente);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> save(@Valid @RequestBody Cliente cliente, BindingResult result) {

        Cliente clienteNuevo = null;
        Map<String, Object> response = new HashMap<>();
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream().map(error -> {
                return "El campo '" + error.getField() + "' " + error.getDefaultMessage();
            }).collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }
        try {
            clienteNuevo = clienteService.save(cliente);
        } catch (Exception e) {
            response.put("mensaje", "Error al procesar su peticion");
            response.put("error", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "Cliente guardado con exito");
        response.put("cliente", clienteNuevo);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id) {
        Cliente clienteActual = clienteService.findById(id);
        Cliente clienteUpdate = null;
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream().map(error -> {
                return "El campo '" + error.getField() + "' " + error.getDefaultMessage();
            }).collect(Collectors.toList());
            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }

        if (clienteActual == null) {
            response.put("mensaje", "Error: no se pudo encontrar el cliente con el id: " + id);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.NOT_FOUND);
        }
        try {
            clienteActual.setNombre(cliente.getNombre());
            clienteActual.setApellidos(cliente.getApellidos());
            clienteActual.setEmail(cliente.getEmail());
            clienteActual.setCreateAt(cliente.getCreateAt());
            clienteUpdate = clienteService.save(clienteActual);
        } catch (Exception e) {
            response.put("mensaje", "Error al procesar su peticion");
            response.put("error", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "Cliente actualizado con exito");
        response.put("cliente", clienteUpdate);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Cliente cliente = clienteService.findById(id);
            String nombreFotoAnterior = cliente.getFoto();
            if (nombreFotoAnterior != null && nombreFotoAnterior.length() > 0) {
                Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                File archivoFotoAnterior = rutaFotoAnterior.toFile();
                if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                    archivoFotoAnterior.delete();
                }

            }
            clienteService.delete(id);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al procesar su peticion");
            response.put("error", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "Cliente eliminado con exito");
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @PutMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile file, @RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        Cliente cliente = clienteService.findById(id);

        if (!file.isEmpty()) {
            String nombreArchivo = UUID.randomUUID() +"_"+ file.getOriginalFilename().replace(" ","");
            Path rutaArchivo = Paths.get("uploads").resolve(nombreArchivo).toAbsolutePath();


            try {
                Files.copy(file.getInputStream(), rutaArchivo);
                String nombreFotoAnterior = cliente.getFoto();
                if (nombreFotoAnterior != null && nombreFotoAnterior.length() > 0) {
                    Path rutaFotoAnterior = Paths.get("uploads").resolve(nombreFotoAnterior).toAbsolutePath();
                    File archivoFotoAnterior = rutaFotoAnterior.toFile();
                    if (archivoFotoAnterior.exists() && archivoFotoAnterior.canRead()) {
                        archivoFotoAnterior.delete();
                    }

                }
                cliente.setFoto(nombreArchivo);
                clienteService.save(cliente);
                response.put("cliente", cliente);
                response.put("mensaje", "Archivo subido con exito");
            } catch (Exception e) {
                response.put("mensaje", "Error al procesar su peticion");
                response.put("error", e.getMessage());
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            response.put("mensaje", "El archivo esta vacio");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("/uploads/{nombreFoto:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String nombreFoto) {
        Path rutaArchivo = Paths.get("uploads").resolve(nombreFoto).toAbsolutePath();
        Resource recurso = null;
        try {
            recurso = new UrlResource(rutaArchivo.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        if (!recurso.exists() || !recurso.isReadable()) {
            throw new RuntimeException("Error: No se pudo cargar la imagen");
        }
        HttpHeaders cabecera = new HttpHeaders();
        cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + recurso.getFilename() + "\"");

        return new ResponseEntity<>(recurso, cabecera,HttpStatus.OK);
    }
}
