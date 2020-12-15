package com.lambdaschool.usermodel.controllers;

import com.lambdaschool.usermodel.models.UserMinimum;
import com.lambdaschool.usermodel.models.UserRoles;
import com.lambdaschool.usermodel.services.RoleService;
import com.lambdaschool.usermodel.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.lambdaschool.usermodel.models.User;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class CreateNewUser {
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @PostMapping(value = "/createnewuser",
            consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<?> addSelf(
            HttpServletRequest httpServletRequest,
            @Valid
            @RequestBody
                    UserMinimum newminuser)
            throws
            URISyntaxException
    {
        // Create the user
        User newuser = new User();
        newuser.setUsername(newminuser.getUsername());
        newuser.setPassword(newminuser.getPassword());
        newuser.setPrimaryemail(newminuser.getPrimaryemail());

        // add the default role of user
        Set<UserRoles> newRoles = new HashSet<>();
        newRoles.add(new UserRoles(newuser,
                roleService.findByName("user")));
        newuser.setRoles(newRoles);
        newuser = userService.save(newuser);

        // return the access token
// To get the access token, surf to the endpoint /login (which is always on the server where this is running)
// just as if a client had done this.
        RestTemplate restTemplate = new RestTemplate();
        String requestURI = "http://localhost" + ":" + httpServletRequest.getLocalPort() + "/login";
        List<MediaType> acceptableMediaTypes = new ArrayList<>();
        acceptableMediaTypes.add(MediaType.APPLICATION_JSON);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(acceptableMediaTypes);
        headers.setBasicAuth(System.getenv("OAUTHCLIENTID"),
                System.getenv("OAUTHCLIENTSECRET"));
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type",
                "password");
        map.add("scope",
                "read write trust");
        map.add("username",
                newminuser.getUsername());
        map.add("password",
                newminuser.getPassword());
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map,
                headers);
        String theToken = restTemplate.postForObject(requestURI,
                request,
                String.class);

        return new ResponseEntity<>(theToken, HttpStatus.OK);
    }
}
