package org.springframework.samples.petclinic.customers.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
class OwnerResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    OwnerRepository ownerRepository;

    @MockBean
    OwnerEntityMapper ownerEntityMapper;

    @Test
    void shouldGetAListOfOwners() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        given(ownerRepository.findAll()).willReturn(Arrays.asList(owner));

        mvc.perform(get("/owners").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("George"))
            .andExpect(jsonPath("$[0].lastName").value("Franklin"))
            .andExpect(jsonPath("$[0].address").value("110 W. Liberty St."))
            .andExpect(jsonPath("$[0].city").value("Madison"))
            .andExpect(jsonPath("$[0].telephone").value("6085551023"));
    }

    @Test
    void shouldGetASingleOwner() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.lastName").value("Franklin"))
            .andExpect(jsonPath("$.address").value("110 W. Liberty St."))
            .andExpect(jsonPath("$.city").value("Madison"))
            .andExpect(jsonPath("$.telephone").value("6085551023"));
    }

    @Test
    void shouldCreateOwner() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        OwnerRequest ownerRequest = new OwnerRequest("George", "Franklin", "110 W. Liberty St.", "Madison", "6085551023");

        given(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).willReturn(owner);
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"George\",\"lastName\":\"Franklin\",\"address\":\"110 W. Liberty St.\",\"city\":\"Madison\",\"telephone\":\"6085551023\"}"))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"))
            .andExpect(jsonPath("$.lastName").value("Franklin"))
            .andExpect(jsonPath("$.address").value("110 W. Liberty St."))
            .andExpect(jsonPath("$.city").value("Madison"))
            .andExpect(jsonPath("$.telephone").value("6085551023"));

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldUpdateOwner() throws Exception {
        Owner owner = new Owner();
        owner.setId(1);
        owner.setFirstName("George");
        owner.setLastName("Franklin");
        owner.setAddress("110 W. Liberty St.");
        owner.setCity("Madison");
        owner.setTelephone("6085551023");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));
        given(ownerEntityMapper.map(any(Owner.class), any(OwnerRequest.class))).willReturn(owner);
        given(ownerRepository.save(any(Owner.class))).willReturn(owner);

        mvc.perform(put("/owners/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"George\",\"lastName\":\"Franklin\",\"address\":\"110 W. Liberty St.\",\"city\":\"Madison\",\"telephone\":\"6085551023\"}"))
            .andExpect(status().isNoContent());

        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void shouldReturn404WhenOwnerNotFound() throws Exception {
        given(ownerRepository.findById(1)).willReturn(Optional.empty());

        mvc.perform(get("/owners/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateOwnerCreation() throws Exception {
        mvc.perform(post("/owners")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"\",\"address\":\"\",\"city\":\"\",\"telephone\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
} 