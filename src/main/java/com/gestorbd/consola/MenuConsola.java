package com.gestorbd.consola;

import com.gestorbd.modelo.Documento;
import com.gestorbd.servicio.GestorBaseDatos;
import com.gestorbd.util.JsonUtil;

import java.io.IOException;
import java.util.*;

/**
 * Interfaz de usuario por consola del Gestor de Base de Datos No Relacional.
 *
 * Implementa el ciclo principal de la aplicación mediante un menú interactivo
 * que cubre todas las operaciones CRUD y las funciones de administración de
 * colecciones.
 *
 * Responsabilidades:
 *  • Presentar el menú principal y los submenús.
 *  • Capturar y validar la entrada del usuario.
 *  • Delegar las operaciones al {@link GestorBaseDatos}.
 *  • Mostrar los resultados de forma legible y formateada.
 *  • Manejar errores de E/S y de lógica sin interrumpir la sesión.
 *
 * Paquete: gestorbd.consola
 */
public class MenuConsola {

    // ─── Constantes de diseño ─────────────────────────────────────────────────

    private static final String LINEA   = "═".repeat(60);
    private static final String LINEA_S = "─".repeat(60);
    private static final String ANCHO   = "%-58s";

    // ─── Atributos ────────────────────────────────────────────────────────────

    private final GestorBaseDatos gestor;
    private final Scanner scanner;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Crea el menú de consola con el directorio de datos por defecto ("data").
     */
    public MenuConsola() {
        this.gestor  = new GestorBaseDatos("data");
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    /**
     * Crea el menú de consola con un directorio de datos personalizado.
     *
     * @param directorioBase Ruta al directorio de almacenamiento.
     */
    public MenuConsola(String directorioBase) {
        this.gestor  = new GestorBaseDatos(directorioBase);
        this.scanner = new Scanner(System.in, "UTF-8");
    }

    // =========================================================================
    // PUNTO DE ENTRADA PRINCIPAL
    // =========================================================================

    /**
     * Inicia el ciclo de vida de la aplicación.
     * Muestra el splash, pide la colección inicial y entra al menú principal.
     */
    public void iniciar() {
        mostrarSplash();
        seleccionarOCrearColeccionInicial();
        menuPrincipal();
        System.out.println("\n  ¡Hasta luego! Los datos han sido guardados.\n");
        scanner.close();
    }

    // =========================================================================
    // SECCIÓN 1 – PANTALLAS GENERALES
    // =========================================================================

    private void mostrarSplash() {
        System.out.println();
        System.out.println("  " + LINEA);
        System.out.println("  ║  GESTOR DE BASE DE DATOS NO RELACIONAL              ║");
        System.out.println("  ║  Motor: Árbol AVL autobalanceado  |  Java SE         ║");
        System.out.println("  ║  Almacenamiento: JSON  |  Complejidad O(log n)       ║");
        System.out.println("  " + LINEA);
        System.out.println();
    }

    private void mostrarEncabezado(String titulo) {
        String info = gestor.hayColeccionActiva()
                ? "Colección: " + gestor.getColeccionActual()
                  + "  (" + gestor.contarDocumentos() + " doc.)"
                : "Sin colección activa";
        System.out.println();
        System.out.println("  " + LINEA);
        System.out.printf("  ║  %-56s ║%n", titulo);
        System.out.printf("  ║  %-56s ║%n", info);
        System.out.println("  " + LINEA);
    }

    // =========================================================================
    // SECCIÓN 2 – SELECCIÓN DE COLECCIÓN INICIAL
    // =========================================================================

    private void seleccionarOCrearColeccionInicial() {
        List<String> existentes = gestor.listarColecciones();

        System.out.println("  Colecciones disponibles en disco:");
        if (existentes.isEmpty()) {
            System.out.println("    (ninguna – se creará una nueva)");
        } else {
            for (int i = 0; i < existentes.size(); i++) {
                System.out.printf("    [%d] %s%n", i + 1, existentes.get(i));
            }
        }

        System.out.println();
        System.out.print("  Ingrese el nombre de la colección a usar: ");
        String nombre = leerLinea();

        if (nombre.isEmpty()) nombre = "documentos";

        try {
            gestor.seleccionarColeccion(nombre);
            System.out.println("  ✔  Colección '" + nombre + "' lista.\n");
        } catch (IOException e) {
            System.err.println("  ✘  Error al cargar la colección: " + e.getMessage());
        }
    }

    // =========================================================================
    // SECCIÓN 3 – MENÚ PRINCIPAL
    // =========================================================================

    private void menuPrincipal() {
        boolean salir = false;

        while (!salir) {
            mostrarEncabezado("MENÚ PRINCIPAL");
            System.out.println();
            System.out.println("     OPERACIONES CRUD");
            System.out.println("     ─────────────────────────────────────────────────");
            System.out.println("     1. Insertar documento");
            System.out.println("     2. Buscar por ID");
            System.out.println("     3. Buscar por campo / criterio");
            System.out.println("     4. Actualizar documento completo");
            System.out.println("     5. Actualizar un campo específico");
            System.out.println("     6. Eliminar documento");
            System.out.println("     7. Listar todos los documentos");
            System.out.println();
            System.out.println("     ADMINISTRACIÓN");
            System.out.println("     ─────────────────────────────────────────────────");
            System.out.println("     8. Cambiar / crear colección");
            System.out.println("     9. Eliminar colección actual");
            System.out.println("    10. Ver estructura del árbol AVL");
            System.out.println();
            System.out.println("     0. Salir");
            System.out.println();

            int opcion = leerOpcion("  >> Opción: ", 0, 10);

            switch (opcion) {
                case 1:  accionInsertar();           break;
                case 2:  accionBuscarPorId();        break;
                case 3:  accionBuscarPorCriterio();  break;
                case 4:  accionActualizarCompleto();  break;
                case 5:  accionActualizarCampo();    break;
                case 6:  accionEliminar();            break;
                case 7:  accionListarTodos();        break;
                case 8:  accionCambiarColeccion();   break;
                case 9:  accionEliminarColeccion();  break;
                case 10: accionVerArbol();            break;
                case 0:  salir = true;               break;
            }
        }
    }

    // =========================================================================
    // SECCIÓN 4 – ACCIONES CRUD
    // =========================================================================

    // ── 4.1 Insertar ─────────────────────────────────────────────────────────

    private void accionInsertar() {
        mostrarEncabezado("INSERTAR DOCUMENTO");
        System.out.println();

        System.out.print("  ID del documento (clave única): ");
        String id = leerLinea();
        if (id.isEmpty()) { System.out.println("  ✘  El ID no puede estar vacío."); pausar(); return; }

        if (gestor.existeId(id)) {
            System.out.println("  ✘  Ya existe un documento con id='" + id + "'.");
            pausar();
            return;
        }

        System.out.println("  Ingrese los campos del documento.");
        System.out.println("  (Deje el nombre del campo vacío para terminar)");
        System.out.println();

        Map<String, Object> campos = new LinkedHashMap<>();
        while (true) {
            System.out.print("    Nombre del campo: ");
            String campo = leerLinea();
            if (campo.isEmpty()) break;
            if ("id".equalsIgnoreCase(campo)) {
                System.out.println("    ✘  El campo 'id' es reservado.");
                continue;
            }
            System.out.print("    Valor de '" + campo + "': ");
            String valor = leerLinea();
            campos.put(campo, parsearValor(valor));
        }

        if (campos.isEmpty()) {
            System.out.println("  ✘  El documento debe tener al menos un campo.");
            pausar();
            return;
        }

        Documento doc = new Documento(id, campos);
        try {
            gestor.insertar(doc);
            System.out.println();
            System.out.println("  ✔  Documento insertado correctamente:");
            mostrarDocumento(doc);
        } catch (IllegalArgumentException e) {
            System.out.println("  ✘  Error: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("  ✘  Error de persistencia: " + e.getMessage());
        }
        pausar();
    }

    // ── 4.2 Buscar por ID ─────────────────────────────────────────────────────

    private void accionBuscarPorId() {
        mostrarEncabezado("BUSCAR POR ID");
        System.out.println();

        System.out.print("  ID a buscar: ");
        String id = leerLinea();
        if (id.isEmpty()) { pausar(); return; }

        Documento doc = gestor.buscarPorId(id);
        System.out.println();
        if (doc != null) {
            System.out.println("  ✔  Documento encontrado:");
            mostrarDocumento(doc);
        } else {
            System.out.println("  ✘  No se encontró ningún documento con id='" + id + "'.");
        }
        pausar();
    }

    // ── 4.3 Buscar por criterio ───────────────────────────────────────────────

    private void accionBuscarPorCriterio() {
        mostrarEncabezado("BUSCAR POR CRITERIO");
        System.out.println();

        System.out.print("  Campo a filtrar: ");
        String campo = leerLinea();
        if (campo.isEmpty()) { pausar(); return; }

        System.out.print("  Valor a buscar (búsqueda parcial): ");
        String valor = leerLinea();

        List<Documento> resultados = gestor.buscarPorCriterio(campo, valor);
        System.out.println();
        System.out.println("  " + LINEA_S);
        System.out.printf("  Resultados encontrados: %d%n", resultados.size());
        System.out.println("  " + LINEA_S);

        if (resultados.isEmpty()) {
            System.out.println("  (Sin resultados)");
        } else {
            for (int i = 0; i < resultados.size(); i++) {
                System.out.printf("%n  [%d/%d]%n", i + 1, resultados.size());
                mostrarDocumento(resultados.get(i));
            }
        }
        pausar();
    }

    // ── 4.4 Actualizar documento completo ─────────────────────────────────────

    private void accionActualizarCompleto() {
        mostrarEncabezado("ACTUALIZAR DOCUMENTO");
        System.out.println();

        System.out.print("  ID del documento a actualizar: ");
        String id = leerLinea();
        if (id.isEmpty()) { pausar(); return; }

        Documento actual = gestor.buscarPorId(id);
        if (actual == null) {
            System.out.println("  ✘  No existe un documento con id='" + id + "'.");
            pausar();
            return;
        }

        System.out.println("  Documento actual:");
        mostrarDocumento(actual);
        System.out.println();
        System.out.println("  Ingrese los nuevos campos (deje el nombre vacío para terminar):");
        System.out.println("  Tip: para mantener campos existentes, ingréselos de nuevo con su valor.");
        System.out.println();

        Map<String, Object> nuevosCampos = new LinkedHashMap<>();
        while (true) {
            System.out.print("    Campo: ");
            String campo = leerLinea();
            if (campo.isEmpty()) break;
            if ("id".equalsIgnoreCase(campo)) {
                System.out.println("    ✘  El campo 'id' no puede modificarse.");
                continue;
            }
            System.out.print("    Valor de '" + campo + "': ");
            nuevosCampos.put(campo, parsearValor(leerLinea()));
        }

        if (nuevosCampos.isEmpty()) {
            System.out.println("  ✘  No se ingresaron campos. Operación cancelada.");
            pausar();
            return;
        }

        Documento actualizado = new Documento(id, nuevosCampos);
        try {
            boolean ok = gestor.actualizar(id, actualizado);
            System.out.println();
            if (ok) {
                System.out.println("  ✔  Documento actualizado:");
                mostrarDocumento(actualizado);
            } else {
                System.out.println("  ✘  No se pudo actualizar el documento.");
            }
        } catch (IOException e) {
            System.out.println("  ✘  Error de persistencia: " + e.getMessage());
        }
        pausar();
    }

    // ── 4.5 Actualizar campo específico ──────────────────────────────────────

    private void accionActualizarCampo() {
        mostrarEncabezado("ACTUALIZAR CAMPO ESPECÍFICO");
        System.out.println();

        System.out.print("  ID del documento: ");
        String id = leerLinea();
        if (id.isEmpty()) { pausar(); return; }

        Documento doc = gestor.buscarPorId(id);
        if (doc == null) {
            System.out.println("  ✘  No existe un documento con id='" + id + "'.");
            pausar();
            return;
        }

        System.out.println("  Documento actual:");
        mostrarDocumento(doc);
        System.out.println();

        System.out.print("  Campo a actualizar: ");
        String campo = leerLinea();
        if (campo.isEmpty() || "id".equalsIgnoreCase(campo)) {
            System.out.println("  ✘  Campo inválido.");
            pausar();
            return;
        }

        System.out.print("  Nuevo valor: ");
        Object valor = parsearValor(leerLinea());

        try {
            boolean ok = gestor.actualizarCampo(id, campo, valor);
            System.out.println();
            if (ok) {
                System.out.println("  ✔  Campo '" + campo + "' actualizado a: " + valor);
                mostrarDocumento(gestor.buscarPorId(id));
            } else {
                System.out.println("  ✘  Error al actualizar.");
            }
        } catch (IOException e) {
            System.out.println("  ✘  Error de persistencia: " + e.getMessage());
        }
        pausar();
    }

    // ── 4.6 Eliminar ──────────────────────────────────────────────────────────

    private void accionEliminar() {
        mostrarEncabezado("ELIMINAR DOCUMENTO");
        System.out.println();

        System.out.print("  ID del documento a eliminar: ");
        String id = leerLinea();
        if (id.isEmpty()) { pausar(); return; }

        Documento doc = gestor.buscarPorId(id);
        if (doc == null) {
            System.out.println("  ✘  No existe un documento con id='" + id + "'.");
            pausar();
            return;
        }

        System.out.println("  Documento a eliminar:");
        mostrarDocumento(doc);
        System.out.print("\n  ¿Confirmar eliminación? (s/n): ");
        String confirm = leerLinea().toLowerCase();

        if (!confirm.equals("s") && !confirm.equals("si") && !confirm.equals("sí")) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        try {
            boolean ok = gestor.eliminar(id);
            System.out.println();
            if (ok) {
                System.out.println("  ✔  Documento con id='" + id + "' eliminado.");
            } else {
                System.out.println("  ✘  Error al eliminar el documento.");
            }
        } catch (IOException e) {
            System.out.println("  ✘  Error de persistencia: " + e.getMessage());
        }
        pausar();
    }

    // ── 4.7 Listar todos ─────────────────────────────────────────────────────

    private void accionListarTodos() {
        mostrarEncabezado("LISTAR TODOS LOS DOCUMENTOS");
        System.out.println();

        List<Documento> todos = gestor.listarTodos();
        if (todos.isEmpty()) {
            System.out.println("  La colección está vacía.");
        } else {
            System.out.println("  Total: " + todos.size() + " documento(s). Ordenados por ID (inorden AVL):");
            System.out.println("  " + LINEA_S);
            for (int i = 0; i < todos.size(); i++) {
                System.out.printf("%n  [%d/%d]%n", i + 1, todos.size());
                mostrarDocumento(todos.get(i));
            }
        }
        pausar();
    }

    // =========================================================================
    // SECCIÓN 5 – ACCIONES DE ADMINISTRACIÓN
    // =========================================================================

    // ── 5.1 Cambiar colección ─────────────────────────────────────────────────

    private void accionCambiarColeccion() {
        mostrarEncabezado("CAMBIAR / CREAR COLECCIÓN");
        System.out.println();

        List<String> existentes = gestor.listarColecciones();
        System.out.println("  Colecciones disponibles:");
        if (existentes.isEmpty()) {
            System.out.println("    (ninguna)");
        } else {
            for (int i = 0; i < existentes.size(); i++) {
                System.out.printf("    [%d] %s%n", i + 1, existentes.get(i));
            }
        }
        System.out.println();

        System.out.print("  Nombre de la colección (nueva o existente): ");
        String nombre = leerLinea();
        if (nombre.isEmpty()) { System.out.println("  Operación cancelada."); pausar(); return; }

        try {
            gestor.seleccionarColeccion(nombre);
            System.out.println("  ✔  Colección '" + nombre + "' activa ("
                    + gestor.contarDocumentos() + " documentos).");
        } catch (IOException e) {
            System.out.println("  ✘  Error: " + e.getMessage());
        }
        pausar();
    }

    // ── 5.2 Eliminar colección ────────────────────────────────────────────────

    private void accionEliminarColeccion() {
        mostrarEncabezado("ELIMINAR COLECCIÓN");
        System.out.println();

        if (!gestor.hayColeccionActiva()) {
            System.out.println("  No hay colección activa.");
            pausar();
            return;
        }

        String nombre = gestor.getColeccionActual();
        System.out.println("  ⚠  Se eliminará permanentemente la colección: '" + nombre + "'");
        System.out.println("  ⚠  Esto borrará " + gestor.contarDocumentos()
                + " documento(s) del disco.");
        System.out.print("\n  ¿Confirmar eliminación? (escriba 'ELIMINAR' para confirmar): ");
        String confirm = leerLinea();

        if (!confirm.equals("ELIMINAR")) {
            System.out.println("  Operación cancelada.");
            pausar();
            return;
        }

        boolean ok = gestor.eliminarColeccion(nombre);
        System.out.println();
        if (ok) {
            System.out.println("  ✔  Colección '" + nombre + "' eliminada.");
            System.out.println("  Seleccione una nueva colección para continuar.");
            pausar();
            seleccionarOCrearColeccionInicial();
        } else {
            System.out.println("  ✘  No se pudo eliminar la colección.");
            pausar();
        }
    }

    // ── 5.3 Ver árbol AVL ─────────────────────────────────────────────────────

    private void accionVerArbol() {
        mostrarEncabezado("ESTRUCTURA DEL ÁRBOL AVL");
        gestor.imprimirArbol();
        System.out.println("  Los nodos muestran: clave  [h=altura, fb=factor de balance]");
        System.out.println("  Factor de balance válido: -1, 0 o +1 por nodo.");
        pausar();
    }

    // =========================================================================
    // SECCIÓN 6 – PRESENTACIÓN DE DOCUMENTOS
    // =========================================================================

    /**
     * Muestra un documento en formato tabla con bordes Unicode.
     */
    private void mostrarDocumento(Documento doc) {
        System.out.println("  ┌" + "─".repeat(56) + "┐");
        System.out.printf("  │  %-54s│%n",
                "ID: " + doc.getId());
        System.out.println("  ├" + "─".repeat(56) + "┤");

        if (doc.getCampos().isEmpty()) {
            System.out.printf("  │  %-54s│%n", "(sin campos adicionales)");
        } else {
            for (Map.Entry<String, Object> e : doc.getCampos().entrySet()) {
                String linea = String.format("  %-20s: %s", e.getKey(),
                        formatearValor(e.getValue()));
                // Truncar si es demasiado largo
                if (linea.length() > 57) linea = linea.substring(0, 54) + "...";
                System.out.printf("  │  %-54s│%n", linea.trim());
            }
        }
        System.out.println("  └" + "─".repeat(56) + "┘");
    }

    /**
     * Muestra una lista de documentos de forma compacta (tabla de resumen).
     */
    private void mostrarResumenDocumentos(List<Documento> lista) {
        System.out.println("  " + LINEA_S);
        System.out.printf("  %-20s  %s%n", "ID", "Campos");
        System.out.println("  " + LINEA_S);
        for (Documento doc : lista) {
            StringBuilder campos = new StringBuilder();
            int cont = 0;
            for (Map.Entry<String, Object> e : doc.getCampos().entrySet()) {
                if (cont > 0) campos.append(", ");
                campos.append(e.getKey()).append(": ").append(formatearValor(e.getValue()));
                if (++cont >= 3) { campos.append(" ..."); break; }
            }
            String resumen = campos.toString();
            if (resumen.length() > 35) resumen = resumen.substring(0, 32) + "...";
            System.out.printf("  %-20s  %s%n", doc.getId(), resumen);
        }
        System.out.println("  " + LINEA_S);
    }

    private String formatearValor(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof String) return "\"" + valor + "\"";
        return valor.toString();
    }

    // =========================================================================
    // SECCIÓN 7 – UTILIDADES DE ENTRADA
    // =========================================================================

    /**
     * Lee una línea de texto del usuario, sin espacios al inicio ni al final.
     */
    private String leerLinea() {
        try {
            String linea = scanner.nextLine();
            return (linea != null) ? linea.trim() : "";
        } catch (NoSuchElementException e) {
            return "";
        }
    }

    /**
     * Lee y valida una opción numérica dentro del rango [min, max].
     */
    private int leerOpcion(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String entrada = leerLinea();
            try {
                int valor = Integer.parseInt(entrada);
                if (valor >= min && valor <= max) return valor;
                System.out.println("  ✘  Ingrese un número entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ✘  Opción inválida. Ingrese un número.");
            }
        }
    }

    /**
     * Pausa la ejecución hasta que el usuario presione Enter.
     */
    private void pausar() {
        System.out.println();
        System.out.print("  Presione Enter para continuar...");
        leerLinea();
    }

    /**
     * Intenta convertir una cadena de texto a su tipo de dato más apropiado.
     *
     * Orden de intento:
     *  1. Integer
     *  2. Long
     *  3. Double
     *  4. Boolean (true/false)
     *  5. String (fallback)
     *
     * @param entrada Cadena ingresada por el usuario.
     * @return El valor convertido al tipo más específico posible.
     */
    private Object parsearValor(String entrada) {
        if (entrada == null || entrada.isEmpty()) return "";

        // Boolean
        if (entrada.equalsIgnoreCase("true"))  return Boolean.TRUE;
        if (entrada.equalsIgnoreCase("false")) return Boolean.FALSE;

        // null
        if (entrada.equalsIgnoreCase("null")) return null;

        // Integer
        try { return Integer.parseInt(entrada); } catch (NumberFormatException ignored) {}

        // Long
        try { return Long.parseLong(entrada); } catch (NumberFormatException ignored) {}

        // Double
        try { return Double.parseDouble(entrada); } catch (NumberFormatException ignored) {}

        // String (por defecto)
        return entrada;
    }
}