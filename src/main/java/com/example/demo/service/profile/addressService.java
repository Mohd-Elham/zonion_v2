package com.example.demo.service.profile;

import com.example.demo.models.Address;
import com.example.demo.models.Users;
import com.example.demo.repository.MyAddressRepo;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class addressService {
    MyAddressRepo myAddressRepo;

    public addressService(MyAddressRepo myAddressRepo) {
        this.myAddressRepo = myAddressRepo;
    }


    public void saveAddress(Users existingUser, Address address) {
        Address newAddress = new Address();
        String randUUID = UUID.randomUUID().toString();
        newAddress.setId(randUUID);
        newAddress.setName(address.getName());
        newAddress.setStreet(address.getStreet());
        newAddress.setCity(address.getCity());
        newAddress.setState(address.getState());
        newAddress.setZip(address.getZip());
        newAddress.setPlace(address.getPlace());
        newAddress.setUserId(existingUser.getId());


        myAddressRepo.save(newAddress);
    }

    public void deleteAddress(String id) {
        myAddressRepo.deleteById(id);
    }

    public void updateAddress(Address address) {
        myAddressRepo.save(address);
    }

    public List<Address> getAddressesByUserId(String id) {
        return myAddressRepo.findAddressesByUserId(id);
    }

    public List<Address> findUserById(String id) {
        return  myAddressRepo.findAddressesByUserId(id);
    }

    public Address getAddressById(String shippingAddress) {
        return myAddressRepo.findById(shippingAddress).get();
    }
    public List<Address> getAddressesByIds(List<String> ids) {
        return myAddressRepo.findAllById(ids);
    }
}
