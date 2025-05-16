package com.boraun.dashboard.admin.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SubAuthorityOrderModel implements Serializable {
    private Long id;
    List<SubAuthorityOrderModel> children;

}
