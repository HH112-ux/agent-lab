package com.caspian.agent.chataiservice.pojo;

import com.caspian.agent.chataiservice.enums.Gender;
import dev.langchain4j.model.output.structured.Description;

import java.time.LocalDate;

public class Person {


    @Description("first name of a person")
    String firstName;

    @Description("last name of a person")
    String lastName;

    @Description("生日")
    LocalDate birthDate;

    @Description("性别")
    Gender gender;

    @Description("住址")
    String address;

}
