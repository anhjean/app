package com.shop.app.modules.catalog.category.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.shop.app.modules.catalog.category.model.Category;
import com.shop.app.modules.catalog.category.model.CategoryDto;
import com.shop.app.modules.catalog.category.repository.CategoryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional("tenantTransactionManager")
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Category createCategory(CategoryDto categoryDto) {
        // Category newCategory = new Category();
        Category newCategory;
        if (categoryDto.getParentId() != null) {
            Optional<Category> parent = categoryRepository.findById(categoryDto.getParentId());
            if (parent.isPresent()) {
                log.info("Parent is present ");
                newCategory = new Category(categoryDto, parent.get());
            } else {
                newCategory = new Category(categoryDto);
            }
        } else {
            newCategory = new Category(categoryDto);
        }

        categoryRepository.save(newCategory);
        newCategory.generateHierarchy();
        return categoryRepository.save(newCategory);
    }

    @Override
    public String deleteCategory(Long id) {
        Optional<Category> foundCategory = categoryRepository.findById(id);
        if (foundCategory.isPresent()) {
            categoryRepository.deleteById(foundCategory.get().getId());
            return "Item is deleted";
        } else {
            return "Not found";
        }
    }

    @Override
    public List<CategoryDto> getAllCategory() {
        List<Category> listCategoty = categoryRepository.findAll();
        List<CategoryDto> listCategoryDto = new ArrayList<>();
        listCategoty.forEach(category -> {
            listCategoryDto.add(new CategoryDto().loadFromEntity(category));
        });
        return listCategoryDto;
    }

    @Override
    public CategoryDto getCategory(Long id) {
        // Define required variables
        List<Category> listChildren = new ArrayList<>();
        // Find item
        Optional<Category> foundCategory = categoryRepository.findById(id);
        // if Item is existed
        if (foundCategory.isPresent()) {
            //load item's parent and child
            //Category parent = categoryRepository.getById(foundCategory.get().getParent().getId());
            listChildren = categoryRepository.findByParentId(id);
            if (listChildren.isEmpty()) {
                return new CategoryDto().loadFromEntity(foundCategory.get());
            } else {
                return new CategoryDto().loadFromEntityWithChildren(foundCategory.get(), listChildren);
            }
        } else {
            return null;
        }

    }

    @Override
    public CategoryDto getCategoryTemplate() {
        return new CategoryDto();
    }

    @Override
    public CategoryDto updateCategory(Long id, Category category) {
        Optional<Category> foundCategory = categoryRepository.findById(id);
        if (foundCategory.isPresent()) {
            foundCategory.get().update(category);
            categoryRepository.save(foundCategory.get());
            return getCategory(id);
        } else {
            return null;
        }
    }

}
