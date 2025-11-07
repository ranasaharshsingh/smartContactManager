package com.springboot.smartContactManager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.User;
import com.springboot.smartContactManager.messagehelper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {

    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepositoy;

    @GetMapping("/")
    public String getHome(Model m) {

        m.addAttribute("title", "Home:Smart Contact Manager!!");
        // user.setName("Raahul");
        // user.setEmail("rahul@gmail.com");

        // Contact contact = new Contact();
        // user.getContact().add(contact);
        // userRepositoy.save(user);

        return "home";
    }
    
    @GetMapping("/about")
    public String getAbout(Model m) {
        m.addAttribute("title", "About:Smart Contact Manager!!");
        return "about";
    }

    @GetMapping("/signup")
    public String getsignup(Model m,HttpSession session) {
        m.addAttribute("title", "Register:Smart Contact Manager!!");
        m.addAttribute("user", new User());
        
        return "signup";
    }

    @GetMapping("/signin")
    public String getMethodName(Model m ,HttpSession session) {
        m.addAttribute("title", "Login:Smart Contact Manager!!");
        session.removeAttribute("message");
        return "signin";
    }
    
    

    // This is handler for regidtering User
    @PostMapping("/do_register")
    public String postMethodName(@Valid @ModelAttribute("user") User user,  @RequestParam(value = "agreement",defaultValue = "false") boolean agreement ,BindingResult result1,Model m ,HttpSession session,@RequestParam("otp") int recievedOtp) {
       try {
            int originalOtp = (int) session.getAttribute("otp");
            System.out.println(originalOtp);
            session.removeAttribute("otpSent");
            if (!agreement) {
                        System.out.println("You have not agreed terms and conditions.");
                        throw new Exception("You have not agreed terms and conditions.");
            }
                    // server side validation
                    if (result1.hasErrors()) {
                        System.out.println("ERROR"+result1.toString());
                        m.addAttribute("user", user);
                        return "signup";
                    }


                    user.setEnabled(true);
                    user.setRole("ROLE_USER");


                    // impotant for Password Encoding
                    user.setPassword(passwordEncoder.encode(user.getPassword()));

                    user.setProfileUrl("default.png");
                    System.out.println("user:"+user);
                    m.addAttribute("user", user);
                    if (originalOtp==recievedOtp) {
                         User result= userRepositoy.save(user);
                    System.out.println(result);

                    session.setAttribute("message", new Message("Successfully Registered!!","alert-success"));
                    System.out.println(agreement);
                    return "signup";
                    }
                    else{
                        session.setAttribute("message", new Message("Incorrect OTP !!","alert-danger"));
                        return "signup";
                    }
                   

       } catch (Exception e) {
        
        e.printStackTrace();
        m.addAttribute("user", user);
        String errorMessage = e.getMessage().contains("You have not agreed terms and conditions.")
            ? "You must agree to the terms and conditions!"
            : "This email is already registered or another error occurred.";
        session.setAttribute("message",new Message(errorMessage,"alert-danger") );
        return"signup";

       }

        
    }
    
    
    
    
}

