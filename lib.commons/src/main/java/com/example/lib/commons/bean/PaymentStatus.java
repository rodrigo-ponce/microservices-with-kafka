package com.example.lib.commons.bean;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentStatus extends ExampleResponse{

        private String status;
        private String message;
}
