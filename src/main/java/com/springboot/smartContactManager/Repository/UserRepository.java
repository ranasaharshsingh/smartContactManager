package com.springboot.smartContactManager.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.springboot.smartContactManager.entities.User;

public interface UserRepository extends JpaRepository<User,Integer> {

    @Query("select u from User u where email = :email")
    public User getUserByUserName(@Param("email") String email);
    
}
