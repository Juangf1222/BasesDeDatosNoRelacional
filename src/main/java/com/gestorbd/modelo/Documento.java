package com.gestorbd.modelo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representa un documento JSON almacenado en la base de datos no relacional.
 *
 * Cada documento posee:
 *  - Un identificador único (id) que actúa como clave principal en el árbol AVL.
 *  - Un conjunto dinámico de campos (clave → valor) que puede contener
 *    cualquier dato representable en JSON.
 *
 * Paquete: gestorbd.modelo
 * Autor:   Gestor BD No Relacional
 */
public class Documento {

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Clave principal del documento; único dentro de una colección. */
    private String id;

    /** Campos adicionales del documento, mantenidos en orden de inserción. */
    private Map<String, Object> campos;

    // ─── Constructores ────────────────────────────────────────────────────────

    /** Constructor por defecto. */
    public Documento() {
        this.campos = new LinkedHashMap<>();
    }

    /**
     * Constructor completo.
     *
     * @param id     Identificador único del documento.
     * @param campos Mapa de campos clave-valor.
     */
    public Documento(String id, Map<String, Object> campos) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El id del documento no puede ser nulo ni vacío.");
        }
        this.id     = id.trim();
        this.campos = new LinkedHashMap<>(campos);
        this.campos.remove("id"); // el id no se duplica en campos
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El id no puede ser nulo ni vacío.");
        }
        this.id = id.trim();
    }

    public Map<String, Object> getCampos() {
        return campos;
    }

    public void setCampos(Map<String, Object> campos) {
        this.campos = (campos != null) ? new LinkedHashMap<>(campos) : new LinkedHashMap<>();
        this.campos.remove("id");
    }

    // ─── Operaciones sobre campos ─────────────────────────────────────────────

    /**
     * Devuelve el valor de un campo dado su nombre.
     * Si la clave es "id" retorna el id del documento.
     *
     * @param clave Nombre del campo.
     * @return Valor del campo, o {@code null} si no existe.
     */
    public Object getCampo(String clave) {
        if ("id".equalsIgnoreCase(clave)) return id;
        return campos.get(clave);
    }

    /**
     * Establece el valor de un campo.
     * Si la clave es "id", actualiza el identificador del documento.
     *
     * @param clave Nombre del campo.
     * @param valor Valor a asignar.
     */
    public void setCampo(String clave, Object valor) {
        if ("id".equalsIgnoreCase(clave)) {
            setId(valor != null ? valor.toString() : null);
        } else {
            campos.put(clave, valor);
        }
    }

    /**
     * Indica si el documento contiene el campo especificado.
     *
     * @param clave Nombre del campo.
     * @return {@code true} si existe, {@code false} en caso contrario.
     */
    public boolean tieneCampo(String clave) {
        if ("id".equalsIgnoreCase(clave)) return id != null;
        return campos.containsKey(clave);
    }

    /**
     * Elimina un campo del documento (no se puede eliminar "id").
     *
     * @param clave Nombre del campo a eliminar.
     */
    public void eliminarCampo(String clave) {
        if ("id".equalsIgnoreCase(clave)) {
            throw new IllegalArgumentException("No se puede eliminar el campo 'id'.");
        }
        campos.remove(clave);
    }

    // ─── Conversión a/desde Map ───────────────────────────────────────────────

    /**
     * Convierte el documento completo a un {@link Map}, incluyendo el campo "id"
     * como primera entrada.
     *
     * @return Mapa completo del documento.
     */
    public Map<String, Object> aMap() {
        Map<String, Object> mapa = new LinkedHashMap<>();
        mapa.put("id", id);
        mapa.putAll(campos);
        return mapa;
    }

    /**
     * Crea un {@link Documento} a partir de un mapa que debe contener el campo "id".
     *
     * @param mapa Mapa fuente.
     * @return Documento creado.
     * @throws IllegalArgumentException Si el mapa no contiene "id".
     */
    public static Documento desdeMap(Map<String, Object> mapa) {
        if (mapa == null || !mapa.containsKey("id")) {
            throw new IllegalArgumentException("El mapa debe contener el campo 'id'.");
        }
        Object valorId = mapa.get("id");
        if (valorId == null) {
            throw new IllegalArgumentException("El campo 'id' no puede ser nulo.");
        }
        String id = valorId.toString();
        Map<String, Object> campos = new LinkedHashMap<>(mapa);
        campos.remove("id");
        return new Documento(id, campos);
    }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Documento{ id='").append(id).append("'");
        for (Map.Entry<String, Object> e : campos.entrySet()) {
            sb.append(", ").append(e.getKey()).append("='").append(e.getValue()).append("'");
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Documento)) return false;
        Documento otro = (Documento) obj;
        return id != null && id.equals(otro.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? id.hashCode() : 0;
    }
}