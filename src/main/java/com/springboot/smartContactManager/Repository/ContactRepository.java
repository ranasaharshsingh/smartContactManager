package com.springboot.smartContactManager.Repository;



import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.smartContactManager.entities.Contact;
import com.springboot.smartContactManager.entities.User;

public interface ContactRepository extends JpaRepository<Contact,Integer> {

    @Query("from Contact as c where c.user.id = :UserId ")
    public Page<Contact> findContactsByUserId(@RequestParam("userId")int UserId,Pageable pageable);
    
    @Query("select c from Contact c where c.name like :name% and c.user.id = :userId")
    public Page<Contact> findByNameContainingAndUserId(@RequestParam("name") String name, @RequestParam("userId") int userId, Pageable pageable); 
    
    public List<Contact> findByNameContainingAndUser(String name, User user);

    List<Contact> findContactsByUser(User user);

    boolean existsByPhoneNumberAndUser(String phoneNumber, User user);

}
