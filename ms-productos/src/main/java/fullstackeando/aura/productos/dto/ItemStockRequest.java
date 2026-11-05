package fullstackeando.aura.productos.dto;

import jakarta.validation.constraints.*;

public record ItemStockRequest(
    @NotBlank(message = "Uno de los productos ya no existe.")
    @Size(max = 10, message = "Uno de los productos ya no existe.")
    String codigo,
    @NotNull(message = "La cantidad debe ser mayor a 0")
    @Positive(message = "La cantidad debe ser mayor a 0")
    Integer cantidad) {}
