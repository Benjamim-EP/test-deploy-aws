package com.devsuperior.dscatalog.resources;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.services.ProductService;

/**
 * Resource que representa os endpoints relacionados aos produtos.
 */
@RestController
@RequestMapping(value = "/products")
public class ProductResource {

	@Autowired
	private ProductService service;

	/**
	 * Endpoint para buscar uma lista paginada de produtos de acordo com os parâmetros fornecidos.
	 * 
	 * @param categoryId O ID da categoria dos produtos a serem filtrados. (opcional)
	 * @param name       O nome dos produtos a serem filtrados. (opcional)
	 * @param pageable   Informações sobre a paginação dos resultados.
	 * @return ResponseEntity contendo uma lista paginada de ProductDTO.
	 */
	@GetMapping
	public ResponseEntity<Page<ProductDTO>> findAll(
			@RequestParam(value = "categoryId", defaultValue = "0") Long categoryId,
			@RequestParam(value = "name", defaultValue = "") String name,
			Pageable pageable) {

		Page<ProductDTO> list = service.findAllPaged(categoryId, name.trim(), pageable);
		return ResponseEntity.ok().body(list);
	}

	/**
	 * Endpoint para buscar um produto pelo seu ID.
	 * 
	 * @param id O ID do produto a ser buscado.
	 * @return ResponseEntity contendo o ProductDTO correspondente ao ID fornecido.
	 */
	@GetMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> findById(@PathVariable Long id) {
		ProductDTO dto = service.findById(id);
		return ResponseEntity.ok().body(dto);
	}

	/**
	 * Endpoint para inserir um novo produto no sistema.
	 * 
	 * @param dto O ProductDTO contendo as informações do novo produto a ser inserido.
	 * @return ResponseEntity contendo o ProductDTO inserido e o cabeçalho da resposta com a URI do recurso criado.
	 */
	@PostMapping
	public ResponseEntity<ProductDTO> insert(@Valid @RequestBody ProductDTO dto) {
		dto = service.insert(dto);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(dto.getId()).toUri();
		return ResponseEntity.created(uri).body(dto);
	}

	/**
	 * Endpoint para atualizar as informações de um produto existente no sistema.
	 * 
	 * @param id  O ID do produto a ser atualizado.
	 * @param dto O ProductDTO contendo as novas informações do produto.
	 * @return ResponseEntity contendo o ProductDTO atualizado.
	 */
	@PutMapping(value = "/{id}")
	public ResponseEntity<ProductDTO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
		dto = service.update(id, dto);
		return ResponseEntity.ok().body(dto);
	}

	/**
	 * Endpoint para deletar um produto pelo seu ID.
	 * 
	 * @param id O ID do produto a ser deletado.
	 * @return ResponseEntity sem conteúdo no corpo da resposta (204 No Content) indicando que o produto foi deletado com sucesso.
	 */
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}
}
