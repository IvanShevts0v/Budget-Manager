package app.budgetmanager.mapper;

import app.budgetmanager.dto.CategoryRequestDto;
import app.budgetmanager.dto.NamedResponseDto;
import app.budgetmanager.model.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    NamedResponseDto toNamedResponseDto(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    Category toCategory(CategoryRequestDto dto);
}
