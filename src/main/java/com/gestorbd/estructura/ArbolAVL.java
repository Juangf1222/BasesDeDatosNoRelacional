package com.gestorbd.estructura;

import com.gestorbd.modelo.Documento;
import java.util.ArrayList;
import java.util.List;

/**
 * Árbol AVL (Adelson-Velsky and Landis) implementado desde cero.
 *
 * El árbol se autobalancea mediante rotaciones tras cada inserción o
 * eliminación, garantizando complejidad O(log n) en todas sus operaciones:
 * búsqueda, inserción, actualización y eliminación.
 *
 * Propiedad AVL:
 *   factorBalance(nodo) = altura(izq) − altura(der)  ∈  {−1, 0, 1}
 *
 * Rotaciones implementadas:
 *   • LL (Izquierda-Izquierda)  → rotación simple a la derecha
 *   • RR (Derecha-Derecha)      → rotación simple a la izquierda
 *   • LR (Izquierda-Derecha)    → rotación doble: izquierda + derecha
 *   • RL (Derecha-Izquierda)    → rotación doble: derecha + izquierda
 *
 * Las claves se comparan lexicográficamente (String.compareTo), de modo
 * que los documentos quedan ordenados por id en el recorrido inorden.
 *
 * Paquete: gestorbd.estructura
 */
public class ArbolAVL {

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Raíz del árbol. Nula cuando el árbol está vacío. */
    private NodoAVL raiz;

    /** Número de documentos almacenados actualmente. */
    private int tamano;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /** Crea un árbol AVL vacío. */
    public ArbolAVL() {
        this.raiz   = null;
        this.tamano = 0;
    }

    // =========================================================================
    // SECCIÓN 1 – UTILIDADES INTERNAS DE ALTURA Y BALANCE
    // =========================================================================

    /**
     * Devuelve la altura de un nodo, tratando {@code null} como altura 0.
     */
    private int altura(NodoAVL nodo) {
        return (nodo == null) ? 0 : nodo.getAltura();
    }

    /**
     * Recalcula y actualiza la altura de un nodo a partir de sus hijos.
     * height(n) = 1 + max(height(left), height(right))
     */
    private void actualizarAltura(NodoAVL nodo) {
        nodo.setAltura(1 + Math.max(altura(nodo.getIzquierdo()),
                                    altura(nodo.getDerecho())));
    }

    /**
     * Calcula el factor de balance de un nodo.
     * Valor positivo → subárbol izquierdo más alto.
     * Valor negativo → subárbol derecho más alto.
     *
     * @param nodo Nodo a evaluar.
     * @return Factor de balance ∈ ℤ.
     */
    private int factorBalance(NodoAVL nodo) {
        return (nodo == null) ? 0
                : altura(nodo.getIzquierdo()) - altura(nodo.getDerecho());
    }

    // =========================================================================
    // SECCIÓN 2 – ROTACIONES
    // =========================================================================

    /**
     * Rotación simple a la <strong>derecha</strong> (caso LL).
     *
     * <pre>
     *       y                   x
     *      / \                 / \
     *     x   T3    →        T1   y
     *    / \                     / \
     *   T1  T2                 T2   T3
     * </pre>
     *
     * @param y Nodo desbalanceado (raíz del subárbol que rota).
     * @return Nueva raíz del subárbol (x).
     */
    private NodoAVL rotarDerecha(NodoAVL y) {
        NodoAVL x  = y.getIzquierdo();
        NodoAVL T2 = x.getDerecho();

        // Realizar rotación
        x.setDerecho(y);
        y.setIzquierdo(T2);

        // Actualizar alturas (primero y, luego x porque x es padre ahora)
        actualizarAltura(y);
        actualizarAltura(x);

        return x; // nueva raíz
    }

    /**
     * Rotación simple a la <strong>izquierda</strong> (caso RR).
     *
     * <pre>
     *     x                     y
     *    / \                   / \
     *   T1   y      →        x   T3
     *       / \             / \
     *      T2  T3          T1  T2
     * </pre>
     *
     * @param x Nodo desbalanceado (raíz del subárbol que rota).
     * @return Nueva raíz del subárbol (y).
     */
    private NodoAVL rotarIzquierda(NodoAVL x) {
        NodoAVL y  = x.getDerecho();
        NodoAVL T2 = y.getIzquierdo();

        // Realizar rotación
        y.setIzquierdo(x);
        x.setDerecho(T2);

        // Actualizar alturas
        actualizarAltura(x);
        actualizarAltura(y);

        return y; // nueva raíz
    }

    /**
     * Balancea el nodo dado si su factor de balance viola la propiedad AVL,
     * eligiendo y aplicando la rotación apropiada.
     *
     * Casos manejados:
     *  • LL: balance > 1 y el hijo izquierdo está balanceado o cargado a la izquierda → rotarDerecha
     *  • LR: balance > 1 y el hijo izquierdo está cargado a la derecha               → rotarIzquierda(izq) + rotarDerecha
     *  • RR: balance < -1 y el hijo derecho está balanceado o cargado a la derecha   → rotarIzquierda
     *  • RL: balance < -1 y el hijo derecho está cargado a la izquierda              → rotarDerecha(der) + rotarIzquierda
     *
     * @param nodo Nodo a balancear.
     * @return Raíz del subárbol ya balanceado.
     */
    private NodoAVL balancear(NodoAVL nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        // ── Caso LL ──────────────────────────────────────────────────────────
        if (balance > 1 && factorBalance(nodo.getIzquierdo()) >= 0) {
            return rotarDerecha(nodo);
        }

        // ── Caso LR ──────────────────────────────────────────────────────────
        if (balance > 1 && factorBalance(nodo.getIzquierdo()) < 0) {
            nodo.setIzquierdo(rotarIzquierda(nodo.getIzquierdo()));
            return rotarDerecha(nodo);
        }

        // ── Caso RR ──────────────────────────────────────────────────────────
        if (balance < -1 && factorBalance(nodo.getDerecho()) <= 0) {
            return rotarIzquierda(nodo);
        }

        // ── Caso RL ──────────────────────────────────────────────────────────
        if (balance < -1 && factorBalance(nodo.getDerecho()) > 0) {
            nodo.setDerecho(rotarDerecha(nodo.getDerecho()));
            return rotarIzquierda(nodo);
        }

        return nodo; // ya está balanceado
    }

    // =========================================================================
    // SECCIÓN 3 – INSERCIÓN  [O(log n)]
    // =========================================================================

    /**
     * Inserta un documento en el árbol.
     * Lanza excepción si ya existe un documento con el mismo id.
     *
     * @param documento Documento a insertar.
     * @throws IllegalArgumentException Si el documento o su id son nulos,
     *                                  o si ya existe un documento con ese id.
     */
    public void insertar(Documento documento) {
        if (documento == null) {
            throw new IllegalArgumentException("No se puede insertar un documento nulo.");
        }
        if (documento.getId() == null || documento.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El documento debe tener un id válido.");
        }
        raiz = insertarRecursivo(raiz, documento.getId(), documento);
        tamano++;
    }

    /**
     * Inserción recursiva con rebalanceo en el camino de vuelta.
     */
    private NodoAVL insertarRecursivo(NodoAVL nodo, String clave, Documento doc) {
        // Caso base: posición vacía → crear hoja
        if (nodo == null) {
            return new NodoAVL(clave, doc);
        }

        int cmp = clave.compareTo(nodo.getClave());

        if (cmp < 0) {
            nodo.setIzquierdo(insertarRecursivo(nodo.getIzquierdo(), clave, doc));
        } else if (cmp > 0) {
            nodo.setDerecho(insertarRecursivo(nodo.getDerecho(), clave, doc));
        } else {
            // clave ya existe en el árbol
            throw new IllegalArgumentException(
                    "Ya existe un documento con id='" + clave + "' en esta colección.");
        }

        return balancear(nodo);
    }

    // =========================================================================
    // SECCIÓN 4 – BÚSQUEDA POR ID  [O(log n)]
    // =========================================================================

    /**
     * Busca y devuelve el documento con el id especificado.
     *
     * @param id Identificador a buscar.
     * @return Documento encontrado, o {@code null} si no existe.
     */
    public Documento buscar(String id) {
        NodoAVL nodo = buscarNodo(raiz, id);
        return (nodo != null) ? nodo.getDocumento() : null;
    }

    /** Búsqueda binaria recursiva por clave. */
    private NodoAVL buscarNodo(NodoAVL nodo, String clave) {
        if (nodo == null) return null;

        int cmp = clave.compareTo(nodo.getClave());

        if (cmp == 0)  return nodo;
        if (cmp < 0)   return buscarNodo(nodo.getIzquierdo(), clave);
                       return buscarNodo(nodo.getDerecho(), clave);
    }

    // =========================================================================
    // SECCIÓN 5 – ACTUALIZACIÓN  [O(log n)]
    // =========================================================================

    /**
     * Actualiza el documento asociado al id dado.
     * La clave (id) permanece inmutable en el árbol; sólo se reemplaza el documento.
     *
     * @param id                   Identificador del documento a actualizar.
     * @param documentoActualizado Nuevo documento (su id será forzado al id dado).
     * @return {@code true} si se actualizó, {@code false} si no se encontró el id.
     */
    public boolean actualizar(String id, Documento documentoActualizado) {
        NodoAVL nodo = buscarNodo(raiz, id);
        if (nodo == null) return false;

        documentoActualizado.setId(id); // garantizar consistencia de clave
        nodo.setDocumento(documentoActualizado);
        return true;
    }

    // =========================================================================
    // SECCIÓN 6 – ELIMINACIÓN  [O(log n)]
    // =========================================================================

    /**
     * Elimina el documento con el id especificado del árbol.
     *
     * @param id Identificador del documento a eliminar.
     * @return {@code true} si se eliminó, {@code false} si no se encontró.
     */
    public boolean eliminar(String id) {
        if (buscarNodo(raiz, id) == null) return false;
        raiz = eliminarRecursivo(raiz, id);
        tamano--;
        return true;
    }

    /**
     * Eliminación recursiva con rebalanceo en el camino de vuelta.
     *
     * Casos:
     *  1. Nodo sin hijos         → retornar null (eliminar directamente).
     *  2. Nodo con un solo hijo  → retornar ese hijo.
     *  3. Nodo con dos hijos     → reemplazar con el sucesor inorden
     *                              (mínimo del subárbol derecho).
     */
    private NodoAVL eliminarRecursivo(NodoAVL nodo, String clave) {
        if (nodo == null) return null;

        int cmp = clave.compareTo(nodo.getClave());

        if (cmp < 0) {
            nodo.setIzquierdo(eliminarRecursivo(nodo.getIzquierdo(), clave));
        } else if (cmp > 0) {
            nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), clave));
        } else {
            // ── Nodo encontrado ───────────────────────────────────────────
            if (nodo.getIzquierdo() == null) {
                // Caso 1 o 2: sin hijo izquierdo
                return nodo.getDerecho(); // puede ser null o un hijo derecho
            } else if (nodo.getDerecho() == null) {
                // Caso 2: sólo hijo izquierdo
                return nodo.getIzquierdo();
            } else {
                // Caso 3: dos hijos → hallar sucesor inorden
                NodoAVL sucesor = obtenerMinimo(nodo.getDerecho());

                // Copiar datos del sucesor al nodo actual
                nodo.setClave(sucesor.getClave());
                nodo.setDocumento(sucesor.getDocumento());

                // Eliminar el sucesor del subárbol derecho
                nodo.setDerecho(eliminarRecursivo(nodo.getDerecho(), sucesor.getClave()));
            }
        }

        return balancear(nodo);
    }

    /**
     * Devuelve el nodo con la clave mínima en el subárbol dado
     * (el nodo más a la izquierda posible).
     */
    private NodoAVL obtenerMinimo(NodoAVL nodo) {
        NodoAVL actual = nodo;
        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }
        return actual;
    }

    // =========================================================================
    // SECCIÓN 7 – BÚSQUEDA POR CRITERIO  [O(n)]
    // =========================================================================

    /**
     * Busca todos los documentos cuyo campo {@code campo} contenga el valor
     * {@code valor} (comparación insensible a mayúsculas).
     *
     * Al no existir índice sobre campos arbitrarios, esta operación recorre
     * todo el árbol en O(n).
     *
     * @param campo Nombre del campo a filtrar.
     * @param valor Valor (o subcadena) a buscar.
     * @return Lista de documentos que cumplen el criterio.
     */
    public List<Documento> buscarPorCriterio(String campo, String valor) {
        List<Documento> resultados = new ArrayList<>();
        buscarCriterioRecursivo(raiz, campo, valor.toLowerCase(), resultados);
        return resultados;
    }

    private void buscarCriterioRecursivo(NodoAVL nodo, String campo, String valorLower,
                                         List<Documento> resultados) {
        if (nodo == null) return;

        Object valorCampo = nodo.getDocumento().getCampo(campo);
        if (valorCampo != null
                && valorCampo.toString().toLowerCase().contains(valorLower)) {
            resultados.add(nodo.getDocumento());
        }

        buscarCriterioRecursivo(nodo.getIzquierdo(), campo, valorLower, resultados);
        buscarCriterioRecursivo(nodo.getDerecho(),    campo, valorLower, resultados);
    }

    // =========================================================================
    // SECCIÓN 8 – RECORRIDOS
    // =========================================================================

    /**
     * Devuelve todos los documentos ordenados ascendentemente por id
     * mediante recorrido inorden (izquierdo → raíz → derecho).
     *
     * @return Lista ordenada de documentos.
     */
    public List<Documento> inorden() {
        List<Documento> lista = new ArrayList<>();
        inordenRecursivo(raiz, lista);
        return lista;
    }

    private void inordenRecursivo(NodoAVL nodo, List<Documento> lista) {
        if (nodo == null) return;
        inordenRecursivo(nodo.getIzquierdo(), lista);
        lista.add(nodo.getDocumento());
        inordenRecursivo(nodo.getDerecho(), lista);
    }

    // =========================================================================
    // SECCIÓN 9 – UTILIDADES PÚBLICAS
    // =========================================================================

    /** @return Número de documentos almacenados. */
    public int getTamano() {
        return tamano;
    }

    /** @return {@code true} si el árbol no contiene ningún documento. */
    public boolean estaVacio() {
        return raiz == null;
    }

    /** Elimina todos los nodos del árbol (no persiste el cambio). */
    public void limpiar() {
        raiz   = null;
        tamano = 0;
    }

    /**
     * Imprime la estructura visual del árbol en consola (útil para debugging).
     * Muestra cada nodo con su clave, altura y factor de balance.
     */
    public void imprimirEstructura() {
        if (raiz == null) {
            System.out.println("  [árbol vacío]");
            return;
        }
        imprimirRecursivo(raiz, "", true);
    }

    private void imprimirRecursivo(NodoAVL nodo, String prefijo, boolean esUltimo) {
        if (nodo == null) return;

        System.out.println(prefijo
                + (esUltimo ? "└── " : "├── ")
                + nodo.getClave()
                + "  [h=" + nodo.getAltura()
                + ", fb=" + factorBalance(nodo) + "]");

        String nuevoPrefijo = prefijo + (esUltimo ? "    " : "│   ");

        // Imprimir derecho primero para que visualmente quede "arriba"
        if (nodo.getDerecho() != null || nodo.getIzquierdo() != null) {
            imprimirRecursivo(nodo.getDerecho(),   nuevoPrefijo, nodo.getIzquierdo() == null);
            imprimirRecursivo(nodo.getIzquierdo(), nuevoPrefijo, true);
        }
    }

    /**
     * Verifica que el árbol cumple la propiedad AVL en todos sus nodos.
     * Útil para pruebas unitarias.
     *
     * @return {@code true} si el árbol es un AVL válido.
     */
    public boolean esValido() {
        return esValidoRecursivo(raiz);
    }

    private boolean esValidoRecursivo(NodoAVL nodo) {
        if (nodo == null) return true;
        int fb = factorBalance(nodo);
        if (fb < -1 || fb > 1) return false;
        return esValidoRecursivo(nodo.getIzquierdo())
            && esValidoRecursivo(nodo.getDerecho());
    }
}