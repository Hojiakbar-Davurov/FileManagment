package com.example.FileCRUD.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponce {

    private String message;
    private boolean status;
    private Object object;

    public ApiResponce(String message, boolean status) {
        this.message = message;
        this.status = status;
    }
}