package com.betsol.employeePortal.repository;

import com.betsol.employeePortal.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends CrudRepository<User,String> {

    @Query(value = "select * from users u where u.email=:email",nativeQuery = true)
    User findByEmailId(@Param("email") String email);
}
