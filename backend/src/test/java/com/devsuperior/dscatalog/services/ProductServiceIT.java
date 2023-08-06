/**
 * Testes de integração para a classe ProductService.
 * Esses testes verificam a funcionalidade do ProductService em conjunto com o banco de dados e o repositório subjacente.
 */
package com.devsuperior.dscatalog.services;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

@SpringBootTest
@Transactional
public class ProductServiceIT {

    // O serviço a ser testado
    @Autowired
    private ProductService service;
    
    // O repositório usado para verificação e limpeza
    @Autowired
    private ProductRepository repository;
    
    // Dados de exemplo para testes
    private Long existingId;         // Um ID de produto existente no banco de dados
    private Long nonExistingId;      // Um ID de produto que não existe no banco de dados
    private Long countTotalProducts; // Número total de produtos no banco de dados
    
    // Configurando os dados de teste
    @BeforeEach
    void setUp() throws Exception {
        existingId = 1L;
        nonExistingId = 1000L;
        countTotalProducts = 25L;
    }
    
    /**
     * Teste para o método delete do ProductService.
     * Ele deve excluir um produto quando o ID existir no banco de dados.
     */
    @Test
    public void deleteShouldDeleteResourceWhenIdExists() {
        
        // Ação: Tentar excluir o produto com o ID existente
        service.delete(existingId);

        // Verificação: Verificar que o número total de produtos diminuiu em um
        Assertions.assertEquals(countTotalProducts - 1, repository.count());
    }
    
    /**
     * Teste para o método delete do ProductService.
     * Ele deve lançar uma ResourceNotFoundException quando o ID não existir no banco de dados.
     */
    @Test
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        
        // Verificação: Verificar que tentar excluir um ID que não existe lança uma ResourceNotFoundException
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            service.delete(nonExistingId);
        });
    }
    
    /**
     * Teste para o método findAllPaged do ProductService.
     * Ele deve retornar uma página de produtos quando forem fornecidos parâmetros válidos (número da página, tamanho da página).
     */
    @Test
    public void findAllPagedShouldReturnPageWhenPage0Size10() {
        
        // Preparação: Configurar a solicitação de paginação (página 0, tamanho 10)
        PageRequest pageRequest = PageRequest.of(0, 10);
        
        // Ação: Obter a primeira página de produtos
        Page<ProductDTO> result = service.findAllPaged(0L, "", pageRequest);
        
        // Verificação: Verificar que a página retornada não está vazia, tem o número e tamanho corretos e contém todos os produtos
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(0, result.getNumber());
        Assertions.assertEquals(10, result.getSize());
        Assertions.assertEquals(countTotalProducts, result.getTotalElements());
    }
    
    /**
     * Teste para o método findAllPaged do ProductService.
     * Ele deve retornar uma página vazia quando o número da página solicitada não existir.
     */
    @Test
    public void findAllPagedShouldReturnEmptyPageWhenPageDoesNotExist() {
        
        // Preparação: Configurar a solicitação de paginação (página 50, tamanho 10)
        PageRequest pageRequest = PageRequest.of(50, 10);
        
        // Ação: Obter uma página de produtos que não existe
        Page<ProductDTO> result = service.findAllPaged(0L, "", pageRequest);
        
        // Verificação: Verificar que a página retornada está vazia
        Assertions.assertTrue(result.isEmpty());
    }
    
    /**
     * Teste para o método findAllPaged do ProductService.
     * Ele deve retornar uma página de produtos ordenada quando ordenada pelo nome.
     */
    @Test
    public void findAllPagedShouldReturnSortedPageWhenSortByName() {
        
        // Preparação: Configurar a solicitação de paginação (página 0, tamanho 10) ordenada pelo nome
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name"));
        
        // Ação: Obter a primeira página de produtos ordenada pelo nome
        Page<ProductDTO> result = service.findAllPaged(0L, "", pageRequest);
        
        // Verificação: Verificar que a página retornada não está vazia e que os produtos estão corretamente ordenados pelo nome
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals("Macbook Pro", result.getContent().get(0).getName());
        Assertions.assertEquals("PC Gamer", result.getContent().get(1).getName());
        Assertions.assertEquals("PC Gamer Alfa", result.getContent().get(2).getName());        
    }
}
