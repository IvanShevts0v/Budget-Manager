package app.budgetmanager.service;

import app.budgetmanager.dto.TagDto;
import app.budgetmanager.dto.TagResponseDto;
import app.budgetmanager.mapper.TagMapper;
import app.budgetmanager.model.entity.Tag;
import app.budgetmanager.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper mapper;

    public TagService(TagRepository tagRepository, TagMapper mapper) {
        this.tagRepository = tagRepository;
        this.mapper = mapper;
    }

    public TagResponseDto getById(Long id) {
        return mapper.toTagResponseDto(tagRepository.findById(id).orElseThrow());
    }

    public TagResponseDto getByName(String name) {
        Tag tag = tagRepository.findByName(name);
        if (tag == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found");
        }
        return mapper.toTagResponseDto(tag);
    }

    public TagResponseDto save(TagDto tagDto) {
        return mapper.toTagResponseDto(tagRepository.save(mapper.toTag(tagDto)));
    }

    public void delete(Long id) {
        tagRepository.deleteById(id);
    }

    public List<TagResponseDto> getAll() {
        return tagRepository.findAll().stream().map(mapper::toTagResponseDto).toList();
    }

    public Tag getEntityById(Long id) {
        return tagRepository.findById(id).orElseThrow();
    }

    public Tag getEntityByName(String name) {
        return tagRepository.findByName(name);
    }
}
