package com.spacecodee.springbootsecurityopentemplate.mappers;

import com.spacecodee.springbootsecurityopentemplate.data.dto.PermissionDTO;
import com.spacecodee.springbootsecurityopentemplate.data.dto.user.details.UserDetailsPermissionDTO;
import com.spacecodee.springbootsecurityopentemplate.persistence.entity.PermissionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IPermissionMapper {

    @Named("toUserDetailsPermissionDTOList")
    default List<UserDetailsPermissionDTO> toUserDetailsPermissionDTOList(Set<PermissionEntity> permissionEntities) {
        if (permissionEntities == null) {
            return Collections.emptyList();
        }

        return permissionEntities.stream()
                .map(this::toBasicPermissionDTO)
                .toList();
    }

    @Named("toBasicPermissionDTO")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "roleDTO", ignore = true)
    @Mapping(target = "operationDTO", source = "operationEntity")
    UserDetailsPermissionDTO toBasicPermissionDTO(PermissionEntity permissionEntity);

    @Mapping(target = "roleEntity", ignore = true)
    PermissionEntity toEntity(PermissionDTO permissionDTO);

    @Mapping(target = "roleDTO.id", source = "roleEntity.id")
    @Mapping(target = "roleDTO.name", source = "roleEntity.name")
    @Mapping(target = "operationDTO", source = "operationEntity")
    PermissionDTO toDto(PermissionEntity entity);
}