package com.smart.smartContactManager1.controller;


import com.smart.smartContactManager1.dao.UserRepository;
import com.smart.smartContactManager1.entity.User;
import com.smart.smartContactManager1.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
public class ForgotController {


    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    Random random = new Random(1000);



    //email id from open handler

    @RequestMapping("/forgot")
    public String openEmailForm(){
        return "forgot_email_form";
    }


    @PostMapping("/send-otp")
    public String sendOtp(@RequestParam("email") String email, HttpSession session){
        System.out.println("EMAIL" +email);


        //generating otp of 4 digit

         int otp = random.nextInt(999999);
        System.out.println("OTP" +otp);

        //write code for send otp to email

        String subject ="OTP From SCM";
        String message =""
                         +"<div style='border:1px solid #e2e2e2; padding:20px'>"
                +"<h1>"
                +""
                 +"OTP is"
                +"<b>"+otp
                +"</n>"
                +"</h1>"
                +"</div>";
        String to= email;



         boolean flag  = this.emailService.sendEmail(subject,message,to);


         if (flag){

             session.setAttribute("myotp",otp);
             session.setAttribute("email",email);
             return "verify_otp";

         }else {

             session.setAttribute("message","Check your email id !!");

             return "forgot_email_form";
         }

    }


    //verify OTP


    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp") int otp,HttpSession session){


        int myOtp = (int)session.getAttribute("myotp");
        String email = (String) session.getAttribute("email");
        if (myOtp==otp){

            //password change form

          User userName = this.userRepository.getUserByUserName(email);

          if(userName==null){
              //send error message

              session.setAttribute("message","User does not exist with this email !!");
              return "forgot_email_form";
          }else{
              //send change password form
          }


            return "password_change_form";
        }else {
            session.setAttribute("message","You have entered wrong OTP");
            return "verify_otp";
        }

    }


    //change password


    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session){


        String email = (String)session.getAttribute("email");
          User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
        this.userRepository.save(user);

        return "redirect:/signin?change=password change successfully..";

    }



}
