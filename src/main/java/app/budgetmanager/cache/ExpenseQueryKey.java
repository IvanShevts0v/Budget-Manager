package app.budgetmanager.cache;

import org.springframework.data.domain.Pageable;

import java.util.Objects;

public class ExpenseQueryKey {

    private final Long walletOwnerUserId;
    private final String categoryName;
    private final int page;
    private final int size;
    private final String sort;
    private final boolean useNative;

    public ExpenseQueryKey(
            Long walletOwnerUserId,
            String categoryName,
            Pageable pageable,
            boolean useNative
    ) {
        this.walletOwnerUserId = walletOwnerUserId;
        this.categoryName = categoryName;
        this.page = pageable.getPageNumber();
        this.size = pageable.getPageSize();
        this.sort = pageable.getSort().toString();
        this.useNative = useNative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExpenseQueryKey that = (ExpenseQueryKey) o;
        return page == that.page
                && size == that.size
                && useNative == that.useNative
                && Objects.equals(walletOwnerUserId, that.walletOwnerUserId)
                && Objects.equals(categoryName, that.categoryName)
                && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletOwnerUserId, categoryName, page, size, sort, useNative);
    }
}
