package com.betsol.employeePortal.repository;

import com.betsol.employeePortal.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface UserRepo extends CrudRepository<User,String> {

    @Query(value = "select * from users u where u.email=:email",nativeQuery = true)
    User findByEmailId(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "update users u set u.isadmin=1 where u.userid=:userid",nativeQuery = true)
    void editAdmin( @Param("userid") String userid);
}
