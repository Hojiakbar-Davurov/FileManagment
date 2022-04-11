package com.example.FileCRUD.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentDto {

    @NotNull
    private Integer id;

    @NotNull
    private String name;

    @NotNull
    private String type;

    @NotNull
    private long size;

    @NotNull
    private String createdAt;
}
