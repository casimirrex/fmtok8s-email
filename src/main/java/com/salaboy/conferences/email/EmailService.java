package com.salaboy.conferences.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salaboy.conferences.email.model.Proposal;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
@RestController
@EnableZeebeClient
@Slf4j
public class EmailService {

    public static void main(String[] args) {
        SpringApplication.run(EmailService.class, args);
    }

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${version:0.0.0}")
    private String version;

    @GetMapping("/info")
    public String infoWithVersion() {
        return "{ \"name\" : \"Email Service\", \"version\" : \"" + version + "\", \"source\": \"https://github.com/salaboy/fmtok8s-email/releases/tag/v"+version+"\" }";
    }

    @PostMapping("/")
    public void sendEmail(@RequestBody Map<String, String> email) {
        String toEmail = email.get("toEmail");
        String emailTitle = email.get("title");
        String emailContent = email.get("content");
        printEmail(toEmail, emailTitle, emailContent);
    }


    @PostMapping("/notification")
    public void sendEmailNotification(@RequestBody Proposal proposal) {
        String emailBody = "Dear " + proposal.getAuthor() + ", \n";
        String emailTitle = "Conference Committee Communication";
        emailBody += "\t\t We are";
        if (proposal.isApproved()) {
            emailBody += " happy ";
        } else {
            emailBody += " sorry ";
        }
        emailBody += "to inform you that: \n";
        emailBody += "\t\t\t `" + proposal.getTitle() + "` -> `" + proposal.getDescription() + "`, \n";
        emailBody += "\t\t was";
        if (proposal.isApproved()) {
            emailBody += " approved ";
        } else {
            emailBody += " rejected ";
        }
        emailBody += "for this conference.";
        printEmail(proposal.getEmail(),emailTitle, emailBody );
    }

    private void printEmail(String to, String title, String body){
        log.info("+-------------------------------------------------------------------+");
        log.info("\t Email Sent to: " + to);
        log.info("\t Email Title: " + title);
        log.info("\t Email Body: " + body);
        log.info("+-------------------------------------------------------------------+\n\n");
    }

    @ZeebeWorker(name = "email-worker", type = "email")
    public void sendEmailNotification(final JobClient client, final ActivatedJob job) {
        Proposal proposal = objectMapper.convertValue(job.getVariablesAsMap().get("proposal"), Proposal.class);
        sendEmailNotification(proposal);
        client.newCompleteCommand(job.getKey()).send();
    }


}
