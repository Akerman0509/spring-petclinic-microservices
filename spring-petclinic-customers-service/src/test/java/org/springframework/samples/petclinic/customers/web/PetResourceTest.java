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
import org.springframework.samples.petclinic.customers.model.PetRepository;
import org.springframework.samples.petclinic.customers.model.PetType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(PetResource.class)
@ActiveProfiles("test")
class PetResourceTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PetRepository petRepository;

    @MockBean
    private OwnerRepository ownerRepository;

    private Pet testPet;
    private Owner testOwner;
    private PetType testPetType;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1);
        testOwner.setFirstName("Test");
        testOwner.setLastName("User");

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
    void shouldGetPetDetailsSuccessfully() throws Exception {
        given(petRepository.findById(1)).willReturn(Optional.of(testPet));

        mvc.perform(get("/owners/1/pets/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Buddy"))
            .andExpect(jsonPath("$.type.id").value(1))
            .andExpect(jsonPath("$.birthDate").value("2020-01-01"));
    }

    @Test
    void shouldCreateNewPetSuccessfully() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(testPetType));
        
        String newPetJson = """
        {
            "name": "Max",
            "birthDate": "2021-03-15",
            "typeId": 1
        }
        """;

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPetJson))
            .andExpect(status().isCreated());
    }

    @Test
    void shouldUpdatePetSuccessfully() throws Exception {
        given(petRepository.findById(1)).willReturn(Optional.of(testPet));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(testPetType));

        String updatePetJson = """
        {
            "id": 1,
            "name": "BuddyUpdated",
            "birthDate": "2020-01-01",
            "typeId": 1
        }
        """;

        mvc.perform(put("/owners/1/pets/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePetJson))
            .andExpect(status().isNoContent());
    }

    @Test
    void shouldGetAllPetTypesSuccessfully() throws Exception {
        List<PetType> petTypes = Arrays.asList(
            createPetType(1, "Dog"),
            createPetType(2, "Cat"),
            createPetType(3, "Bird")
        );
        
        given(petRepository.findPetTypes()).willReturn(petTypes);
        
        mvc.perform(get("/petTypes").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Dog"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Cat"))
            .andExpect(jsonPath("$[2].id").value(3))
            .andExpect(jsonPath("$[2].name").value("Bird"));
    }

    @Test
    void shouldHandleInvalidPetTypeId() throws Exception {
        given(petRepository.findPetTypeById(999)).willReturn(Optional.empty());
        
        String newPetJson = """
        {
            "name": "InvalidPet",
            "birthDate": "2021-03-15",
            "typeId": 999
        }
        """;

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPetJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleInvalidBirthDate() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(testPetType));
        
        String newPetJson = """
        {
            "name": "InvalidDatePet",
            "birthDate": "invalid-date",
            "typeId": 1
        }
        """;

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPetJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleEmptyPetName() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.of(testOwner));
        given(petRepository.findPetTypeById(1)).willReturn(Optional.of(testPetType));
        
        String newPetJson = """
        {
            "name": "",
            "birthDate": "2021-03-15",
            "typeId": 1
        }
        """;

        mvc.perform(post("/owners/1/pets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(newPetJson))
            .andExpect(status().isBadRequest());
    }

    private PetType createPetType(int id, String name) {
        PetType petType = new PetType();
        petType.setId(id);
        petType.setName(name);
        return petType;
    }
}