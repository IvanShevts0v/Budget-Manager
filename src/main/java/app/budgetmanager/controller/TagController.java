package app.budgetmanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.dto.TagDto;
import app.budgetmanager.service.TagService;

@RestController
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public List<NamedResponseDto> getAll() {
        return tagService.getAll();
    }

    @GetMapping("/by-name")
    public NamedResponseDto getByName(@RequestParam String name) {
        return tagService.getByName(name);
    }

    @GetMapping("/{id}")
    public NamedResponseDto getById(@PathVariable Long id) {
        return tagService.getById(id);
    }

    @PostMapping
    public NamedResponseDto create(@RequestBody TagDto dto) {
        return tagService.save(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        tagService.delete(id);
    }
}
