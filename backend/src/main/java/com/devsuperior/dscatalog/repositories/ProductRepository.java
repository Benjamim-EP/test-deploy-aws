package com.devsuperior.dscatalog.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.devsuperior.dscatalog.entities.Category;
import com.devsuperior.dscatalog.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Consulta personalizada para encontrar produtos por categorias e nome, usando paginação.
     * Este método realiza uma busca com base na lista fornecida de categorias e no nome parcial do produto.
     *
     * @param categories A lista de categorias para filtrar os produtos. Se nula, todos os produtos serão considerados.
     * @param name       O nome parcial do produto a ser pesquisado (sem distinguir maiúsculas e minúsculas).
     * @param pageable   O objeto "pageable" para controlar a paginação e ordenação dos resultados.
     * @return Uma página de produtos que correspondem aos critérios de pesquisa.
     */
    @Query("SELECT DISTINCT obj FROM Product obj INNER JOIN obj.categories cats WHERE "
            + "(COALESCE(:categories) IS NULL OR cats IN :categories) AND "
            + "(LOWER(obj.name) LIKE LOWER(CONCAT('%',:name,'%'))) ")
    Page<Product> find(List<Category> categories, String name, Pageable pageable);

    /**
     * Consulta personalizada para encontrar produtos com suas categorias associadas.
     * Este método é usado para buscar produtos juntamente com suas categorias associadas em uma única consulta.
     *
     * @param products A lista de produtos para os quais se deseja obter as categorias associadas.
     * @return Uma lista de produtos com suas categorias associadas.
     */
    @Query("SELECT obj FROM Product obj JOIN FETCH obj.categories WHERE obj IN :products")
    List<Product> findProductsWithCategories(List<Product> products);
}
