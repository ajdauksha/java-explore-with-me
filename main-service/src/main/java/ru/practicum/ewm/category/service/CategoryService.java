package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.DataConflictException;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category createCategory(Category category) {
        log.info("Creating category: {}", category.getName());

        if (categoryRepository.existsByName(category.getName())) {
            throw new DataConflictException("Category with name " + category.getName() + " already exists");
        }

        return categoryRepository.save(category);
    }

    public List<Category> getCategories(Integer from, Integer size) {
        log.info("Getting categories from: {}, size: {}", from, size);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return categoryRepository.findAll(pageable).getContent();
    }

    public Category getCategoryById(Long categoryId) {
        log.info("Getting category by id: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found with id: " + categoryId));
    }

    @Transactional
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        log.info("Updating category with id: {}", categoryId);

        Category existingCategory = getCategoryById(categoryId);

        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
                categoryRepository.existsByName(updatedCategory.getName())) {
            throw new DataConflictException("Category with name " + updatedCategory.getName() + " already exists");
        }

        existingCategory.setName(updatedCategory.getName());
        return categoryRepository.save(existingCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with id: {}", categoryId);

        if (categoryRepository.existsEventsByCategoryId(categoryId)) {
            throw new DataConflictException("Cannot delete category with associated events");
        }

        categoryRepository.deleteById(categoryId);
    }
}