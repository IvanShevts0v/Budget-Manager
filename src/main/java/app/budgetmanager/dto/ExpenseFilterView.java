package app.budgetmanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ExpenseFilterView {

    Long getId();

    String getDescription();

    BigDecimal getAmount();

    LocalDate getDate();

    String getCategory();

    Long getWalletId();

    String getWalletName();

    Long getUserId();

    String getUserName();

    String getTagNames();
}
