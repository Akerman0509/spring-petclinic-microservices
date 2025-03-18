/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.vets.model.Specialty;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Maciej Szarlinski
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(VetResource.class)
@ActiveProfiles("test")
class VetResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VetRepository vetRepository;

    @Test
    void shouldGetAListOfVets() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("James");
        vet.setLastName("Carter");

        Specialty specialty = new Specialty();
        specialty.setId(1);
        specialty.setName("radiology");
        vet.addSpecialty(specialty);

        given(vetRepository.findAll()).willReturn(Arrays.asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("James"))
            .andExpect(jsonPath("$[0].lastName").value("Carter"))
            .andExpect(jsonPath("$[0].specialties[0].id").value(1))
            .andExpect(jsonPath("$[0].specialties[0].name").value("radiology"));
    }

    @Test
    void shouldGetEmptyListOfVets() throws Exception {
        given(vetRepository.findAll()).willReturn(Arrays.asList());

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldGetVetsWithMultipleSpecialties() throws Exception {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("Linda");
        vet.setLastName("Douglas");

        Specialty surgery = new Specialty();
        surgery.setId(2);
        surgery.setName("surgery");
        vet.addSpecialty(surgery);

        Specialty dentistry = new Specialty();
        dentistry.setId(3);
        dentistry.setName("dentistry");
        vet.addSpecialty(dentistry);

        given(vetRepository.findAll()).willReturn(Arrays.asList(vet));

        mvc.perform(get("/vets").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].firstName").value("Linda"))
            .andExpect(jsonPath("$[0].lastName").value("Douglas"))
            .andExpect(jsonPath("$[0].specialties").isArray())
            .andExpect(jsonPath("$[0].specialties.length()").value(2))
            .andExpect(jsonPath("$[0].specialties[0].name").value("surgery"))
            .andExpect(jsonPath("$[0].specialties[1].name").value("dentistry"));
    }
}
