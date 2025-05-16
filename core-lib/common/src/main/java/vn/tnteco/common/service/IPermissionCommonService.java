package vn.tnteco.common.service;

import io.reactivex.rxjava3.core.Single;
import vn.tnteco.common.core.model.UserPrincipal;
import vn.tnteco.common.data.dto.PermissionDto;

import java.util.List;

public interface IPermissionCommonService {
    Single<List<PermissionDto>> getPermissionsOfMenu(UserPrincipal userPrincipal);

    Single<Boolean> isPermitByAction(UserPrincipal userPrincipal);
}
