package com.techwasti.restapi.springbootmongodb.restapi

import com.techwasti.restapi.springbootmongodb.restapi.data.Patient
import com.techwasti.restapi.springbootmongodb.restapi.data.PatientRepository
import com.techwasti.restapi.springbootmongodb.restapi.request.PatientRequest
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/patients")
class PatientController(
        private val patientsRepository: PatientRepository
) {

    @GetMapping
    fun getAllPatients(): ResponseEntity<List<Patient>> {
        val patients = patientsRepository.findAll()
        return ResponseEntity.ok(patients)
    }

    @GetMapping("/{id}")
    fun getOnePatient(@PathVariable("id") id: String): ResponseEntity<Patient> {
        val patient = patientsRepository.findOneById(ObjectId(id))
        return ResponseEntity.ok(patient)
    }

    @PostMapping
    fun createPatient(@RequestBody request: PatientRequest): ResponseEntity<Patient> {
        val patient = patientsRepository.save(Patient(
                name = request.name,
                description = request.description
        ))
        return ResponseEntity(patient, HttpStatus.CREATED)
    }

    @PutMapping("/{id}")
    fun updatePatient(@RequestBody request: PatientRequest, @PathVariable("id") id: String): ResponseEntity<Patient> {
        val patient = patientsRepository.findOneById(ObjectId(id))
        val updatedPatient = patientsRepository.save(Patient(
                id = patient.id,
                name = request.name,
                description = request.description,
                createdDate = patient.createdDate,
                modifiedDate = LocalDateTime.now()
        ))
        return ResponseEntity.ok(updatedPatient)
    }

    @DeleteMapping("/{id}")
    fun deletePatient(@PathVariable("id") id: String): ResponseEntity<Unit> {
        patientsRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}