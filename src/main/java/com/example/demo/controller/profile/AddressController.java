package com.example.demo.controller.profile;


import com.example.demo.config.AuthProviderFinder;
import com.example.demo.models.Address;
import com.example.demo.models.Users;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ProfileService;
import com.example.demo.service.profile.addressService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile/address")
public class AddressController {

    private final addressService addressService;
    private final ProfileService profileService;
    private final UserRepository userRepository;
    private final AuthProviderFinder authProviderFinder;

    public AddressController(ProfileService profileService, UserRepository userRepository, addressService addressService, AuthProviderFinder authProviderFinder) {
        this.profileService = profileService;
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.authProviderFinder = authProviderFinder;
    }

//    @GetMapping("/add")
//    public String add(ModelMap model) {
//        model.addAttribute("address", new Address());
//
//
//        return "main/profile/address/add_address";
//    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") String id, ModelMap model) {
        addressService.deleteAddress(id);

        return "redirect:/profile/address";
    }

    @PostMapping("/delete/checkout")
    public String deleteCheckout(@RequestParam("id") String id, ModelMap model) {
        addressService.deleteAddress(id);

        return "redirect:/checkout";
    }

    @PostMapping("/add")
    public String save(
            @ModelAttribute Address address,
            ModelMap model, Authentication auth) {


        System.out.println("THe incoming data is " + address);

        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users existingUser = userRepository.findByUsername(currentUser.getUsername());

        System.out.println("the exisiting user is " + existingUser);
        addressService.saveAddress(existingUser, address);


        return "redirect:/profile/address";

    }

    @PostMapping("/add/checkout")
    public String saveCheckout(
            @ModelAttribute Address address,
            ModelMap model, Authentication auth) {


        System.out.println("THe incoming data is " + address);

        Users currentUser = authProviderFinder.getUserAuth(auth);
        Users existingUser = userRepository.findByUsername(currentUser.getUsername());

        System.out.println("the exisiting user is " + existingUser);
        addressService.saveAddress(existingUser, address);


        return "redirect:/checkout";

    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute Address address){

        System.out.println("INSIDE EDIT");
        System.out.println("THe incoming data is EDITITITITI" + address);

        addressService.updateAddress(address);


        return "redirect:/profile/address";
    }

    @PostMapping("/edit/checkout")
    public String editCheckout(@ModelAttribute Address address){

        System.out.println("INSIDE EDIT");
        System.out.println("THe incoming data is EDITITITITI" + address);

        addressService.updateAddress(address);


        return "redirect:/checkout";
    }
}


