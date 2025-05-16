package com.example.app.data.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AddressModel {

    private String province;

    private String district;

    private String ward;

    private String detail;

    private String receiverName;

    private String receiverPhone;
}
