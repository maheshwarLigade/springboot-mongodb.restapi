package com.techwasti.restapi.springbootmongodb.restapi

import com.techwasti.restapi.springbootmongodb.restapi.data.Patient
import com.techwasti.restapi.springbootmongodb.restapi.data.PatientRepository
import com.techwasti.restapi.springbootmongodb.restapi.request.PatientRequest
import org.bson.types.ObjectId
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PatientControllerIntTest @Autowired constructor(
        private val patientRepository: PatientRepository,
        private val restTemplate: TestRestTemplate
) {
    private val defaultPatientId = ObjectId.get()

    @LocalServerPort
    protected var port: Int = 0

    @BeforeEach
    fun setUp() {
        patientRepository.deleteAll()
    }


    private fun getRootUrl(): String? = "http://localhost:$port/patients"

    private fun saveOnePatient() = patientRepository.save(Patient(defaultPatientId, "Name", "Description"))

    private fun preparePatientRequest() = PatientRequest("Name", "Default description")

    @Test
    fun `should return all patients`() {
        saveOnePatient()

        val response = restTemplate.getForEntity(
                getRootUrl(),
                List::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(1, response.body?.size)
    }

    @Test
    fun `should return single patient by id`() {
        saveOnePatient()

        val response = restTemplate.getForEntity(
                getRootUrl() + "/$defaultPatientId",
                Patient::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertNotNull(response.body)
        assertEquals(defaultPatientId, response.body?.id)
    }

    // delete Patient
    @Test
    fun `should delete existing patient`() {
        saveOnePatient()

        val delete = restTemplate.exchange(
                getRootUrl() + "/$defaultPatientId",
                HttpMethod.DELETE,
                HttpEntity(null, HttpHeaders()),
                ResponseEntity::class.java
        )

        assertEquals(204, delete.statusCode.value())
        assertThrows(EmptyResultDataAccessException::class.java) { patientRepository.findOneById(defaultPatientId) }
    }
    // update operation
    @Test
    fun `should update existing patient`() {
        saveOnePatient()
        val patientRequest = preparePatientRequest()

        val updateResponse = restTemplate.exchange(
                getRootUrl() + "/$defaultPatientId",
                HttpMethod.PUT,
                HttpEntity(patientRequest, HttpHeaders()),
                Patient::class.java
        )
        val updatedTask = patientRepository.findOneById(defaultPatientId)

        assertEquals(200, updateResponse.statusCode.value())
        assertEquals(defaultPatientId, updatedTask.id)
        assertEquals(patientRequest.description, updatedTask.description)
        assertEquals(patientRequest.name, updatedTask.name)
    }

    @Test
    fun `should create new patient`() {
        val patientRequest = preparePatientRequest()

        val response = restTemplate.postForEntity(
                getRootUrl(),
                patientRequest,
                Patient::class.java
        )


        assertEquals(201, response.statusCode.value())
        assertNotNull(response.body)
        assertNotNull(response.body?.id)
        assertEquals(patientRequest.description, response.body?.description)
        assertEquals(patientRequest.name, response.body?.name)
    }

}