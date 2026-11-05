package fullstackeando.aura.productos.service;

import fullstackeando.aura.productos.dto.*;
import fullstackeando.aura.productos.model.Producto;
import fullstackeando.aura.productos.repository.ProductoRepository;
import fullstackeando.aura.productos.exception.ReglaNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class ProductoGestionService {
    private final ProductoRepository productos;
    public ProductoGestionService(ProductoRepository productos) { this.productos = productos; }

    public ProductoResponse crearProducto(ProductoRequest datos) {
        String codigo = datos.codigo().trim().toUpperCase(Locale.ROOT);
        if (productos.existsById(codigo)) throw new ReglaNegocioException("Ya existe un producto con ese código");
        Producto producto = new Producto();
        producto.setCodigo(codigo);
        copiarDatos(producto, datos);
        return new ProductoResponse(productos.saveAndFlush(producto));
    }

    public ProductoResponse actualizarProducto(String codigo, ActualizarProductoRequest datos) {
        if (datos.precio() == null && datos.stock() == null)
            throw new ReglaNegocioException("Ingresa un precio o una cantidad");
        Producto producto = buscarActivo(codigo);
        if (datos.precio() != null) producto.setPrecio(datos.precio());
        if (datos.stock() != null) producto.setStock(datos.stock());
        return new ProductoResponse(producto);
    }

    public ProductoResponse reemplazarProducto(String codigo, ProductoRequest datos) {
        if (!codigo.equals(datos.codigo().trim().toUpperCase(Locale.ROOT)))
            throw new ReglaNegocioException("No se puede cambiar el código del producto");
        Producto producto = buscarActivo(codigo);
        copiarDatos(producto, datos);
        return new ProductoResponse(producto);
    }

    public void eliminarProducto(String codigo) { buscarActivo(codigo).setActivo(false); }

    //ordena los codigos y suma duplicados antes de bloquear para evitar sobreventa y bloqueos cruzados
    public List<ProductoDescontado> descontarStock(List<ItemStockRequest> items) {
        var cantidades = agrupar(items);
        var resultado = new ArrayList<ProductoDescontado>();
        for (var item : cantidades.entrySet()) {
            Producto producto = productos.bloquearPorCodigo(item.getKey())
                    .filter(Producto::isActivo)
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos ya no existe."));
            if (item.getValue() > producto.getStock())
                throw new ReglaNegocioException("No hay stock suficiente de " + producto.getNombre() + ".");
            producto.setStock(producto.getStock() - item.getValue());
            resultado.add(new ProductoDescontado(producto.getCodigo(), producto.getNombre(),
                    producto.getPrecio(), item.getValue()));
        }
        return resultado;
    }

    public void reponerStock(List<ItemStockRequest> items) {
        for (var item : agrupar(items).entrySet()) {
            //la compensacion tambien repone productos dados de baja despues de la compra
            Producto producto = productos.bloquearPorCodigo(item.getKey())
                    .orElseThrow(() -> new ReglaNegocioException("Uno de los productos ya no existe."));
            long nuevo = (long) producto.getStock() + item.getValue();
            if (nuevo > Integer.MAX_VALUE) throw new ReglaNegocioException("El stock excede el máximo permitido");
            producto.setStock((int) nuevo);
        }
    }

    private Map<String, Integer> agrupar(List<ItemStockRequest> items) {
        if (items.isEmpty()) throw new ReglaNegocioException("Agrega al menos un producto");
        Map<String, Integer> cantidades = new TreeMap<>();
        for (var item : items) {
            long suma = (long) cantidades.getOrDefault(item.codigo(), 0) + item.cantidad();
            if (suma > Integer.MAX_VALUE) throw new ReglaNegocioException("La cantidad excede el máximo permitido");
            cantidades.put(item.codigo(), (int) suma);
        }
        return cantidades;
    }

    private Producto buscarActivo(String codigo) {
        return productos.bloquearPorCodigo(codigo).filter(Producto::isActivo)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
    }

    private void copiarDatos(Producto producto, ProductoRequest datos) {
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(datos.categoria().trim());
        producto.setSubcategoria(datos.subcategoria().trim());
        producto.setMarca(datos.marca().trim());
        producto.setUnidad(datos.unidad().trim());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setStockMinimo(datos.stockMinimo());
    }
}
