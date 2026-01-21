package com.example.mikusmoneybackend.miku;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * Mapper for converting between Miku entity and DTOs.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MikuMapper {

    /**
     * Converts MikuCreateRequest to Miku entity.
     * Note: Credential-related fields are handled separately in the service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publicCode", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "credential", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "savingsPigs", ignore = true)
    Miku toEntity(MikuCreateRequest request);

    /**
     * Converts Miku entity to MikuResponse DTO.
     */
    MikuResponse toResponse(Miku miku);
}
