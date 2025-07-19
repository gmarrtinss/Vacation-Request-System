package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Users;
import com.rh.vacationbackend.model.UsersRole;
import com.rh.vacationbackend.repository.UsersRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder; // O codificador de senhas

    // Usando injeção de dependência via construtor (prática recomendada)
    public UsersService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

     /**
     * CREATE: Cria um novo funcionário com senha criptografada.
     */
    @Transactional
    public Users create(Users users) {
        if  (usersRepository.findByCpf(users.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF already exists in the system.");
        }
        if (usersRepository.findByEmail(users.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists in the system.");
        }

        if (users.getRole() == UsersRole.MANAGER) {
            if (users.getPassword() == null || users.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required for managers.");
            }
            // CRIPTOGRAFA A SENHA ANTES DE SALVAR
            String senhaCriptografada = passwordEncoder.encode(users.getPassword());
            users.setPassword(senhaCriptografada);

        } else {
            users.setPassword(null);
        }

        return usersRepository.save(users);
    }

    /**
     * READ: Busca todos os funcionários.
     */
    @Transactional(readOnly = true)
    public List<Users> findAll() {
        return usersRepository.findAll();
    }

    /**
     * READ: Busca um funcionário específico pelo seu ID.
     */
    @Transactional(readOnly = true)
    public Users findById(UUID id) {
        return usersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o ID: " + id)); // Em um projeto real, use uma exceção customizada
    }

    @Transactional(readOnly = true)
    public Users findManagedEmployeeByCpf(String cpf) {
        Users usersToFind = usersRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o CPF: " + cpf));
        return usersToFind;
    }

    /**
     * UPDATE: Atualiza os dados de um funcionário existente.
     */
    @Transactional
    public Users update(UUID id, Users usersDetails) {
        // 1. Busca a entidade existente no banco para garantir que ela existe.
        Users existingUsers = findById(id);

        // 2. Atualiza os campos da entidade encontrada com os novos valores.
        // Evita-se atualizar campos sensíveis como CPF, ID, ou a senha diretamente aqui.
        existingUsers.setName(usersDetails.getName());
        existingUsers.setEmail(usersDetails.getEmail());
        existingUsers.setRole(usersDetails.getRole());
        existingUsers.setSector(usersDetails.getSector());
        existingUsers.setPosition(usersDetails.getPosition());
        existingUsers.setManager(usersDetails.getManager());
        // A lógica de senha seria tratada em um método separado, ex: changePassword()

        // 3. O método save, ao receber um objeto com ID, executa um UPDATE.
        return usersRepository.save(existingUsers);
    }

    /**
     * DELETE: Remove um funcionário do banco de dados.
     */
    @Transactional
    public void delete(UUID id) {
        // Verifica se o funcionário existe antes de tentar deletar.
        // O findById já lança uma exceção se não encontrar.
        findById(id);
        usersRepository.deleteById(id);
    }

    // --- MÉTODOS DE NEGÓCIO ESPECÍFICOS ---

    /**
     * READ (Específico): Lista todos os funcionários gerenciados por um gestor.
     * Este método seria chamado por um endpoint protegido, acessível apenas por gestores.
     */
    @Transactional(readOnly = true)
    public List<Users> findEmployeesByManager(Users manager) {
        if (manager.getRole() != UsersRole.MANAGER) {
            throw new SecurityException("Only managers can acess their employees.");
        }
        return usersRepository.findByManager(manager);
    }
}