package app.budgetmanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.CategoryRequestDto;
import app.budgetmanager.dto.CategoryResponseDto;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.service.CategoryService;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponseDto> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/by-name/exact")
    public CategoryResponseDto getByNameExact(@RequestParam String name) {
        return categoryService.getByNameExact(name);
    }

    @GetMapping("/by-name")
    public List<CategoryResponseDto> getByName(@RequestParam String name) {
        return categoryService.getByName(name);
    }

    @GetMapping("/{id}")
    public CategoryResponseDto getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public CategoryResponseDto create(@RequestBody Category category) {
        return categoryService.save(category);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @PutMapping("/{id}")
    public CategoryResponseDto putCategory(@PathVariable Long id, @RequestBody CategoryRequestDto dto) {
        return categoryService.update(id, dto);
    }

    @PatchMapping("/{id}")
    public CategoryResponseDto patchCategory(@PathVariable Long id, @RequestBody CategoryRequestDto dto) {
        return categoryService.patch(id, dto);
    }
}
