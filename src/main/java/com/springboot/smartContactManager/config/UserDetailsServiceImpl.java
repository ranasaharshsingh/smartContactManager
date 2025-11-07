package com.springboot.smartContactManager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.User;

public class UserDetailsServiceImpl implements UserDetailsService{

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.getUserByUserName(username);

            if(user==null)
            {
                throw new UsernameNotFoundException("Could not found user!!");
            }

            CustomUserDetails customUserDetails = new CustomUserDetails(user);

        return customUserDetails;
    }
    
}
