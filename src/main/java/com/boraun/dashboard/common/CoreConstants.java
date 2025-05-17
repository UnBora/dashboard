package com.boraun.dashboard.common;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoreConstants {
    public final static String WEB_ADMIN_HOME = "/admin";
    public final static String WEB_ADMIN_LOGIN = "/admin/authentication/login";
    public final static String WEB_ADMIN_LOGOUT = "/admin/authentication/logout";
    public final static String WEB_ADMIN_LOGOUT_SUCCESS = WEB_ADMIN_LOGIN + "?logout";
    public final static String WEB_ADMIN_SESSION_EXPIRED = WEB_ADMIN_LOGIN + "?expired";
    public final static String WEB_ADMIN_UNAUTHORIZED_ACCESS = "/admin/unauthorized";
    public static final int BUTTONS_TO_SHOW = 15;
    public static final int INITIAL_PAGE = 0;
    public static final int INITIAL_PAGE_SIZE = 10;
    public static final int[] PAGE_SIZES = {5, 10, 15, 30, 60, 100, 200, 300, 500, 1000};

    public enum Status {
        Disabled,
        Enabled,
        Pending,
        Deleted;

        public static Status[] getStatus(Optional<String[]> stringOptional) {
            return getStatus(stringOptional.orElse(new String[]{"1"}));
        }

        public static Status[] getStatus(String[] values) {
            Status[] reValue = new Status[values.length];
            for (int i = 0; i < values.length; i++) {
                reValue[i] = Status.values()[Integer.parseInt(values[i])];
            }
            return reValue;
        }

        public static List<Status> getStatusesFromValues(String[] values) {
            return Arrays.stream(values)
                    .map(value -> Status.values()[Integer.parseInt(value)])
                    .collect(Collectors.toList());
        }
    }

    public enum ApplicantType {
        Individual,
        LegalEntity
    }

    public enum FieldValueDataType {
        String,
        Integer,
        Long,
        Double,
        BigDecimal,
        Boolean,
        List
    }



    public enum BeneficiaryType {
        Individual,
        LegalEntity
    }

    public enum Gender {
        Male,
        Female
    }


}