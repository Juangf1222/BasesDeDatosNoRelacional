package com.gestorbd.servicio;

import com.gestorbd.estructura.ArbolAVL;
import com.gestorbd.modelo.Documento;
import com.gestorbd.persistencia.PersistenciaJSON;

import java.io.IOException;
import java.util.*;

/**
 * Gestor principal de la base de datos no relacional.
 *
 * Actúa como fachada (patrón Facade) entre la capa de presentación y las
 * capas de estructura e infraestructura. Coordina:
 *  - Los árboles AVL en memoria (uno por colección).
 *  - La capa de persistencia (PersistenciaJSON).
 *  - Las operaciones CRUD con persistencia automática.
 *
 * Colecciones:
 *  Cada "colección" es equivalente a una tabla lógica. Internamente se
 *  representa como un {@link ArbolAVL} en memoria y un archivo .json en disco.
 *  Se pueden tener múltiples colecciones abiertas simultáneamente en caché.
 *
 * Flujo de una operación de escritura:
 *  1. Validar parámetros.
 *  2. Operar sobre el árbol AVL en memoria.
 *  3. Persistir automáticamente el estado completo en disco.
 *
 * Paquete: gestorbd.servicio
 */
public class GestorBaseDatos {

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Caché de colecciones en memoria: nombre → árbol AVL. */
    private final Map<String, ArbolAVL> colecciones;

    /** Capa de persistencia JSON. */
    private final PersistenciaJSON persistencia;

    /** Nombre de la colección seleccionada actualmente. */
    private String coleccionActual;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Crea el gestor apuntando al directorio de datos dado.
     *
     * @param directorioBase Ruta al directorio donde se guardan los archivos .json.
     */
    public GestorBaseDatos(String directorioBase) {
        this.colecciones    = new LinkedHashMap<>();
        this.persistencia   = new PersistenciaJSON(directorioBase);
        this.coleccionActual = null;
    }

    // =========================================================================
    // SECCIÓN 1 – GESTIÓN DE COLECCIONES
    // =========================================================================

    /**
     * Selecciona (y carga si es necesario) la colección activa.
     *
     * Si la colección ya está en caché, la activa sin releer el disco.
     * Si es nueva o no estaba en caché, la carga desde el archivo .json.
     *
     * @param nombre Nombre de la colección (insensible a mayúsculas).
     * @throws IOException Si falla la lectura del archivo.
     */
    public void seleccionarColeccion(String nombre) throws IOException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la colección no puede estar vacío.");
        }
        String clave = nombre.trim().toLowerCase();
        coleccionActual = clave;

        if (!colecciones.containsKey(clave)) {
            // Cargar desde disco
            ArbolAVL arbol = new ArbolAVL();
            List<Documento> documentos = persistencia.cargarTodos(clave);
            for (Documento doc : documentos) {
                arbol.insertar(doc);
            }
            colecciones.put(clave, arbol);
            System.out.println("[GestorBD] Colección '" + clave + "' cargada ("
                    + documentos.size() + " documentos).");
        }
    }

    /**
     * Elimina una colección de la caché en memoria y del disco.
     *
     * @param nombre Nombre de la colección a eliminar.
     * @return {@code true} si existía y fue eliminada.
     */
    public boolean eliminarColeccion(String nombre) {
        if (nombre == null) return false;
        String clave = nombre.trim().toLowerCase();
        colecciones.remove(clave);
        if (clave.equals(coleccionActual)) coleccionActual = null;
        return persistencia.eliminarColeccion(clave);
    }

    /**
     * Devuelve los nombres de todas las colecciones existentes en disco.
     *
     * @return Lista de nombres de colecciones.
     */
    public List<String> listarColecciones() {
        return persistencia.listarColecciones();
    }

    /**
     * Indica si hay una colección actualmente seleccionada.
     *
     * @return {@code true} si hay una colección activa.
     */
    public boolean hayColeccionActiva() {
        return coleccionActual != null && colecciones.containsKey(coleccionActual);
    }

    // ─── Acceso interno al árbol ──────────────────────────────────────────────

    /**
     * Obtiene el árbol AVL de la colección activa.
     *
     * @return Árbol AVL de la colección activa.
     * @throws IllegalStateException Si no hay ninguna colección seleccionada.
     */
    private ArbolAVL obtenerArbolActual() {
        if (!hayColeccionActiva()) {
            throw new IllegalStateException(
                    "No hay ninguna colección seleccionada. Use seleccionarColeccion() primero.");
        }
        return colecciones.get(coleccionActual);
    }

    /**
     * Persiste el estado actual de la colección activa en disco.
     *
     * @throws IOException Si falla la escritura.
     */
    private void persistir() throws IOException {
        List<Documento> todos = obtenerArbolActual().inorden();
        persistencia.guardarTodos(coleccionActual, todos);
    }

    // =========================================================================
    // SECCIÓN 2 – OPERACIONES CRUD
    // =========================================================================

    // ── 2.1 Insertar ─────────────────────────────────────────────────────────

    /**
     * Inserta un nuevo documento en la colección activa.
     * El id del documento debe ser único dentro de la colección.
     *
     * Complejidad: O(log n) árbol + O(n) persistencia.
     *
     * @param documento Documento a insertar.
     * @throws IllegalArgumentException Si ya existe un documento con ese id.
     * @throws IOException              Si falla la persistencia.
     */
    public void insertar(Documento documento) throws IOException {
        obtenerArbolActual().insertar(documento);
        persistir();
    }

    // ── 2.2 Buscar por ID ─────────────────────────────────────────────────────

    /**
     * Busca y devuelve el documento con el id especificado.
     *
     * Complejidad: O(log n).
     *
     * @param id Identificador a buscar.
     * @return Documento encontrado, o {@code null} si no existe.
     */
    public Documento buscarPorId(String id) {
        return obtenerArbolActual().buscar(id);
    }

    /**
     * Indica si existe un documento con el id dado.
     *
     * @param id Identificador a verificar.
     * @return {@code true} si existe.
     */
    public boolean existeId(String id) {
        return buscarPorId(id) != null;
    }

    // ── 2.3 Buscar por criterio ───────────────────────────────────────────────

    /**
     * Busca documentos cuyo campo {@code campo} contenga el valor dado
     * (búsqueda parcial, insensible a mayúsculas).
     *
     * Complejidad: O(n) (recorre todo el árbol).
     *
     * @param campo  Nombre del campo a filtrar.
     * @param valor  Subcadena a buscar.
     * @return Lista de documentos que cumplen el criterio.
     */
    public List<Documento> buscarPorCriterio(String campo, String valor) {
        return obtenerArbolActual().buscarPorCriterio(campo, valor);
    }

    /**
     * Devuelve todos los documentos de la colección activa ordenados por id.
     *
     * Complejidad: O(n).
     *
     * @return Lista ordenada de documentos.
     */
    public List<Documento> listarTodos() {
        return obtenerArbolActual().inorden();
    }

    // ── 2.4 Actualizar ───────────────────────────────────────────────────────

    /**
     * Reemplaza el documento con el id dado por {@code documentoActualizado}.
     * El id del documento no puede cambiar mediante esta operación.
     *
     * Complejidad: O(log n) árbol + O(n) persistencia.
     *
     * @param id                   Id del documento a actualizar.
     * @param documentoActualizado Nuevo estado del documento.
     * @return {@code true} si se actualizó, {@code false} si el id no existe.
     * @throws IOException Si falla la persistencia.
     */
    public boolean actualizar(String id, Documento documentoActualizado) throws IOException {
        boolean actualizado = obtenerArbolActual().actualizar(id, documentoActualizado);
        if (actualizado) persistir();
        return actualizado;
    }

    /**
     * Actualiza un campo específico de un documento existente,
     * preservando el resto de campos.
     *
     * @param id    Id del documento a modificar.
     * @param campo Nombre del campo a actualizar.
     * @param valor Nuevo valor del campo.
     * @return {@code true} si se actualizó, {@code false} si el id no existe.
     * @throws IOException Si falla la persistencia.
     */
    public boolean actualizarCampo(String id, String campo, Object valor) throws IOException {
        Documento doc = buscarPorId(id);
        if (doc == null) return false;

        doc.setCampo(campo, valor);
        boolean resultado = obtenerArbolActual().actualizar(id, doc);
        if (resultado) persistir();
        return resultado;
    }

    // ── 2.5 Eliminar ──────────────────────────────────────────────────────────

    /**
     * Elimina el documento con el id especificado de la colección activa.
     *
     * Complejidad: O(log n) árbol + O(n) persistencia.
     *
     * @param id Id del documento a eliminar.
     * @return {@code true} si se eliminó, {@code false} si el id no existía.
     * @throws IOException Si falla la persistencia.
     */
    public boolean eliminar(String id) throws IOException {
        boolean eliminado = obtenerArbolActual().eliminar(id);
        if (eliminado) persistir();
        return eliminado;
    }

    // =========================================================================
    // SECCIÓN 3 – INFORMACIÓN DE LA COLECCIÓN
    // =========================================================================

    /**
     * @return Nombre de la colección actualmente activa, o {@code null}.
     */
    public String getColeccionActual() {
        return coleccionActual;
    }

    /**
     * @return Número de documentos en la colección activa.
     */
    public int contarDocumentos() {
        return obtenerArbolActual().getTamano();
    }

    /**
     * @return {@code true} si la colección activa no tiene documentos.
     */
    public boolean coleccionVacia() {
        return obtenerArbolActual().estaVacio();
    }

    /**
     * Imprime la estructura visual del árbol AVL de la colección activa.
     * Útil para fines académicos y de demostración.
     */
    public void imprimirArbol() {
        System.out.println();
        System.out.println("  Árbol AVL – Colección: " + coleccionActual);
        System.out.println("  ─────────────────────────────────────");
        obtenerArbolActual().imprimirEstructura();
        System.out.println("  ─────────────────────────────────────");
        System.out.println("  Válido AVL: " + obtenerArbolActual().esValido());
        System.out.println();
    }

    /**
     * Fuerza la recarga de una colección desde disco (descarta la caché).
     *
     * @param nombre Nombre de la colección.
     * @throws IOException Si falla la lectura.
     */
    public void recargarColeccion(String nombre) throws IOException {
        if (nombre == null) return;
        colecciones.remove(nombre.trim().toLowerCase());
        seleccionarColeccion(nombre);
    }
}