package com.devsuperior.dscatalog.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;

/**
 * ProductServiceTests é uma classe de teste para o ProductService.
 * Ela inclui casos de teste para garantir a correção dos métodos do serviço.
 */
@ExtendWith(SpringExtension.class)
public class ProductServiceTests {

    @InjectMocks
    private ProductService service;

    @Mock
    private ProductRepository repository;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private Product product;
    private PageImpl<Product> page;

    /**
     * Este método é executado antes de cada caso de teste.
     * Ele configura os dados necessários e o comportamento simulado para os testes.
     *
     * @throws Exception se ocorrer um erro durante a configuração.
     */
    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 2L;
        dependentId = 3L;
        product = Factory.createProduct();
        page = new PageImpl<>(List.of(product));

        // Simulando o comportamento dos métodos do repositório
        Mockito.when(repository.findAll((Pageable) any())).thenReturn(page);
        Mockito.when(repository.save(any())).thenReturn(product);
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(product));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
        Mockito.when(repository.find(any(), any(), any())).thenReturn(page);
        Mockito.doNothing().when(repository).deleteById(existingId);
        Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
        Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);
    }

    /**
     * Caso de teste para o método findAllPaged do ProductService.
     * Ele deve retornar uma página de ProductDTO.
     */
    @Test
    public void findAllPagedShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 12);
        Page<ProductDTO> result = service.findAllPaged(0L, "", pageable);
        Assertions.assertNotNull(result);
    }

    /**
     * Caso de teste para o método delete do ProductService quando o produto possui entidades dependentes.
     * Ele deve lançar uma DatabaseException.
     */
    @Test
    public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
        Assertions.assertThrows(DatabaseException.class, () -> {
            service.delete(dependentId);
        });
        Mockito.verify(repository, times(1)).deleteById(dependentId);
    }

    /**
     * Caso de teste para o método delete do ProductService quando o ID fornecido não existe.
     * Ele deve lançar uma ResourceNotFoundException.
     */
    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
        Mockito.verify(repository, times(1)).deleteById(nonExistingId);
    }

    /**
     * Caso de teste para o método delete do ProductService quando o ID fornecido existe.
     * Ele não deve lançar nenhuma exceção e deve deletar o produto com sucesso.
     */
    @Test
    public void deleteShouldDoNothingWhenIdExists() {
        Assertions.assertDoesNotThrow(() -> {
            service.delete(existingId);
        });
        Mockito.verify(repository, times(1)).deleteById(existingId);
    }
}
