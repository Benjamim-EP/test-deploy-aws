package com.devsuperior.dscatalog.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.dscatalog.dto.CategoryDTO;
import com.devsuperior.dscatalog.dto.ProductDTO;
import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;
import com.devsuperior.dscatalog.repositories.CategoryRepository;
import com.devsuperior.dscatalog.repositories.ProductRepository;
import com.devsuperior.dscatalog.services.exceptions.DatabaseException;
import com.devsuperior.dscatalog.services.exceptions.ResourceNotFoundException;

/**
 * A classe ProductService fornece vários serviços relacionados aos produtos no catálogo.
 * Ela interage com o ProductRepository e o CategoryRepository para executar operações de CRUD
 * nos produtos e lidar com as associações de categorias.
 */

@Service
public class ProductService {

	@Autowired
	private ProductRepository repository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	/**
	 * Recupera uma lista paginada de produtos filtrada por categoria e nome.
	 *
	 * @param categoryId O ID da categoria para filtrar produtos (0 se não estiver filtrando por categoria).
	 * @param name O nome do produto para filtrar (vazio ou nulo se não estiver filtrando por nome).
	 * @param pageable As informações de paginação.
	 * @return Uma página de ProductDTO contendo os produtos filtrados e suas categorias associadas.
	 */
	@Transactional(readOnly = true)
	public Page<ProductDTO> findAllPaged(Long categoryId, String name, Pageable pageable) {
		List<Category> categories = (categoryId == 0) ? null : Arrays.asList(categoryRepository.getOne(categoryId));
		Page<Product> page = repository.find(categories, name, pageable);
		repository.findProductsWithCategories(page.getContent());
		return page.map(x -> new ProductDTO(x, x.getCategories()));
	}

	/**
	 * Recupera um produto específico pelo seu ID.
	 *
	 * @param id O ID do produto a ser recuperado.
	 * @return A representação ProductDTO do produto encontrado, juntamente com suas categorias associadas.
	 * @throws ResourceNotFoundException Se o produto com o ID fornecido não for encontrado.
	 */
	@Transactional(readOnly = true)
	public ProductDTO findById(Long id) {
		Optional<Product> obj = repository.findById(id);
		Product entity = obj.orElseThrow(() -> new ResourceNotFoundException("Entidade não encontrada"));
		return new ProductDTO(entity, entity.getCategories());
	}

	/**
	 * Insere um novo produto no catálogo.
	 *
	 * @param dto O ProductDTO contendo os dados para criar o novo produto.
	 * @return A representação ProductDTO do produto recém-criado.
	 */
	@Transactional
	public ProductDTO insert(ProductDTO dto) {
		Product entity = new Product();
		copyDtoToEntity(dto, entity);
		entity = repository.save(entity);
		return new ProductDTO(entity);
	}

	/**
	 * Atualiza um produto existente no catálogo.
	 *
	 * @param id O ID do produto a ser atualizado.
	 * @param dto O ProductDTO contendo os dados atualizados para o produto.
	 * @return A representação ProductDTO do produto atualizado.
	 * @throws ResourceNotFoundException Se o produto com o ID fornecido não for encontrado.
	 */
	@Transactional
	public ProductDTO update(Long id, ProductDTO dto) {
		try {
			Product entity = repository.getOne(id);
			copyDtoToEntity(dto, entity);
			entity = repository.save(entity);
			return new ProductDTO(entity);
		}
		catch (EntityNotFoundException e) {
			throw new ResourceNotFoundException("ID não encontrado " + id);
		}		
	}

	/**
	 * Exclui um produto do catálogo pelo seu ID.
	 *
	 * @param id O ID do produto a ser excluído.
	 * @throws ResourceNotFoundException Se o produto com o ID fornecido não for encontrado.
	 * @throws DatabaseException Se houver uma violação de integridade do banco de dados ao tentar excluir o produto.
	 */
	public void delete(Long id) {
		try {
			repository.deleteById(id);
		}
		catch (EmptyResultDataAccessException e) {
			throw new ResourceNotFoundException("ID não encontrado " + id);
		}
		catch (DataIntegrityViolationException e) {
			throw new DatabaseException("Violação de integridade");
		}
	}
	
	/**
	 * Copia os dados do ProductDTO para a entidade Product.
	 *
	 * @param dto O ProductDTO contendo os dados a serem copiados.
	 * @param entity A entidade Product para onde os dados devem ser copiados.
	 */
	private void copyDtoToEntity(ProductDTO dto, Product entity) {

		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		entity.setDate(dto.getDate());
		entity.setImgUrl(dto.getImgUrl());
		entity.setPrice(dto.getPrice());
		
		entity.getCategories().clear();
		for (CategoryDTO catDto : dto.getCategories()) {
			Category category = categoryRepository.getOne(catDto.getId());
			entity.getCategories().add(category);			
		}
	}	
}
