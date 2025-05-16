package vn.tnteco.common.service;

import io.reactivex.rxjava3.core.Single;
import org.springframework.stereotype.Service;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.core.model.UserPrincipal;
import vn.tnteco.common.data.constant.ErrorResponseBase;
import vn.tnteco.common.data.dto.PermissionDto;

import java.util.List;

@Service
public class DefaultPermissionServiceImpl implements IPermissionCommonService {
    @Override
    public Single<List<PermissionDto>> getPermissionsOfMenu(UserPrincipal userPrincipal) {
        return Single.error(new ApiException(ErrorResponseBase.BUSINESS_ERROR));
    }

    @Override
    public Single<Boolean> isPermitByAction(UserPrincipal userPrincipal) {
        return Single.error(new ApiException(ErrorResponseBase.BUSINESS_ERROR));
    }
}
