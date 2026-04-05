package app.budgetmanager.mapper;

import app.budgetmanager.dto.TagDto;
import app.budgetmanager.dto.TagResponseDto;
import app.budgetmanager.model.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagResponseDto toTagResponseDto(Tag tag);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    Tag toTag(TagDto dto);
}
