package app.budgetmanager.mapper;

import app.budgetmanager.dto.ExpenseFilterView;
import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.model.entity.Expense;
import app.budgetmanager.model.entity.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "category", source = "category.name")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "walletName", source = "wallet.name")
    @Mapping(target = "userId", source = "wallet.user.id")
    @Mapping(target = "userName", source = "wallet.user.username")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagNamesSorted")
    ExpenseResponseDto toExpenseResponseDto(Expense expense);

    @Named("tagNamesSorted")
    default List<String> tagNamesSorted(Set<Tag> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream().map(Tag::getName).sorted().toList();
    }

    default ExpenseResponseDto fromFilterView(ExpenseFilterView row) {
        if (row == null) {
            return null;
        }
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(row.getId());
        dto.setDescription(row.getDescription());
        dto.setAmount(row.getAmount());
        dto.setDate(row.getDate());
        dto.setCategory(row.getCategory());
        dto.setWalletId(row.getWalletId());
        dto.setWalletName(row.getWalletName());
        dto.setUserId(row.getUserId());
        dto.setUserName(row.getUserName());
        dto.setTags(splitTagNames(row.getTagNames()));
        return dto;
    }

    default List<String> splitTagNames(String tagNames) {
        if (tagNames == null || tagNames.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isEmpty())
                .sorted()
                .toList();
    }
}
