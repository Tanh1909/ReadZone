package com.example.app.repository.user;

import com.example.app.data.tables.pojos.User;
import io.reactivex.rxjava3.core.Single;
import vn.tnteco.repository.IRxRepository;

import java.util.Optional;

public interface IRxUserRepository extends IRxRepository<User, Integer> {

    Single<Optional<User>> getByEmail(String username);

    Single<Boolean> existByEmail(String email);

}
