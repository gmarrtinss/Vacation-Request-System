package com.rh.vacationbackend.service;

import com.rh.vacationbackend.model.Employee;
import com.rh.vacationbackend.model.EmployeeRole;
import com.rh.vacationbackend.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder; // O codificador de senhas

    // Usando injeção de dependência via construtor (prática recomendada)
    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

     /**
     * CREATE: Cria um novo funcionário com senha criptografada.
     */
    @Transactional
    public Employee create(Employee employee) {
        if (employeeRepository.findByCpf(employee.getCpf()).isPresent()) {
            throw new IllegalArgumentException("CPF already exists in the system.");
        }
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists in the system.");
        }

        if (employee.getRole() == EmployeeRole.MANAGER) {
            if (employee.getPassword() == null || employee.getPassword().isBlank()) {
                throw new IllegalArgumentException("Password is required for managers.");
            }
            // CRIPTOGRAFA A SENHA ANTES DE SALVAR
            String senhaCriptografada = passwordEncoder.encode(employee.getPassword());
            employee.setPassword(senhaCriptografada);

        } else {
            employee.setPassword(null);
        }

        return employeeRepository.save(employee);
    }

    /**
     * READ: Busca todos os funcionários.
     */
    @Transactional(readOnly = true)
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * READ: Busca um funcionário específico pelo seu ID.
     */
    @Transactional(readOnly = true)
    public Employee findById(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o ID: " + id)); // Em um projeto real, use uma exceção customizada
    }

    @Transactional(readOnly = true)
    public Employee findManagedEmployeeByCpf(String cpf) {
        Employee employeeToFind = employeeRepository.findByCpf(cpf)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado com o CPF: " + cpf));
        return employeeToFind;
    }

    /**
     * UPDATE: Atualiza os dados de um funcionário existente.
     */
    @Transactional
    public Employee update(UUID id, Employee employeeDetails) {
        // 1. Busca a entidade existente no banco para garantir que ela existe.
        Employee existingEmployee = findById(id);

        // 2. Atualiza os campos da entidade encontrada com os novos valores.
        // Evita-se atualizar campos sensíveis como CPF, ID, ou a senha diretamente aqui.
        existingEmployee.setName(employeeDetails.getName());
        existingEmployee.setEmail(employeeDetails.getEmail());
        existingEmployee.setRole(employeeDetails.getRole());
        existingEmployee.setSector(employeeDetails.getSector());
        existingEmployee.setPosition(employeeDetails.getPosition());
        existingEmployee.setManager(employeeDetails.getManager());
        // A lógica de senha seria tratada em um método separado, ex: changePassword()

        // 3. O método save, ao receber um objeto com ID, executa um UPDATE.
        return employeeRepository.save(existingEmployee);
    }

    /**
     * DELETE: Remove um funcionário do banco de dados.
     */
    @Transactional
    public void delete(UUID id) {
        // Verifica se o funcionário existe antes de tentar deletar.
        // O findById já lança uma exceção se não encontrar.
        findById(id);
        employeeRepository.deleteById(id);
    }

    // --- MÉTODOS DE NEGÓCIO ESPECÍFICOS ---

    /**
     * READ (Específico): Lista todos os funcionários gerenciados por um gestor.
     * Este método seria chamado por um endpoint protegido, acessível apenas por gestores.
     */
    @Transactional(readOnly = true)
    public List<Employee> findEmployeesByManager(Employee manager) {
        if (manager.getRole() != EmployeeRole.MANAGER) {
            throw new SecurityException("Only managers can acess their employees.");
        }
        return employeeRepository.findByManager(manager);
    }
}