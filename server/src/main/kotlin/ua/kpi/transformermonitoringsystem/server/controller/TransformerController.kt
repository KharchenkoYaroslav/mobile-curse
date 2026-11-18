package ua.kpi.transformermonitoringsystem.server.controller

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ua.kpi.transformermonitoringsystem.core.dto.TransformerDto
import ua.kpi.transformermonitoringsystem.core.dto.TransformerStatsDto
import ua.kpi.transformermonitoringsystem.core.dto.TransformerStatusDto
import ua.kpi.transformermonitoringsystem.core.service.TransformerService

@RestController
@RequestMapping("/transformers")
class TransformerController(
    private val transformerService: TransformerService
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllTransformers(): List<TransformerDto> {
        return transformerService.getAllTransformers()
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun getTransformerById(@PathVariable("id") id: Int): TransformerDto {
        return transformerService.getTransformerById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun addTransformer(@Valid @RequestBody dto: TransformerDto): TransformerDto {
        return transformerService.addTransformer(dto)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateTransformer(
        @PathVariable("id") id: Int,
        @Valid @RequestBody dto: TransformerDto
    ): TransformerDto {
        return transformerService.updateTransformer(id, dto)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTransformer(@PathVariable("id") id: Int) {
        transformerService.deleteTransformer(id)
    }

    // татистика
    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    fun getStats(): TransformerStatsDto {
        return transformerService.getNetworkStats()
    }

    // Історія статусів
    @GetMapping("/{id}/statuses")
    @ResponseStatus(HttpStatus.OK)
    fun getStatuses(@PathVariable("id") id: Int): List<TransformerStatusDto> {
        return transformerService.getStatusesByTransformerId(id)
    }

    @PostMapping("/{id}/statuses")
    @ResponseStatus(HttpStatus.CREATED)
    fun addStatus(
        @PathVariable("id") id: Int,
        @Valid @RequestBody dto: TransformerStatusDto
    ): TransformerStatusDto {
        return transformerService.addStatus(id, dto)
    }

    @DeleteMapping("/statuses/{statusId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteStatus(@PathVariable("statusId") statusId: Int) {
        transformerService.deleteStatus(statusId)
    }
}
