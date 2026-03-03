package app.budgetmanager.mapper;

import app.budgetmanager.dto.ExpenseResponseDto;
import app.budgetmanager.entity.Expense;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    ExpenseResponseDto toDto(Expense expense);
}
