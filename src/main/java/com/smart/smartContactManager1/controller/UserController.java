package com.smart.smartContactManager1.controller;


import com.smart.smartContactManager1.dao.ContactRepository;
import com.smart.smartContactManager1.dao.UserRepository;
import com.smart.smartContactManager1.entity.Contact;
import com.smart.smartContactManager1.entity.User;
import com.smart.smartContactManager1.helper.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;


    //method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {


        String userName = principal.getName();
        System.out.println("USERNAME " + userName);

        //get the user using username

        User user = userRepository.getUserByUserName(userName);

        System.out.println("USER " + user);
        model.addAttribute("user", user);


    }


    //dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        String userName = principal.getName();
        System.out.println("USERNAME " + userName);

        //get the user using username

        User user = userRepository.getUserByUserName(userName);

        System.out.println("USER " + user);
        model.addAttribute("user", user);

        return "normal/user_dashboard";
    }


    //open and form handler


    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {

        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }


    //processing add contact form

    @PostMapping("/process-contact")
    public String processContact(@ModelAttribute Contact contact,
                                 @RequestParam("profileImage") MultipartFile file,
                                 Principal principal,
                                 HttpSession session) {


        try {


            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);


            //processing and uploading file..

            if (file.isEmpty()) {

                //if the file is empty then try our message

                System.out.println("File is Empty");
                contact.setImage("contactus.png");


            } else {
                // file the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());


                File saveFile = new ClassPathResource("static/css/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("image is uploaded");
            }


            user.getContacts().add(contact);

            contact.setUser(user);

            this.userRepository.save(user);

            System.out.println("DATA" + contact);

            System.out.println("Added to database");

            //message success....

            session.setAttribute("message", new Message("Your content is added !! Add more..", "success"));
        } catch (Exception e) {

            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();

            //message error

            session.setAttribute("message", new Message("Something went wrong try again..", "danger"));

        }
        return "normal/add_contact_form";

    }

    // Show contact handler


    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model m, Principal principal) {
        m.addAttribute("title", "Show User Contacts");

        //Contact List bhejni hai
        String userName = principal.getName();

        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);

        m.addAttribute("contacts", contacts);
        m.addAttribute("currentPage", page);
        m.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/show_contacts";
    }


    //showing particular contact details...

    @RequestMapping("/{cId}/contact")
    public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

        System.out.println("CID" + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);

        Contact contact = contactOptional.get();

        //

        String userName = principal.getName();

        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title",contact.getName());
        }
            return "normal/contact_detail";



    }


    //delete contact handler

    @GetMapping("/delete/{cid}")
    public String deleteContact(@PathVariable("cid") Integer cId,Model model,Principal principal,HttpSession session){

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();


        //Check...

        String name = principal.getName();
        User user = this.userRepository.getUserByUserName(name);


        if (user.getId()==contact.getUser().getId()){

//            contact.setUser(null);

//            this.contactRepository.delete(contact);

            user.getContacts().remove(contact);
            this.userRepository.save(user);

            session.setAttribute("message",new Message("Contact deleted successfully","success"));

        }
        return "redirect:/user/show-contacts/0";


    }


    //Open update form handler


    @PostMapping("/update-contact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model m){

        m.addAttribute("title","Update Contacts");

         Contact contact = this.contactRepository.findById(cId).get();

        m.addAttribute("contact",contact);
        return "normal/update_form";
    }


    //update contact handler


    @RequestMapping(value = "/process_update",method = RequestMethod.POST)
    public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal principal){

       try{

           //old contact detailes

            Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();

           //image

           if (!file.isEmpty()){

               //file work
               //rewrite

               //delete old photo

               File deleteFile = new ClassPathResource("static/css/img").getFile();

               File file1 = new File(deleteFile,oldContactDetail.getImage());

               file1.delete();


               //update new photo

               File saveFile = new ClassPathResource("static/css/img").getFile();

               Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

               Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

               contact.setImage(file.getOriginalFilename());


           }else{
              contact.setImage(oldContactDetail.getImage());
           }

           User user = this.userRepository.getUserByUserName(principal.getName());
           contact.setUser(user);
           this.contactRepository.save(contact);

           session.setAttribute("message",new Message("Your contact is updated..", "success"));

       }catch (Exception e){
           e.printStackTrace();
       }

        System.out.println("CONTACT NAME " +contact.getName());
        System.out.println("CONTACT ID " +contact.getcId());

        return "redirect:/user/"+contact.getcId()+"/contact";
    }

    //Your profile handler


    @GetMapping("/profile")
    public String yourProfile(Model model){
        model.addAttribute("title","Profile Page");
        return "normal/profile";
    }


    //Open settings handler


    @GetMapping("settings")
    public  String openSettings(){
        return "normal/settings";
    }


    //change password handler


    @PostMapping("/change-password")
    public  String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){

        System.out.println("OLD PASSWORD" +oldPassword);
        System.out.println("NEW PASSWORD" +newPassword);

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())){
            //change the password

            user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(user);
            session.setAttribute("message",new Message("Your password has successfully changed..", "success"));

        }else{
            //error
            session.setAttribute("message",new Message("Please correct enter old password..", "danger"));
            return "redirect:/user/settings";
        }
            return "redirect:/user/index";

        }

    }


