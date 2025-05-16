package vn.tnteco.common.data.constant;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageResponse {

    public static final String SUCCESS = "Thành công";
    public static final String FAIL = "Thất bại";
    public static final String RESOURCE_NOT_FOUND = "resource not found!";
    public static final String INSERT_FAIL = "fail to insert!";
    public static final String UPDATE_FAIL = "fail to update!";
    public static final String ID_MUST_NOT_BE_NULL = "ID_MUST_NOT_BE_NULL";
    public static final String CREATED_MUST_NOT_BE_NULL = "CREATED_MUST_NOT_BE_NULL";
    public static final String NOT_PERMISSION = "Bạn không có quyền thực hiện thao tác này";
}
