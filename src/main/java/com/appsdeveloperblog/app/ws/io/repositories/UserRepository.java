package com.appsdeveloperblog.app.ws.io.repositories;

//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.appsdeveloperblog.app.ws.io.entity.UserEntity;


//the interface CrudRepository takes the parameters
//<Class which needs to be persisted, datatype of ID>
@Repository
public interface UserRepository extends PagingAndSortingRepository<UserEntity,Long> {
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
	UserEntity findUserByEmailVerificationToken(String token);
}
