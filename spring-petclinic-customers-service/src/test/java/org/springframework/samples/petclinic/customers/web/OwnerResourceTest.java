package org.springframework.samples.petclinic.customers.web;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private OwnerRepository ownerRepository;

    private Owner testOwner;
    private Pet testPet;
    private PetType testPetType;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1);
        testOwner.setFirstName("Test");
        testOwner.setLastName("User");
        testOwner.setAddress("123 Test St");
        testOwner.setCity("Test City");
        testOwner.setTelephone("1234567890");

        testPetType = new PetType();
        testPetType.setId(1);
        testPetType.setName("Dog");

        testPet = new Pet();
        testPet.setId(1);
        testPet.setName("Buddy");
        testPet.setType(testPetType);
        testPet.setBirthDate(java.sql.Date.valueOf("2020-01-01"));
        testOwner.addPet(testPet);
    }

    @Test
    void shouldGetOwnerDetailsSuccessfully() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("Test"))
            .andExpect(jsonPath("$.lastName").value("User"))
            .andExpect(jsonPath("$.address").value("123 Test St"))
            .andExpect(jsonPath("$.city").value("Test City"))
            .andExpect(jsonPath("$.telephone").value("1234567890"))
            .andExpect(jsonPath("$.pets[0].id").value(1))
            .andExpect(jsonPath("$.pets[0].name").value("Buddy"));
    }

    @Test
    void shouldCreateNewOwnerSuccessfully() throws Exception {
        String newOwnerJson = """
        {
            "firstName": "John",
            "lastName": "Doe",
            "address": "456 New St",
            "city": "New City",
            "telephone": "9876543210"
        }
        """;

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newOwnerJson))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdateOwnerSuccessfully() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));

        String updateOwnerJson = """
        {
            "id": 1,
            "firstName": "TestUpdated",
            "lastName": "UserUpdated",
            "address": "789 Update St",
            "city": "Update City",
            "telephone": "5555555555"
        }
        """;

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateOwnerJson))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetAllOwnersSuccessfully() throws Exception {
        Owner owner2 = new Owner();
        owner2.setId(2);
        owner2.setFirstName("Jane");
        owner2.setLastName("Smith");

        List<Owner> owners = Arrays.asList(testOwner, owner2);
        given(ownerRepository.findAll()).willReturn(owners);

        mvc.perform(get("/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Test"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void shouldHandleInvalidOwnerId() throws Exception {
        given(ownerRepository.findById(999)).willReturn(Optional.empty());

        mvc.perform(get("/owners/999").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldHandleEmptyOwnerName() throws Exception {
        String newOwnerJson = """
        {
            "firstName": "",
            "lastName": "",
            "address": "123 Test St",
            "city": "Test City",
            "telephone": "1234567890"
        }
        """;

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newOwnerJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidTelephoneNumber() throws Exception {
        String newOwnerJson = """
        {
            "firstName": "John",
            "lastName": "Doe",
            "address": "123 Test St",
            "city": "Test City",
            "telephone": "invalid-number"
        }
        """;

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newOwnerJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleOwnerWithMultiplePets() throws Exception {
        Pet secondPet = new Pet();
        secondPet.setId(2);
        secondPet.setName("Max");
        secondPet.setType(testPetType);
        secondPet.setBirthDate(java.sql.Date.valueOf("2021-01-01"));
        testOwner.addPet(secondPet);

        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pets.length()").value(2))
            .andExpect(jsonPath("$.pets[0].name").value("Buddy"))
            .andExpect(jsonPath("$.pets[1].name").value("Max"));
    }

    @Test
    void shouldHandleOwnerSearch() throws Exception {
        Owner owner2 = new Owner();
        owner2.setId(2);
        owner2.setFirstName("John");
        owner2.setLastName("Smith");

        List<Owner> owners = Arrays.asList(testOwner, owner2);
        given(ownerRepository.findByLastNameContainingIgnoreCase("Smith")).willReturn(owners);

        mvc.perform(get("/owners/search?lastName=Smith").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].lastName").value("User"))
            .andExpect(jsonPath("$[1].lastName").value("Smith"));
    }
}