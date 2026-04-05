package app.budgetmanager.service;

import app.budgetmanager.dto.CategoryRequestDto;
import app.budgetmanager.dto.CategoryResponseDto;
import app.budgetmanager.mapper.CategoryMapper;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.repository.CategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper mapper) {
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    public CategoryResponseDto getById(Long id) {
        return mapper.toCategoryResponseDto(categoryRepository.findById(id).orElseThrow());
    }

    public CategoryResponseDto getByNameExact(String name) {
        List<Category> found = categoryRepository.findByName(name);
        if (found.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return mapper.toCategoryResponseDto(found.get(0));
    }

    public List<CategoryResponseDto> getByName(String name) {
        return categoryRepository.findByName(name).stream().map(mapper::toCategoryResponseDto).toList();
    }

    public CategoryResponseDto save(Category category) {
        return mapper.toCategoryResponseDto(categoryRepository.save(category));
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public List<CategoryResponseDto> getAll() {
        return categoryRepository.findAll().stream().map(mapper::toCategoryResponseDto).toList();
    }

    public CategoryResponseDto update(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(dto.getName());
        return mapper.toCategoryResponseDto(categoryRepository.save(category));
    }

    public CategoryResponseDto patch(Long id, CategoryRequestDto dto) {
        Category category = categoryRepository.findById(id).orElseThrow();
        if (dto.getName() != null) {
            category.setName(dto.getName());
        }
        return mapper.toCategoryResponseDto(categoryRepository.save(category));
    }

    public Category getEntityById(Long id) {
        return categoryRepository.findById(id).orElseThrow();
    }

    public Category getEntityByName(String name) {
        List<Category> found = categoryRepository.findByName(name);
        return found.isEmpty() ? null : found.get(0);
    }
}
