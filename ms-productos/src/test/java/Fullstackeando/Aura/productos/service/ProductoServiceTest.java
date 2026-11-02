package Fullstackeando.Aura.productos.service;

import Fullstackeando.Aura.productos.model.Producto;
import Fullstackeando.Aura.productos.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceTest {

    private ProductoRepository productoRepository;
    private ProductoService productoService;

    @BeforeEach
    void preparar() {
        productoRepository = mock(ProductoRepository.class);
        productoService = new ProductoService(productoRepository);
    }

    @Test
    void listarDevuelveDatosDelCatalogoYAlertaDeStockBajo() {
        Producto producto = new Producto();
        producto.setCodigo("MC001");
        producto.setNombre("Cemento");
        producto.setCategoria("Construcción");
        producto.setSubcategoria("Cementos");
        producto.setMarca("Polpaico");
        producto.setUnidad("Saco");
        producto.setPrecio(5000);
        producto.setStock(2);
        producto.setStockMinimo(3);
        when(productoRepository.findByActivoTrue()).thenReturn(List.of(producto));

        var resultado = productoService.listarProductos();

        assertEquals(1, resultado.size());
        var respuesta = resultado.getFirst();
        assertAll(
                () -> assertEquals("MC001", respuesta.getCodigo()),
                () -> assertEquals("Cemento", respuesta.getNombre()),
                () -> assertEquals("Construcción", respuesta.getCategoria()),
                () -> assertEquals("Cementos", respuesta.getSubcategoria()),
                () -> assertEquals("Polpaico", respuesta.getMarca()),
                () -> assertEquals("Saco", respuesta.getUnidad()),
                () -> assertEquals(5000, respuesta.getPrecio()),
                () -> assertEquals(2, respuesta.getStock()),
                () -> assertEquals(3, respuesta.getStockMinimo()),
                () -> assertTrue(respuesta.isStockBajo())
        );
    }

    @Test
    void obtenerNoMarcaStockBajoCuandoEsIgualAlMinimo() {
        Producto producto = new Producto();
        producto.setCodigo("MC001");
        producto.setStock(3);
        producto.setStockMinimo(3);
        when(productoRepository.findByCodigoAndActivoTrue("MC001"))
                .thenReturn(Optional.of(producto));

        var resultado = productoService.obtenerProducto("MC001");

        assertEquals("MC001", resultado.getCodigo());
        assertFalse(resultado.isStockBajo());
    }

    @Test
    void obtenerRechazaProductoQueNoEstaEnElCatalogoActivo() {
        when(productoRepository.findByCodigoAndActivoTrue("MC001"))
                .thenReturn(Optional.empty());

        var error = assertThrows(NoSuchElementException.class,
                () -> productoService.obtenerProducto("MC001"));

        assertEquals("Producto no encontrado", error.getMessage());
    }
}
