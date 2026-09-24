package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.CategoryRequestDto;
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.mapper.CategoryMapper;
import app.budgetmanager.model.entity.Category;
import app.budgetmanager.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper mapper;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categoryRepository, mapper, new ExpenseFilterCache());
    }

    @Test // чтение по идентификатору
    void getByIdShouldMapCategory() {
        Category category = new Category(1L, "еда");
        NamedResponseDto response = new NamedResponseDto(1L, "еда");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toNamedResponseDto(category)).thenReturn(response);

        assertEquals(response, service.getById(1L));
    }

    @Test // точное совпадение имени
    void getByNameExactShouldReturnFirstMatch() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findByName("еда")).thenReturn(List.of(category));
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "еда"));

        assertEquals("еда", service.getByNameExact("еда").getName());
    }

    @Test // точное имя не найдено
    void getByNameExactShouldThrowWhenEmpty() {
        when(categoryRepository.findByName("еда")).thenReturn(List.of());

        assertThrows(ResponseStatusException.class, () -> service.getByNameExact("еда"));
    }

    @Test // поиск по имени со страницей
    void getByNameShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category(1L, "еда");
        when(categoryRepository.findByName("еда", pageable)).thenReturn(new PageImpl<>(List.of(category)));
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "еда"));

        assertEquals(1, service.getByName("еда", pageable).getContent().size());
    }

    @Test // создание
    void createShouldSaveMappedEntity() {
        CategoryRequestDto request = new CategoryRequestDto("еда");
        Category category = new Category(null, "еда");
        Category saved = new Category(1L, "еда");
        when(mapper.toCategory(request)).thenReturn(category);
        when(categoryRepository.save(category)).thenReturn(saved);
        when(mapper.toNamedResponseDto(saved)).thenReturn(new NamedResponseDto(1L, "еда"));

        assertEquals(1L, service.create(request).getId());
    }

    @Test // удаление
    void deleteShouldRemoveCategory() {
        service.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test // полный список
    void getAllShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Category category = new Category(1L, "еда");
        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category)));
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "еда"));

        assertEquals(1, service.getAll(pageable).getTotalElements());
    }

    @Test // полная замена имени
    void updateShouldChangeName() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "кафе"));

        assertEquals("кафе", service.update(1L, new CategoryRequestDto("кафе")).getName());
        assertEquals("кафе", category.getName());
    }

    @Test // частичное обновление имени
    void patchShouldChangeNameWhenProvided() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "кафе"));

        service.patch(1L, new CategoryRequestDto("кафе"));

        assertEquals("кафе", category.getName());
    }

    @Test // частичное обновление без имени
    void patchShouldKeepNameWhenNull() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);
        when(mapper.toNamedResponseDto(category)).thenReturn(new NamedResponseDto(1L, "еда"));

        service.patch(1L, new CategoryRequestDto());

        assertEquals("еда", category.getName());
    }

    @Test // получение сущности по id
    void getEntityByIdShouldReturnCategory() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertEquals(category, service.getEntityById(1L));
    }

    @Test // получение сущности по имени
    void getEntityByNameShouldReturnFirstOrNull() {
        Category category = new Category(1L, "еда");
        when(categoryRepository.findByName("еда")).thenReturn(List.of(category));
        when(categoryRepository.findByName("нет")).thenReturn(List.of());

        assertEquals(category, service.getEntityByName("еда"));
        assertNull(service.getEntityByName("нет"));
    }

    @Test // замена отсутствующей записи
    void updateShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.update(1L, new CategoryRequestDto("кафе")));
    }

    @Test // чтение отсутствующей записи
    void getByIdShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(1L));
    }

    @Test // получение отсутствующей сущности
    void getEntityByIdShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getEntityById(1L));
    }

    @Test // частичное обновление отсутствующей записи
    void patchShouldThrowWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.patch(1L, new CategoryRequestDto("кафе")));
    }
}
