package app.budgetmanager.mapper;

import app.budgetmanager.dto.UserRequestDto;
import app.budgetmanager.dto.UserResponseDto;
import app.budgetmanager.model.entity.User;
import app.budgetmanager.model.entity.Wallet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "walletIds", source = "wallets", qualifiedByName = "mapWalletsToIds")
    UserResponseDto toUserResponseDto(User user);

    @Named("mapWalletsToIds")
    default List<Long> mapWalletsToIds(List<Wallet> wallets) {
        if (wallets == null) {
            return Collections.emptyList();
        }
        return wallets.stream().map(Wallet::getId).toList();
    }

    @BeanMapping(ignoreUnmappedSourceProperties = "defaultWalletName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallets", ignore = true)
    User toUser(UserRequestDto dto);
}
