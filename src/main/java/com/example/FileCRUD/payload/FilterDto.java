package com.example.FileCRUD.payload;

import lombok.Data;

@Data
public class FilterDto {

    private String searchByName;

    private String type;

    private boolean size1;
    private boolean size2;
    private boolean size3;

    private String dateFrom;
    private String dateUntil;
}
