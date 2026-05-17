package com.gestorbd.persistencia;

import com.gestorbd.modelo.Documento;
import com.gestorbd.util.JsonUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Capa de persistencia del gestor de base de datos.
 *
 * Responsabilidades:
 *  • Guardar una colección completa de documentos en un archivo .json.
 *  • Cargar una colección completa desde un archivo .json al iniciar.
 *  • Gestionar el directorio de almacenamiento (crearlo si no existe).
 *  • Listar las colecciones existentes.
 *  • Eliminar una colección de disco.
 *
 * Formato del archivo:
 *  Se usa un arreglo JSON con un objeto por documento, con indentación de 2
 *  espacios para que el archivo sea legible por humanos.
 *
 *  Ejemplo: empleados.json
 *  <pre>
 *  [
 *    {"id":"emp001","nombre":"Ana García","cargo":"Desarrolladora","salario":4500000},
 *    {"id":"emp002","nombre":"Luis Pérez","cargo":"Diseñador","salario":3800000}
 *  ]
 *  </pre>
 *
 * Cada vez que se modifica la colección en memoria se invoca {@link #guardarTodos}
 * para que el archivo refleje el estado actual (persistencia automática).
 *
 * Paquete: gestorbd.persistencia
 */
public class PersistenciaJSON {

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Ruta al directorio donde se almacenan los archivos .json. */
    private final String directorioBase;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Crea una instancia de PersistenciaJSON apuntando al directorio dado.
     * Si el directorio no existe, lo crea automáticamente.
     *
     * @param directorioBase Ruta al directorio de datos (ej.: "data").
     */
    public PersistenciaJSON(String directorioBase) {
        this.directorioBase = directorioBase;
        inicializarDirectorio();
    }

    // ─── Inicialización ───────────────────────────────────────────────────────

    /**
     * Crea el directorio base si no existe.
     */
    private void inicializarDirectorio() {
        File dir = new File(directorioBase);
        if (!dir.exists()) {
            boolean creado = dir.mkdirs();
            if (!creado) {
                System.err.println("[PersistenciaJSON] Advertencia: no se pudo crear el directorio '"
                        + directorioBase + "'.");
            }
        }
    }

    // ─── Ruta del archivo ─────────────────────────────────────────────────────

    /**
     * Construye la ruta completa del archivo .json de una colección.
     *
     * @param coleccion Nombre de la colección.
     * @return Ruta absoluta/relativa del archivo.
     */
    private String obtenerRutaArchivo(String coleccion) {
        return directorioBase + File.separator + coleccion + ".json";
    }

    // =========================================================================
    // SECCIÓN 1 – GUARDAR (Persistencia completa)
    // =========================================================================

    /**
     * Serializa y guarda la lista completa de documentos de una colección
     * en el archivo {@code <coleccion>.json}.
     *
     * Si el archivo ya existe, es sobreescrito completamente.
     * El formato de salida es un arreglo JSON con indentación de 2 espacios.
     *
     * @param coleccion  Nombre de la colección.
     * @param documentos Lista de documentos a persistir (puede estar vacía).
     * @throws IOException Si ocurre un error de escritura.
     */
    public void guardarTodos(String coleccion, List<Documento> documentos)
            throws IOException {

        String ruta = obtenerRutaArchivo(coleccion);

        // Construir el JSON del arreglo manualmente para control total
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < documentos.size(); i++) {
            Map<String, Object> mapa = documentos.get(i).aMap();
            // Indentación de 2 espacios por elemento
            String jsonDoc = JsonUtil.serializar(mapa, 2);
            // Añadir sangría al inicio de cada línea del objeto
            String indentado = indentarBloque(jsonDoc, "  ");
            sb.append(indentado);
            if (i < documentos.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append(']');

        // Escritura atómica: escribir a un archivo temporal y luego renombrar
        File archivoFinal = new File(ruta);
        File archivoTmp   = new File(ruta + ".tmp");

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(archivoTmp), StandardCharsets.UTF_8))) {
            writer.write(sb.toString());
        }

        // Renombramiento atómico (en la misma partición no hay riesgo de corrupción)
        if (archivoFinal.exists()) archivoFinal.delete();
        boolean renombrado = archivoTmp.renameTo(archivoFinal);
        if (!renombrado) {
            // Si rename falla (distinta partición), copiar y borrar
            Files.copy(archivoTmp.toPath(), archivoFinal.toPath(),
                       StandardCopyOption.REPLACE_EXISTING);
            archivoTmp.delete();
        }
    }

    /**
     * Aplica una sangría (prefijo) a cada línea de un bloque de texto.
     */
    private String indentarBloque(String texto, String sangria) {
        String[] lineas = texto.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lineas.length; i++) {
            sb.append(sangria).append(lineas[i]);
            if (i < lineas.length - 1) sb.append('\n');
        }
        return sb.toString();
    }

    // =========================================================================
    // SECCIÓN 2 – CARGAR (Lectura al iniciar)
    // =========================================================================

    /**
     * Lee el archivo de una colección y devuelve la lista de documentos.
     * Si el archivo no existe, devuelve una lista vacía (primera ejecución).
     *
     * @param coleccion Nombre de la colección.
     * @return Lista de documentos leídos del archivo.
     * @throws IOException Si ocurre un error de lectura inesperado.
     */
    public List<Documento> cargarTodos(String coleccion) throws IOException {
        String ruta      = obtenerRutaArchivo(coleccion);
        File   archivo   = new File(ruta);
        List<Documento>  lista = new ArrayList<>();

        if (!archivo.exists()) {
            return lista; // colección nueva, sin datos previos
        }

        // Leer todo el contenido del archivo
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(archivo), StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea).append('\n');
            }
        }

        String contenido = sb.toString().trim();
        if (contenido.isEmpty() || contenido.equals("[]")) {
            return lista; // archivo vacío o arreglo vacío
        }

        // Deserializar el arreglo JSON
        List<Map<String, Object>> mapas;
        try {
            mapas = JsonUtil.deserializarArreglo(contenido);
        } catch (Exception e) {
            throw new IOException(
                    "Error al parsear el archivo '" + ruta + "': " + e.getMessage(), e);
        }

        // Convertir cada mapa a un Documento
        for (Map<String, Object> mapa : mapas) {
            try {
                lista.add(Documento.desdeMap(mapa));
            } catch (Exception e) {
                System.err.println("[PersistenciaJSON] Documento omitido por error: "
                        + e.getMessage());
            }
        }

        return lista;
    }

    // =========================================================================
    // SECCIÓN 3 – GESTIÓN DE COLECCIONES
    // =========================================================================

    /**
     * Lista los nombres de todas las colecciones existentes en el directorio base.
     *
     * @return Lista de nombres de colecciones (sin extensión .json).
     */
    public List<String> listarColecciones() {
        List<String> colecciones = new ArrayList<>();
        File dir = new File(directorioBase);
        File[] archivos = dir.listFiles(
                (d, nombre) -> nombre.toLowerCase().endsWith(".json"));

        if (archivos != null) {
            for (File f : archivos) {
                String nombre = f.getName();
                colecciones.add(nombre.substring(0, nombre.length() - 5));
            }
        }
        return colecciones;
    }

    /**
     * Indica si existe el archivo de una colección.
     *
     * @param coleccion Nombre de la colección.
     * @return {@code true} si el archivo existe.
     */
    public boolean existeColeccion(String coleccion) {
        return new File(obtenerRutaArchivo(coleccion)).exists();
    }

    /**
     * Elimina el archivo de una colección del disco.
     *
     * @param coleccion Nombre de la colección a eliminar.
     * @return {@code true} si el archivo fue eliminado, {@code false} si no existía.
     */
    public boolean eliminarColeccion(String coleccion) {
        File archivo = new File(obtenerRutaArchivo(coleccion));
        return archivo.exists() && archivo.delete();
    }

    /**
     * Devuelve el directorio base de almacenamiento.
     *
     * @return Ruta al directorio de datos.
     */
    public String getDirectorioBase() {
        return directorioBase;
    }
}