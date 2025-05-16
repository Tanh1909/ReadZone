package vn.tnteco.spring.config.authentication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ICheckPermissionService {

    boolean isPermission(HttpServletRequest request, HttpServletResponse response) throws Exception;

}
