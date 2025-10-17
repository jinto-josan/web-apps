package com.youtube.common.domain;

/**
 * Interface for mapping between DTOs and domain objects.
 * Provides a contract for object transformation.
 * 
 * @param <S> the source type
 * @param <T> the target type
 */
public interface Mapper<S, T> {
    
    /**
     * Maps a source object to a target DTO.
     * 
     * @param source the source object
     * @return the mapped DTO
     */
    T toDto(S source);
    
    /**
     * Maps a DTO to a source object.
     * 
     * @param dto the DTO
     * @return the mapped source object
     */
    S fromDto(T dto);
}
