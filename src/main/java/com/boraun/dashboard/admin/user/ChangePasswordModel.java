package com.boraun.dashboard.admin.user;

import com.boraun.dashboard.common.BaseEntity;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ChangePasswordModel extends BaseEntity {
    @NotEmpty(message = "Current Password is required")
    private String currentPassword;

    @NotEmpty(message = "New Password is required")
    private String newPassword;

    @NotEmpty(message = "Confirm Password is required")
    private String confirmPassword;

}
