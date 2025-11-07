package com.springboot.smartContactManager.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.springboot.smartContactManager.Repository.ContactRepository;
import com.springboot.smartContactManager.Repository.UserRepository;
import com.springboot.smartContactManager.entities.Contact;
import com.springboot.smartContactManager.entities.User;

import ezvcard.Ezvcard;
import ezvcard.VCard;

@Service
public class CsfImportServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

   public void importFromVcf(MultipartFile file, String username) throws Exception {

    User user = userRepository.getUserByUserName(username);
    List<VCard> vCards = Ezvcard.parse(file.getInputStream()).all();

    int importedCount = 0;
    int skippedCount = 0;
    int duplicateCount = 0;

    for (VCard vCard : vCards) {
        try {
            // Skip if no phone numbers
            if (vCard.getTelephoneNumbers().isEmpty()) {
                skippedCount++;
                continue;
            }

            for (var tel : vCard.getTelephoneNumbers()) {
                String number = tel.getText();
                if (number == null || number.trim().isEmpty()) {
                    skippedCount++;
                    continue;
                }
                number = number.trim();

                // Skip duplicate phone numbers
                if (contactRepository.existsByPhoneNumberAndUser(number,user)) {
                    duplicateCount++;
                    System.out.println("Duplicate skipped: " + number);
                    continue;
                }

                Contact contact = new Contact();

                // Name
                String name = (vCard.getFormattedName() != null) 
                                ? vCard.getFormattedName().getValue().trim() 
                                : "Unknown";
                contact.setName(name);

                // Nickname
                contact.setNickName(name);

                // Email
                if (!vCard.getEmails().isEmpty()) {
                    contact.setEmail(vCard.getEmails().get(0).getValue().trim());
                } else {
                    contact.setEmail("null");
                }

                // Phone
                contact.setPhoneNumber(number);

                // Image placeholder
                contact.setImageUrl("contact.png");

                // Link user
                contact.setUser(user);

                // Save contact
                contactRepository.save(contact);
                importedCount++;

                System.out.println("Imported: " + name + " - " + number);
            }

        } catch (Exception e) {
            skippedCount++;
            System.err.println("Failed to import a VCard: " + e.getMessage());
        }
    }

    System.out.println("Import Summary -> Imported: " + importedCount 
                        + ", Duplicates: " + duplicateCount 
                        + ", Skipped: " + skippedCount);
}
}
