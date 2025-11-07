package com.springboot.smartContactManager.controller;

import java.security.Principal;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.Email;
import com.springboot.smartContactManager.entities.User;
import com.springboot.smartContactManager.messagehelper.Message;
import com.springboot.smartContactManager.services.EmailService;

import jakarta.servlet.http.HttpSession;


@Controller
public class VerifyMailController {

    private final BCryptPasswordEncoder passwordEncoder;
   
   

    @Autowired
    Email email;

    @Autowired
    EmailService service;

    @Autowired
    UserRepository userRepository;

    Random random = new Random();

    VerifyMailController(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    
    @GetMapping("/forget")
    public String getForgetPage(Model model) {
        
        return "verify_email";
    }

    // for reseting password 
    @PostMapping("/forget")
    public String postForgetPage(Model model,@RequestParam("username") String username,Principal principal,HttpSession session,Email email) {
        try { System.out.println(username);
        model.addAttribute("username", username);
        User user = userRepository.getUserByUserName(username);
        if (user==null) {
            System.out.println("cannot find user");
            session.setAttribute("message", new Message("User Not Found. Please Enter A Valid E-mail !!","alert-danger"));
            return "verify_email";
        }
        else{
            int otp = random.nextInt(9999)+100;
            email.setTo(username);
            email.setFrom("aitechtalks1@gmail.com");
            
            email.setTitle("Smart Contact Manager E-mail Verification");
            email.setText("Hello,\n\nYour One Time Password is "+otp+"\nPlease Copy and paste or enter the code manually.\n\n\nEnjoy Services\nSmart Contact Manager Team.");
            System.out.println(otp);
            boolean result = this.service.sendMail(email.getTo(),email.getFrom(), email.getTitle(), email.getText());
            if (result) {
                session.setAttribute("message", new Message("OTP Sent To Your E-mail!!","alert-success"));
                session.setAttribute("otp",otp);
                session.setAttribute("username",username);
             System.out.println(user);
              return "verify_otp";
            }
            else{
                session.setAttribute("message", new Message("Something Went Wrong","alert-danger"));
                return "verify_email";
            }           
        } 
            
        } catch (Exception e) {
            System.err.println(e);
            session.setAttribute("message", new Message("Something Went Wrong","alert-danger"));
            return "verify_email";
        }
                      
    }

    @PostMapping("/reset-password")
    public String postMethodName(HttpSession session , Principal principal,Model model,@RequestParam("otp") int recievedOtp) {      
        try{
            int originalOtp = (int) session.getAttribute("otp");
        System.out.println(originalOtp);
        if(originalOtp==recievedOtp) {
            session.removeAttribute("message");
            return "reset_password"; 
        } else { 
            
           session.setAttribute("message", new Message("Invalid OTP", "alert-danger")); 
           return "verify_email"; 
        }
    }
    catch(Exception e){
        System.err.println(e);
        session.setAttribute("message", new Message("Something Went Wrong!!", "alert-danger"));
        return "varify_email";
        }
    }

    @PostMapping("/change-password")
    public String postPasswordChange(
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model,
                                 Principal principal, HttpSession session) {
    try {
        String username = (String)session.getAttribute("username");
       User user = userRepository.getUserByUserName(username);
       String password =user.getPassword();
       if(!newPassword.equals(confirmPassword)) {
                        session.setAttribute("message", new Message("New Password and Confirm Password Does Not Matches !!", "alert-danger"));
                        return "reset_password";
         } 
         else if (passwordEncoder.matches(newPassword, password)) {
                        session.setAttribute("message", new Message("New Password Cannot be same as Old Password !!", "alert-danger")); 
                        return "reset_password";                       
                    } 
         if(newPassword.equals(confirmPassword) && !passwordEncoder.matches(newPassword, password)) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                        session.setAttribute("message", new Message("Password Changed Successfully !!", "alert-success"));
                        return "/signin";
         }
         else{
            return "signin";
         }
                                    
        
        
    } catch (Exception e) {
        System.out.println(e);
        session.setAttribute("message", new Message("Something Went Wrong !!", "alert-danger"));
        return "signin";
    }
       
    }

    @GetMapping("/mail-varification")
    public String postVarifyEmail(Model model,@RequestParam("email") String username,Principal principal,HttpSession session,Email email) {
        try { System.out.println(username);
             model.addAttribute("username", username);
             model.addAttribute("user", new User());
            session.removeAttribute("otpSent");
            

            int otp = random.nextInt(9999)+100;
            email.setTo(username);
            email.setFrom("aitechtalks1@gmail.com");
            
            email.setTitle("Smart Contact Manager E-mail Verification");
            email.setText("Hello,\n\nYour One Time Password is "+otp+"\nPlease Copy and paste or enter the code manually.\n\n\nEnjoy Services\nSmart Contact Manager Team.");
            System.out.println(otp);
            boolean result = this.service.sendMail(email.getTo(),email.getFrom(), email.getTitle(), email.getText());
            if (result) {
                session.setAttribute("message", new Message("OTP Sent To Your E-mail!!","alert-success"));
                session.setAttribute("otp",otp);
                session.setAttribute("username",username); 
                session.setAttribute("otpSent", true);
                return "redirect:/signup";   
            }
            else{
                session.setAttribute("message", new Message("Something Went Wrong","alert-danger"));
                return "redirect:/signup";
            }           
        
            
        } catch (Exception e) {
            System.err.println(e);
            session.setAttribute("message", new Message("Something Went Wrong","alert-danger"));
            return "redirect:/signup";
        }
                      
    }

    
        
}
