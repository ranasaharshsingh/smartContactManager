package com.springboot.smartContactManager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.springboot.smartContactManager.Repository.ContactRepository;
import com.springboot.smartContactManager.Repository.PaymentRepository;
import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.Contact;
import com.springboot.smartContactManager.entities.PaymentData;
import com.springboot.smartContactManager.entities.User;
import com.springboot.smartContactManager.messagehelper.Message;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.razorpay.*;



@Controller
@RequestMapping("/user")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder;
     @Autowired
    UserRepository userRepository;
    @Autowired
    ContactRepository contactRepository;

    @Autowired
    PaymentRepository paymentRepository;


    UserController(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    // this @ helps to add thsi function to all the handlers 
    @ModelAttribute
    public void addCommonData(Model model , Principal principal)
    {
        String emailString = principal.getName();
        
        User user = userRepository.getUserByUserName(emailString);
        
        model.addAttribute("user", user);
    }

    // dashboard handler
    @GetMapping("/dashboard")
    public String dashboard(Model model,Principal principal) {
        User user = userRepository.getUserByUserName(principal.getName());
        List<Contact> contact = user.getContact();
        long totalContacts =contact.size();
        model.addAttribute("title", "Dashboard Page");
        model.addAttribute("totalContacts", totalContacts);
        return "normalUser/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String getAddContact(Model model,Principal principal,HttpSession session) {
        model.addAttribute("title", "Add Contact Page");
        model.addAttribute("contact", new Contact());
        session.removeAttribute("message");
        return "normalUser/add_contact";
    }
    
    @PostMapping("/process-contact")
    public String addContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
                               HttpSession session, Principal  principal) {
        
    try {
        
       System.out.println(contact);
       String name = principal.getName(); 
       User user = userRepository.getUserByUserName(name);
        contact.setUser(user);


        // saving and procecessing image 
        if (file.isEmpty()) {
            System.out.println("No image Found");
            contact.setImageUrl("contact.png");
            
        } else {
            
        contact.setImageUrl(name+file.getOriginalFilename());
         File saveFile= new ClassPathResource("/static/img").getFile();
         Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+name+file.getOriginalFilename());
         Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image Saved Successfully.");
        }
        user.getContact().add(contact);

        userRepository.save(user);

        session.setAttribute("message", new Message("Contact Saved Successfully !! ...Add More...","alert-success"));

    } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
        session.setAttribute("message", new Message("Something went wrong while saving contact!", "alert-danger"));
    }
        
        return "normalUser/add_contact";
        
        
    }

    @GetMapping("/show-contacts/{page}")
    public String getMethodName(Model m ,Principal  principal,Pageable pageable,@PathVariable("page") int page,HttpSession session) {

        m.addAttribute("title", "Show Contact Page");
        User user= userRepository.getUserByUserName( principal.getName());
        int userId= user.getId();

        // paginantion where
        //  page = current page 
        //   page size[5] = total no. of contact per page 
         pageable= PageRequest.of(page,5);
        Page<Contact> contacts = contactRepository.findContactsByUserId(userId,pageable);
        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());
        session.removeAttribute("message");
        return "normalUser/show_contact";
    }
    
    @GetMapping("/show-contact/contact/{customerId}")
    public String showContact(@PathVariable("customerId") int customerId,Model model,Principal principal) {
        Optional<Contact> cOptional = contactRepository.findById(customerId);
        Contact contact = cOptional.get();
        User user=userRepository.getUserByUserName(principal.getName());
        if (user.getId()== contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title",contact.getName());

        }

        
        return "normalUser/contact_details";
    }
    
    
    @GetMapping("/show-contacts/contact/delete/{customerId}")
    public String deleteContact(@PathVariable("customerId") int customerId, Model model, Principal principal, HttpSession session) {
    Optional<Contact> contactOptional = contactRepository.findById(customerId);
    User user = userRepository.getUserByUserName(principal.getName());

    if (contactOptional.isPresent()) {
        Contact contact = contactOptional.get();
        if (user.getId() == contact.getUser().getId()) {
            contact.setUser(null);
            user.setContact(null); // Remove contact from user
            userRepository.save(user); // Save user to update the relationship
            contactRepository.delete(contact);
            session.setAttribute("message", new Message("Contact deleted successfully!", "alert-success"));
        } else {
            session.setAttribute("message", new Message("You are not authorized to delete this contact.", "alert-danger"));
        }
    } else {
        session.setAttribute("message", new Message("Contact not found.", "alert-danger"));
    }
    return "redirect:/user/show-contacts/0";

}


    @PostMapping("/update-contact/{customerId}")
    public String updateContactForm(@PathVariable("customerId") int customerId,Model model, Principal principal) {
    model.addAttribute("title", "Update Contact");
    Optional<Contact> contactOptional = contactRepository.findById(customerId);
    Contact contact = contactOptional.get();
    
    User user = userRepository.getUserByUserName(principal.getName());  
        if (user.getId()== contact.getUser().getId()) {
            model.addAttribute("contact", contact);  
        }
                 
     return "normalUser/contact_details_update";
    }
    @PostMapping("/update-contact")
    public String updateContact(Model model,HttpSession session, 
                                @RequestParam("profileImage") MultipartFile file,
                                Principal principal,@RequestParam("customerId") int customerId,
                                @ModelAttribute("contact") Contact contact) {
       try {
        User user= userRepository.getUserByUserName(principal.getName()); 
        Contact oldContact = contactRepository.getById(customerId);
        // saving and processing image
        if (!file.isEmpty()) {
            // delete old image          
                File deleteFile = new ClassPathResource("/static/img").getFile();
                File file1 = new File(deleteFile, file.getOriginalFilename());
                file1.delete();
            System.out.println("Old image deleted successfully.");
            // set new image
            contact.setImageUrl(user.getEmail() + file.getOriginalFilename());
            File saveFile = new ClassPathResource("/static/img").getFile();
            Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + user.getEmail() + file.getOriginalFilename());
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image saved successfully.");
        }
        else{
            contact.setImageUrl(oldContact.getImageUrl());
        }
        
        contact.setUser(user);
        // save contact
        contactRepository.save(contact);
        session.setAttribute("message", new Message("Contact updated successfully!", "alert-success"));
    } catch (Exception e) {
         e.printStackTrace();
         session.setAttribute("message", new Message("Something went wrong while updating contact!", "alert-danger"));
         System.out.println("Error: " + e.getMessage());
       }
        
      
        return "redirect:/user/show-contact/contact/"+customerId;
                                }
    // handler for user profile page
    @GetMapping("/user-profile")
    public String getuUsetProfle(Model model) {
        return "normalUser/user_profile";
    }
    
    @GetMapping("/search-contacts")
    public String getMethodName( Model model, Principal principal, HttpSession session,@RequestParam(value = "filter", required = false) String filter,@RequestParam(value = "page", defaultValue = "0") int page) {
        System.out.println("Filter: " + filter);
        // Default page number
        model.addAttribute("title", "Search Contact Page");
        User user= userRepository.getUserByUserName(principal.getName());
        int userId = user.getId();
        Pageable pageable = PageRequest.of(page, 5);
        Page<Contact> contacts;
        if (filter != null && !filter.isEmpty()) {
            contacts = contactRepository.findByNameContainingAndUserId(filter, userId,pageable);
                if (contacts.isEmpty()) {
                    session.setAttribute("message", new Message("No contacts found matching the search criteria.", "alert-info"));
                } else {                  
                    session.removeAttribute("message");   
                                     
                }
        }
        else {
            contacts = contactRepository.findContactsByUserId(user.getId(), pageable);
            if (contacts.isEmpty()) {
                session.setAttribute("message", new Message("No contacts found.", "alert-info"));
            } else {
                session.removeAttribute("message");
                
            }
        }
        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages()); 
        model.addAttribute("filter", filter); 
        
        
        return "normalUser/search_contacts";

    }

    @GetMapping("/user-settings")
    public String getUserSettings(Model model, HttpSession session) {
        session.removeAttribute("message");
        model.addAttribute("title", "User Settings");
        return "normalUser/user_settings";
    }
    @GetMapping("/user-settings/change-name")
    public String getMethodName(Model model) 
    {
        model.addAttribute("title", "Change Email");
        return "normalUser/change_name";
    }

    @PostMapping("/user-settings/change-name")
    public String postMethodName(@RequestParam("newName") String newName,
                                 Model model,
                                 Principal principal, HttpSession session) {
        try {
        
                    User user = userRepository.getUserByUserName(principal.getName());
                    String name =user.getName();
                    
                    if (newName.equals(name) ) {
                        session.setAttribute("message", new Message("New Name Cannot be same as Old Name !!", "alert-danger"));
                         
                    } 
                    else{
                        user.setName(newName);
                        userRepository.save(user);
                        session.setAttribute("message", new Message("Name Changed Successfully!!","alert-success"));
                    }

                
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong while changing Name!", "alert-danger"));
        }

        
        return "redirect:/user/user-settings/change-name";
    }
    
    

    @GetMapping("/user-settings/change-password")
    public String getPasswordChange(Model model) {
        
        model.addAttribute("title", "Change Password");
        return "normalUser/change_password";
    }
    
    @PostMapping("/user-settings/change-password")
    public String postPasswordChange(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 Model model,
                                 Principal principal, HttpSession session) {
        try {
        
                    User user = userRepository.getUserByUserName(principal.getName());
                    String password =user.getPassword();
                    System.out.println("Password"+password);
                    System.out.println("Old currentPassword: " + currentPassword);   
                    System.out.println("New Password: " + newPassword);
                    System.out.println("Confirm Password: " + confirmPassword); 
                    if (!passwordEncoder.matches(currentPassword,password)) {
                        session.setAttribute("message", new Message("Current Password Does Not Matches !!", "alert-danger"));
                        
                    } 
                    else if(!newPassword.equals(confirmPassword)) {
                        session.setAttribute("message", new Message("New Password and Confirm Password Does Not Matches !!", "alert-danger"));
                        
                    } 
                    else if (passwordEncoder.matches(newPassword, password )) {
                        session.setAttribute("message", new Message("New Password Cannot be same as Old Password !!", "alert-danger"));
                         
                    } 
                    if(passwordEncoder.matches(currentPassword, password)&& newPassword.equals(confirmPassword) && !passwordEncoder.matches(newPassword, password)) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                        session.setAttribute("message", new Message("Password Changed Successfully !!", "alert-success"));
                    }

                
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong while changing password!", "alert-danger"));
        }

        
        return "redirect:/user/user-settings/change-password";
    }
    
    // creating order for payment
    @PostMapping("/create-order")
    @ResponseBody
    public String postCreateOrder(@RequestBody Map<String,Object> data,Principal principal) throws Exception {
        
        System.out.println(data);
        int amt=Integer.parseInt(data.get("amount").toString());
        var client =new RazorpayClient("rzp_test_RBGjpTKqPP5Lmi","WlMXcWNyLUIgIu5g2GyLagxA");

        JSONObject option = new JSONObject();
        option.put("amount", amt*100);//we have to put in paise therefore amt*100
        option.put("currency", "INR");
        option.put("receipt", "txn_235425");

        // creating order 
        Order order = client.orders.create(option);
        System.out.println(order);

        // save data at database
        PaymentData paymentData = new PaymentData();
        paymentData.setOrderId(order.get("id"));
        int amount = order.get("amount");
        paymentData.setAmount(amount/100);
        paymentData.setReceipt(order.get("receipt"));
        paymentData.setStatus(order.get("status"));
        User user =userRepository.getUserByUserName(principal.getName());
        paymentData.setUser(user);
        paymentData.setUserName(user.getName());

        paymentRepository.save(paymentData);



        return order.toString();
    }
    
    @PostMapping("/update-order")
    public ResponseEntity<?> postUpdateServer(@RequestBody Map<String,Object> data) {
        
        PaymentData paymentData= this.paymentRepository.findByOrderId(data.get("order_id").toString());
        paymentData.setPaymentId(data.get("payment_id").toString());
        paymentData.setStatus(data.get("status").toString());

        paymentRepository.save(paymentData);
        System.out.println(data);
        return ResponseEntity.ok(Map.of("msg","updated"));
    }
    
}

