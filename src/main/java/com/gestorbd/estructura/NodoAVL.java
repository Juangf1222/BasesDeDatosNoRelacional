package com.gestorbd.estructura;

import com.gestorbd.modelo.Documento;

/**
 * Nodo del Árbol AVL (Adelson-Velsky and Landis).
 *
 * Almacena:
 *  - La clave de búsqueda (id del documento).
 *  - El documento asociado a esa clave.
 *  - Referencias a los subárboles izquierdo y derecho.
 *  - La altura del nodo (usada para calcular el factor de balance).
 *
 * La propiedad AVL garantiza que, para cada nodo, la diferencia entre
 * las alturas de sus subárboles (factor de balance) está en {-1, 0, 1}.
 *
 * Paquete: gestorbd.estructura
 */
public class NodoAVL {

    // ─── Atributos ────────────────────────────────────────────────────────────

    /** Clave de indexación; corresponde al id del documento. */
    private String clave;

    /** Documento almacenado en este nodo. */
    private Documento documento;

    /** Subárbol izquierdo (claves menores que {@code clave}). */
    private NodoAVL izquierdo;

    /** Subárbol derecho (claves mayores que {@code clave}). */
    private NodoAVL derecho;

    /**
     * Altura del nodo en el árbol.
     * Un nodo hoja tiene altura 1. Un nodo nulo tiene altura 0.
     */
    private int altura;

    // ─── Constructor ─────────────────────────────────────────────────────────

    /**
     * Crea un nuevo nodo hoja con la clave y el documento dados.
     * Los hijos se inicializan en {@code null} y la altura en 1.
     *
     * @param clave     Identificador (clave de indexación).
     * @param documento Documento a almacenar.
     */
    public NodoAVL(String clave, Documento documento) {
        this.clave      = clave;
        this.documento  = documento;
        this.izquierdo  = null;
        this.derecho    = null;
        this.altura     = 1;
    }

    // ─── Getters y Setters ────────────────────────────────────────────────────

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public Documento getDocumento() {
        return documento;
    }

    public void setDocumento(Documento documento) {
        this.documento = documento;
    }

    public NodoAVL getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoAVL izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoAVL getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoAVL derecho) {
        this.derecho = derecho;
    }

    public int getAltura() {
        return altura;
    }

    public void setAltura(int altura) {
        this.altura = altura;
    }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "NodoAVL{ clave='" + clave + "', altura=" + altura + " }";
    }
}