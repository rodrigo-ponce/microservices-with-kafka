package com.example.lib.commons.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExampleRequest implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    private String id;

}
