package app.budgetmanager.mapper;

import app.budgetmanager.dto.WalletRequestDto;
import app.budgetmanager.dto.WalletResponseDto;
import app.budgetmanager.model.entity.Wallet;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.username", target = "username")
    WalletResponseDto toWalletResponseDto(Wallet wallet);

    @BeanMapping(ignoreUnmappedSourceProperties = "userId")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    Wallet toWallet(WalletRequestDto dto);
}
