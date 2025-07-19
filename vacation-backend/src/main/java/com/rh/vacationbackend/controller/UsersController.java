// Versão Corrigida do UsersController.java

package com.rh.vacationbackend.controller;

import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.service.UsersService; // IMPORT CORRETO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    // INJETE O SERVICE, NÃO O REPOSITORY
    @Autowired
    private UsersService usersService;

    // CREATE - Cria um novo usuario
    @PostMapping
    public ResponseEntity<Users> create(@RequestBody Users users) {
        // CHAME O METODO DO SERVICE
        Users saved = usersService.create(users);
        return ResponseEntity.ok(saved);
    }

    // READ - Buscar todos os usuarios
    @GetMapping
    public ResponseEntity<List<Users>> getAll() {
        // CHAME O METODO DO SERVICE
        List<Users> list = usersService.findAll();
        return ResponseEntity.ok(list);
    }

    // READ - Buscar usuario por ID
    @GetMapping("/{id}")
    public ResponseEntity<Users> getById(@PathVariable UUID id) {
        // O SERVICE JÁ TRATA O CASO DE "NÃO ENCONTRADO"
        Users user = usersService.findById(id);
        return ResponseEntity.ok(user);
    }

    // UPDATE - Atualizar usuario por ID
    @PutMapping("/{id}")
    public ResponseEntity<Users> update(@PathVariable UUID id, @RequestBody Users updatedData) {
        // O SERVICE CONTÉM A LÓGICA DE UPDATE
        Users saved = usersService.update(id, updatedData);
        return ResponseEntity.ok(saved);
    }

    // DELETE - Excluir usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        // O SERVICE VERIFICA SE O USUÁRIO EXISTE ANTES DE DELETAR
        usersService.delete(id);
        return ResponseEntity.noContent().build();
    }
}