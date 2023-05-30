package com.smart.smartContactManager1.controller;


import com.smart.smartContactManager1.dao.UserRepository;
import com.smart.smartContactManager1.entity.User;
import com.smart.smartContactManager1.helper.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class HomeController {



    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @RequestMapping("/")
  public String home(Model model){

        model.addAttribute("title","Home - Smart Contact Manager");

      return "home";
  }



    @RequestMapping("/about")
    public String about(Model model){

        model.addAttribute("title","about - Smart Contact Manager");

        return "about";
    }


    @RequestMapping("/signup")
    public String signup(Model model){

        model.addAttribute("title","Register - Smart Contact Manager");
        model.addAttribute("user",new User());
        return "signup";
    }



    //handler for register user



    @RequestMapping(value = "/do_register",method = RequestMethod.POST)
  public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result1, @RequestParam(value = "agreement" ,defaultValue = "false") boolean agreement, Model model, HttpSession session){



       try {

           if(!agreement){
               System.out.println("You have not agreed the terms and conditions");
               throw new Exception("You have not agreed terms and conditions");
           }


           if(result1.hasErrors()){
               System.out.println("Error" +result1.toString());
               model.addAttribute("user",user);
               return "signup";
           }

           user.setRole("ROLE_USER");
           user.setEnabled(true);
           user.setImageUrl("default.png");
           user.setPassword(passwordEncoder.encode(user.getPassword()));

           System.out.println("Agreement" + agreement);

           System.out.println("USER" +user);
           User result = this.userRepository.save(user);
           model.addAttribute("user" ,new User());


           session.setAttribute("message", new Message("Successfully registered !!","alert-success"));

           return "signup";

       }catch (Exception e){
           e.printStackTrace();
           model.addAttribute("user",user);
           session.setAttribute("message", new Message("Something went wrong"+e.getMessage(),"alert-error"));

           return "signup";

       }



  }



  //handler for custom login


    @GetMapping("/signin")
    public String customLogin(Model model){
        model.addAttribute("title","Login page");
        return "login";
    }

}
