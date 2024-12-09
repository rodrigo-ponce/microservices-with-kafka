package com.example.lib.commons.bean;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment extends ExampleRequest{

    private double amount;
    private String description;
}
