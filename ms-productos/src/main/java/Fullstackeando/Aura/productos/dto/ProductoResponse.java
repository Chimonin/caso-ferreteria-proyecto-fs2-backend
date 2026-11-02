package Fullstackeando.Aura.productos.dto;

import Fullstackeando.Aura.productos.model.Producto;

//los mismos datos que devuelve crearProductoDTO en el frontend
public class ProductoResponse {

    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final String subcategoria;
    private final String marca;
    private final String unidad;
    private final int precio;
    private final int stock;
    private final int stockMinimo;
    private final boolean stockBajo;

    public ProductoResponse(Producto producto) {
        codigo = producto.getCodigo();
        nombre = producto.getNombre();
        categoria = producto.getCategoria();
        subcategoria = producto.getSubcategoria();
        marca = producto.getMarca();
        unidad = producto.getUnidad();
        precio = producto.getPrecio();
        stock = producto.getStock();
        stockMinimo = producto.getStockMinimo();
        stockBajo = stock < stockMinimo;
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoria; }
    public String getSubcategoria() { return subcategoria; }
    public String getMarca() { return marca; }
    public String getUnidad() { return unidad; }
    public int getPrecio() { return precio; }
    public int getStock() { return stock; }
    public int getStockMinimo() { return stockMinimo; }
    public boolean isStockBajo() { return stockBajo; }
}
