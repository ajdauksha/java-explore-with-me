package ru.practicum.ewm.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.validation.Create;
import ru.practicum.ewm.validation.Update;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    @Null(groups = {Create.class, Update.class})
    private Long id;

    @NotBlank(groups = {Create.class, Update.class})
    @Size(min = 1, max = 50, groups = {Create.class, Update.class})
    private String name;

}