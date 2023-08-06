package com.devsuperior.dscatalog.resources;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dscatalog.tests.Factory;
import com.devsuperior.dscatalog.tests.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Esta classe contém métodos de teste para a classe ProductResource.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ProductResourceTests {

	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService service;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private TokenUtil tokenUtil;
	
	private String username;
	private String password;

	private Long existingId;
	private Long nonExistingId;
	private Long dependentId;
	private ProductDTO productDTO;
	private PageImpl<ProductDTO> page;
	
	@BeforeEach
	void setUp() throws Exception {
		
		// Credenciais de usuário para teste
		username = "maria@gmail.com";
		password = "123456";

		// IDs de produtos para teste
		existingId = 1L;
		nonExistingId = 2L;
		dependentId = 3L;
		
		// Criação de um ProductDTO de teste
		productDTO = Factory.createProductDTO();
		
		// Criação de uma Page de ProductDTO contendo o produto de teste
		page = new PageImpl<>(List.of(productDTO));
		
		// Define o comportamento do mock ProductService
		when(service.findAllPaged(any(), any(), any())).thenReturn(page);
		when(service.findById(existingId)).thenReturn(productDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class);
		when(service.insert(any())).thenReturn(productDTO);
		when(service.update(eq(existingId), any())).thenReturn(productDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DatabaseException.class).when(service).delete(dependentId);
	}
	
	/**
	 * Teste para excluir um produto com ID existente.
	 * Deve retornar 204 No Content.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void deleteShouldReturnNoContentWhenIdExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		ResultActions result = 
				mockMvc.perform(delete("/products/{id}", existingId)
					.header("Authorization", "Bearer " + accessToken)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNoContent());
	}
	
	/**
	 * Teste para excluir um produto com ID não existente.
	 * Deve retornar 404 Not Found.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void deleteShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);

		ResultActions result = 
				mockMvc.perform(delete("/products/{id}", nonExistingId)
					.header("Authorization", "Bearer " + accessToken)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}
	
	/**
	 * Teste para inserir um novo produto.
	 * Deve retornar 201 Created e o novo ProductDTO no corpo da resposta.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void insertShouldReturnProductDTOCreated() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 
				mockMvc.perform(post("/products")
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	/**
	 * Teste para atualizar um produto existente com um ID válido.
	 * Deve retornar 200 Ok e o ProductDTO atualizado no corpo da resposta.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void updateShouldReturnProductDTOWhenIdExists() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 
				mockMvc.perform(put("/products/{id}", existingId)
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	/**
	 * Teste para atualizar um produto com um ID não existente.
	 * Deve retornar 404 Not Found.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void updateShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		String accessToken = tokenUtil.obtainAccessToken(mockMvc, username, password);
		String jsonBody = objectMapper.writeValueAsString(productDTO);
		
		ResultActions result = 
				mockMvc.perform(put("/products/{id}", nonExistingId)
					.header("Authorization", "Bearer " + accessToken)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}
	
	/**
	 * Teste para recuperar todos os produtos.
	 * Deve retornar 200 Ok com uma lista de produtos no corpo da resposta.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result = 
				mockMvc.perform(get("/products")
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
	}
	
	/**
	 * Teste para recuperar um produto com um ID existente.
	 * Deve retornar 200 Ok com o ProductDTO solicitado no corpo da resposta.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void findByIdShouldReturnProductWhenIdExists() throws Exception {
		ResultActions result = 
				mockMvc.perform(get("/products/{id}", existingId)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").exists());
		result.andExpect(jsonPath("$.description").exists());
	}
	
	/**
	 * Teste para recuperar um produto com um ID não existente.
	 * Deve retornar 404 Not Found.
	 *
	 * @throws Exception se houver erro ao realizar a requisição de teste
	 */
	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() throws Exception {
		ResultActions result = 
				mockMvc.perform(get("/products/{id}", nonExistingId)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	} 
}
