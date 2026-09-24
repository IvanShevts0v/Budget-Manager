package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.dto.TagDto;
import app.budgetmanager.mapper.TagMapper;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import app.budgetmanager.repository.ExpenseRepository;
import app.budgetmanager.repository.TagRepository;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private TagMapper mapper;

    private TagService service;

    @BeforeEach
    void setUp() {
        service = new TagService(tagRepository, expenseRepository, mapper, new ExpenseFilterCache());
    }

    @Test // чтение по идентификатору
    void getByIdShouldMapTag() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(mapper.toNamedResponseDto(tag)).thenReturn(new NamedResponseDto(1L, "важно"));

        assertEquals("важно", service.getById(1L).getName());
    }

    @Test // чтение по имени
    void getByNameShouldMapTag() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findByName("важно")).thenReturn(tag);
        when(mapper.toNamedResponseDto(tag)).thenReturn(new NamedResponseDto(1L, "важно"));

        assertEquals("важно", service.getByName("важно").getName());
    }

    @Test // имя не найдено
    void getByNameShouldThrowWhenMissing() {
        when(tagRepository.findByName("важно")).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> service.getByName("важно"));
    }

    @Test // создание
    void saveShouldPersistMappedTag() {
        TagDto request = new TagDto("важно");
        Tag mapped = new Tag(null, "важно");
        Tag saved = new Tag(1L, "важно");
        when(mapper.toTag(request)).thenReturn(mapped);
        when(tagRepository.save(mapped)).thenReturn(saved);
        when(mapper.toNamedResponseDto(saved)).thenReturn(new NamedResponseDto(1L, "важно"));

        assertEquals(1L, service.save(request).getId());
    }

    @Test // частичное обновление имени
    void patchShouldChangeNameWhenProvided() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.toNamedResponseDto(tag)).thenReturn(new NamedResponseDto(1L, "срочно"));

        service.patch(1L, new TagDto("срочно"));

        assertEquals("срочно", tag.getName());
    }

    @Test // частичное обновление без имени
    void patchShouldKeepNameWhenNull() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(mapper.toNamedResponseDto(tag)).thenReturn(new NamedResponseDto(1L, "важно"));

        service.patch(1L, new TagDto());

        assertEquals("важно", tag.getName());
    }

    @Test // удаление с отвязкой от связанных записей
    void deleteShouldDetachFromExpenses() {
        Tag tag = new Tag(1L, "важно");
        Expense expense = new Expense();
        expense.getTags().add(tag);
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(expenseRepository.findByTagId(1L)).thenReturn(List.of(expense));

        service.delete(1L);

        assertFalse(expense.getTags().contains(tag));
        verify(expenseRepository).save(expense);
        verify(tagRepository).delete(tag);
    }

    @Test // полный список
    void getAllShouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(tag)));
        when(mapper.toNamedResponseDto(tag)).thenReturn(new NamedResponseDto(1L, "важно"));

        assertEquals(1, service.getAll(pageable).getTotalElements());
    }

    @Test // получение сущности по id
    void getEntityByIdShouldReturnTag() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        assertEquals(tag, service.getEntityById(1L));
    }

    @Test // получение сущности по имени
    void getEntityByNameShouldReturnTagOrNull() {
        Tag tag = new Tag(1L, "важно");
        when(tagRepository.findByName("важно")).thenReturn(tag);
        when(tagRepository.findByName("нет")).thenReturn(null);

        assertEquals(tag, service.getEntityByName("важно"));
        assertNull(service.getEntityByName("нет"));
    }

    @Test // чтение отсутствующей записи
    void getByIdShouldThrowWhenMissing() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(1L));
    }

    @Test // удаление отсутствующей записи
    void deleteShouldThrowWhenMissing() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.delete(1L));
        verify(tagRepository, never()).delete(any());
    }

    @Test // получение отсутствующей сущности
    void getEntityByIdShouldThrowWhenMissing() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getEntityById(1L));
    }

    @Test // частичное обновление отсутствующей записи
    void patchShouldThrowWhenMissing() {
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        TagDto request = new TagDto("срочно");
        assertThrows(NoSuchElementException.class, () -> service.patch(1L, request));
    }
}
