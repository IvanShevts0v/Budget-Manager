package app.budgetmanager.mapper;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "userId", source = "wallet.user.id")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagNamesSorted")
    ExpenseResponseDto toExpenseResponseDto(Expense expense);

    @Named("tagNamesSorted")
    default List<String> tagNamesSorted(Set<Tag> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream().map(Tag::getName).sorted().toList();
    }
}
