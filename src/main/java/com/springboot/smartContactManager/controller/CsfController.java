package com.springboot.smartContactManager.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.smartContactManager.Repository.ContactRepository;
import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.Contact;
import com.springboot.smartContactManager.entities.User;
import com.springboot.smartContactManager.messagehelper.Message;
import com.springboot.smartContactManager.services.CsfImportServices;

import org.springframework.ui.Model;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Telephone;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class CsfController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CsfImportServices vcfImportService;

    @Autowired
    private ContactRepository contactRepository;

     @ModelAttribute
    public void addCommonData(Model model , Principal principal)
    {
        String emailString = principal.getName();
        System.out.println("username"+emailString);
        User user = userRepository.getUserByUserName(emailString);
        System.out.println("user"+user);
        model.addAttribute("user", user);
    }

    // Just return import page
    @GetMapping("/contacts/import-vcf")
    public String getImportPage(Model model) {
        return "normalUser/csf_manager"; // 
    }

    // ✅ Import VCF
    @PostMapping("/contacts/import-vcf")
    public String importVcf(@RequestParam("file") MultipartFile file,Principal principal,HttpSession session) {
        try {
            session.removeAttribute("message");
            String username = principal.getName();
           if (file == null || file.isEmpty() || file.getSize() == 0) {
                    session.setAttribute("message", new Message("Please upload a valid VCF file!", "alert-danger"));
                    return "normalUser/csf_manager";
                }
            else{
            vcfImportService.importFromVcf(file,username);
            session.setAttribute("message",new Message("Contacts Imported Successfully !!","alert-success"));
            return "normalUser/csf_manager";
            } 
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message",new Message("something Went Wrong !!","alert-danger"));
            return "normalUser/csf_manager";   
        }
    }

    
    @GetMapping("/contacts/export-vcf")
    public String exportVcf(HttpServletResponse response, Principal principal,HttpSession session) throws IOException {
       try {
        session.removeAttribute("message");
         User user= userRepository.getUserByUserName(principal.getName());
        // 1. Get all contacts
        List<Contact> contacts = contactRepository.findContactsByUser(user);
        System.out.println(contacts);
        if(contacts.isEmpty()){
            session.setAttribute("message",new Message("No Contacts available !!", "alert-danger"));
            System.out.println("nO CONTACTS");
            return "normalUser/csf_manager";
        }
        else{
        // 2. Convert to VCARD list
        List<VCard> vCards = new ArrayList<>();
        for (Contact c : contacts) {
            VCard vCard = new VCard();
            
            // Name
            if (c.getName() != null && !c.getName().trim().isEmpty()) {
                vCard.setFormattedName(c.getName());
            }
            // Nick Name 
            if (c.getNickName() != null && !c.getNickName().trim().isEmpty()) {
                vCard.setNickname(c.getNickName());
                
            }
             //  Discription
            if (c.getDescription() != null && !c.getDescription().trim().isEmpty()) {
                vCard.addNote(c.getDescription());
                
            }

            // Email
            if (c.getEmail() != null && !c.getEmail().trim().isEmpty()) {
                vCard.addEmail(c.getEmail());
            }

            // Phone
            if (c.getPhoneNumber() != null) {
                String phoneStr =(c.getPhoneNumber());
                if (!phoneStr.trim().isEmpty()) {
                    Telephone tel = new Telephone(phoneStr);
                    tel.getTypes().add(TelephoneType.CELL); // optional: mark as mobile
                    vCard.addTelephoneNumber(tel);
                }
            }

            vCards.add(vCard);
            
        }


        // 3. Response headers
        response.setContentType("text/vcard");
        response.setHeader("Content-Disposition", "attachment; filename=contacts.vcf");

        // 4. Write to output
        Ezvcard.write(vCards).go(response.getWriter());
        session.setAttribute("message",new Message("Contacts Exported Successfully !!","alert-success"));
        return "normalUser/csf_manager";
    }
       } catch (Exception e) {
        e.printStackTrace();
        session.setAttribute("message",new Message("something Went Wrong !!","alert-danger"));
        return "normalUser/csf_manager";
       }
    }

}
