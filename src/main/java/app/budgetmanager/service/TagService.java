package app.budgetmanager.service;

import app.budgetmanager.cache.ExpenseFilterCache;
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.dto.TagDto;
import app.budgetmanager.mapper.TagMapper;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import app.budgetmanager.repository.ExpenseRepository;
import app.budgetmanager.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ExpenseRepository expenseRepository;
    private final TagMapper mapper;
    private final ExpenseFilterCache expenseFilterCache;

    public NamedResponseDto getById(Long id) {
        return mapper.toNamedResponseDto(tagRepository.findById(id).orElseThrow());
    }

    public NamedResponseDto getByName(String name) {
        Tag tag = tagRepository.findByName(name);
        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
        }
        return mapper.toNamedResponseDto(tag);
    }

    public NamedResponseDto save(TagDto tagDto) {
        return mapper.toNamedResponseDto(tagRepository.save(mapper.toTag(tagDto)));
    }

    public NamedResponseDto patch(Long id, TagDto dto) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        if (dto.getName() != null) {
            tag.setName(dto.getName());
        }
        NamedResponseDto response = mapper.toNamedResponseDto(tagRepository.save(tag));
        expenseFilterCache.invalidate();
        return response;
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = tagRepository.findById(id).orElseThrow();
        List<Expense> expenses = expenseRepository.findByTagId(id);
        for (Expense expense : expenses) {
            expense.getTags().remove(tag);
            expenseRepository.save(expense);
        }
        tagRepository.delete(tag);
        expenseFilterCache.invalidate();
    }

    public Page<NamedResponseDto> getAll(Pageable pageable) {
        return tagRepository.findAll(pageable).map(mapper::toNamedResponseDto);
    }

    public Tag getEntityById(Long id) {
        return tagRepository.findById(id).orElseThrow();
    }

    public Tag getEntityByName(String name) {
        return tagRepository.findByName(name);
    }
}
