package com.gestorbd.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para serializar y deserializar JSON <strong>sin librerías externas</strong>.
 *
 * Funcionalidades:
 *  • Serialización de {@code Map<String,Object>} a cadena JSON.
 *  • Deserialización de cadena JSON a {@code Map<String,Object>}.
 *  • Deserialización de arreglo JSON a {@code List<Map<String,Object>>}.
 *  • Formato legible con indentación.
 *
 * Tipos de valores JSON soportados:
 *  {@code String}, {@code Integer}, {@code Long}, {@code Double},
 *  {@code Boolean}, {@code null}, objetos anidados y arreglos.
 *
 * El parser utiliza un analizador de descenso recursivo con un índice
 * de posición mutable encapsulado en la clase interna {@link Parser}.
 *
 * Paquete: gestorbd.util
 */
public final class JsonUtil {

    /** Constructor privado: clase utilitaria, no instanciable. */
    private JsonUtil() {}

    // =========================================================================
    // SECCIÓN 1 – SERIALIZACIÓN (Objeto → JSON)
    // =========================================================================

    /**
     * Serializa un {@link Map} a una cadena JSON compacta (sin espacios).
     *
     * @param mapa Mapa a serializar.
     * @return Cadena JSON.
     */
    public static String serializar(Map<String, Object> mapa) {
        if (mapa == null) return "null";
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<String, Object> entrada : mapa.entrySet()) {
            if (!primero) sb.append(',');
            primero = false;
            sb.append('"').append(escapar(entrada.getKey())).append('"');
            sb.append(':');
            sb.append(serializarValor(entrada.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    /**
     * Serializa un {@link Map} a JSON con indentación (formato legible).
     *
     * @param mapa   Mapa a serializar.
     * @param indent Número de espacios de indentación por nivel.
     * @return Cadena JSON formateada.
     */
    public static String serializar(Map<String, Object> mapa, int indent) {
        return serializarConIndent(mapa, indent, 0);
    }

    @SuppressWarnings("unchecked")
    private static String serializarValor(Object valor) {
        if (valor == null)           return "null";
        if (valor instanceof Boolean) return valor.toString();
        if (valor instanceof Number)  return valor.toString();
        if (valor instanceof String)  return '"' + escapar((String) valor) + '"';
        if (valor instanceof Map)     return serializar((Map<String, Object>) valor);
        if (valor instanceof List) {
            List<?> lista = (List<?>) valor;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(serializarValor(lista.get(i)));
            }
            sb.append(']');
            return sb.toString();
        }
        // Tipo desconocido → tratar como String
        return '"' + escapar(valor.toString()) + '"';
    }

    @SuppressWarnings("unchecked")
    private static String serializarConIndent(Object valor, int indent, int nivel) {
        String pad  = " ".repeat(indent * nivel);
        String pad1 = " ".repeat(indent * (nivel + 1));

        if (valor == null)            return "null";
        if (valor instanceof Boolean) return valor.toString();
        if (valor instanceof Number)  return valor.toString();
        if (valor instanceof String)  return '"' + escapar((String) valor) + '"';

        if (valor instanceof Map) {
            Map<String, Object> mapa = (Map<String, Object>) valor;
            if (mapa.isEmpty()) return "{}";
            StringBuilder sb = new StringBuilder("{\n");
            boolean primero = true;
            for (Map.Entry<String, Object> e : mapa.entrySet()) {
                if (!primero) sb.append(",\n");
                primero = false;
                sb.append(pad1)
                  .append('"').append(escapar(e.getKey())).append("\": ")
                  .append(serializarConIndent(e.getValue(), indent, nivel + 1));
            }
            sb.append('\n').append(pad).append('}');
            return sb.toString();
        }

        if (valor instanceof List) {
            List<?> lista = (List<?>) valor;
            if (lista.isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[\n");
            for (int i = 0; i < lista.size(); i++) {
                if (i > 0) sb.append(",\n");
                sb.append(pad1).append(serializarConIndent(lista.get(i), indent, nivel + 1));
            }
            sb.append('\n').append(pad).append(']');
            return sb.toString();
        }

        return '"' + escapar(valor.toString()) + '"';
    }

    /**
     * Escapa caracteres especiales para representación JSON válida.
     */
    private static String escapar(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    // =========================================================================
    // SECCIÓN 2 – DESERIALIZACIÓN (JSON → Objeto)
    // =========================================================================

    /**
     * Deserializa una cadena JSON que representa un objeto ({...})
     * a un {@code Map<String,Object>}.
     *
     * @param jsonStr Cadena JSON.
     * @return Mapa resultante.
     * @throws IllegalArgumentException Si el JSON es inválido.
     */
    public static Map<String, Object> deserializar(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        Parser parser = new Parser(jsonStr.trim());
        parser.omitirEspacios();
        if (parser.fin() || parser.actual() != '{') {
            throw new IllegalArgumentException(
                    "Se esperaba un objeto JSON '{...}', pero se encontró: " + jsonStr);
        }
        return parser.parsearObjeto();
    }

    /**
     * Deserializa una cadena JSON que representa un arreglo ([...])
     * a una lista de mapas.
     *
     * @param jsonStr Cadena JSON de arreglo.
     * @return Lista de mapas.
     * @throws IllegalArgumentException Si el JSON es inválido.
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> deserializarArreglo(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Parser parser = new Parser(jsonStr.trim());
        parser.omitirEspacios();
        if (parser.fin() || parser.actual() != '[') {
            throw new IllegalArgumentException(
                    "Se esperaba un arreglo JSON '[...]', pero se encontró: " + jsonStr);
        }
        List<Object> lista = parser.parsearArreglo();
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Object o : lista) {
            if (o instanceof Map) {
                resultado.add((Map<String, Object>) o);
            }
        }
        return resultado;
    }

    // =========================================================================
    // SECCIÓN 3 – PARSER DE DESCENSO RECURSIVO (clase interna)
    // =========================================================================

    /**
     * Analizador sintáctico JSON de descenso recursivo.
     *
     * Gramática soportada:
     * <pre>
     *   valor   → objeto | arreglo | cadena | número | "true" | "false" | "null"
     *   objeto  → '{' (cadena ':' valor (',' cadena ':' valor)*)? '}'
     *   arreglo → '[' (valor (',' valor)*)? ']'
     *   cadena  → '"' ... '"'
     *   número  → [-] [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)?
     * </pre>
     */
    private static final class Parser {

        private final String texto;
        private int pos;

        Parser(String texto) {
            this.texto = texto;
            this.pos   = 0;
        }

        // ── Utilidades de posición ────────────────────────────────────────

        boolean fin() {
            return pos >= texto.length();
        }

        char actual() {
            return texto.charAt(pos);
        }

        void omitirEspacios() {
            while (!fin() && Character.isWhitespace(actual())) {
                pos++;
            }
        }

        void consumir(char esperado) {
            if (fin() || actual() != esperado) {
                throw new IllegalArgumentException(
                        "Se esperaba '" + esperado + "' en posición " + pos
                        + " pero se encontró '" + (fin() ? "EOF" : actual()) + "'.");
            }
            pos++;
        }

        // ── Parseo de objeto ──────────────────────────────────────────────

        Map<String, Object> parsearObjeto() {
            Map<String, Object> mapa = new LinkedHashMap<>();
            consumir('{');
            omitirEspacios();

            if (!fin() && actual() == '}') {
                pos++; // objeto vacío
                return mapa;
            }

            while (!fin()) {
                omitirEspacios();
                String clave = parsearCadena();
                omitirEspacios();
                consumir(':');
                omitirEspacios();
                Object valor = parsearValor();
                mapa.put(clave, valor);

                omitirEspacios();
                if (!fin() && actual() == ',') {
                    pos++; // consumir coma y continuar
                } else {
                    break;
                }
            }

            omitirEspacios();
            consumir('}');
            return mapa;
        }

        // ── Parseo de arreglo ─────────────────────────────────────────────

        List<Object> parsearArreglo() {
            List<Object> lista = new ArrayList<>();
            consumir('[');
            omitirEspacios();

            if (!fin() && actual() == ']') {
                pos++; // arreglo vacío
                return lista;
            }

            while (!fin()) {
                omitirEspacios();
                lista.add(parsearValor());
                omitirEspacios();
                if (!fin() && actual() == ',') {
                    pos++;
                } else {
                    break;
                }
            }

            omitirEspacios();
            consumir(']');
            return lista;
        }

        // ── Parseo de valor (dispatcher) ──────────────────────────────────

        Object parsearValor() {
            omitirEspacios();
            if (fin()) return null;

            char c = actual();

            if (c == '"') return parsearCadena();
            if (c == '{') return parsearObjeto();
            if (c == '[') return parsearArreglo();
            if (c == 't') { verificarLiteral("true");  return Boolean.TRUE;  }
            if (c == 'f') { verificarLiteral("false"); return Boolean.FALSE; }
            if (c == 'n') { verificarLiteral("null");  return null;          }
            if (c == '-' || Character.isDigit(c)) return parsearNumero();

            throw new IllegalArgumentException(
                    "Carácter inesperado '" + c + "' en posición " + pos);
        }

        /** Verifica y consume un literal JSON (true, false, null). */
        private void verificarLiteral(String literal) {
            if (pos + literal.length() > texto.length()) {
                throw new IllegalArgumentException(
                        "Se esperaba literal '" + literal + "' en posición " + pos);
            }
            String subcadena = texto.substring(pos, pos + literal.length());
            if (!subcadena.equals(literal)) {
                throw new IllegalArgumentException(
                        "Literal inválido '" + subcadena + "' en posición " + pos);
            }
            pos += literal.length();
        }

        // ── Parseo de cadena ──────────────────────────────────────────────

        String parsearCadena() {
            consumir('"');
            StringBuilder sb = new StringBuilder();

            while (!fin()) {
                char c = actual();

                if (c == '"') {
                    pos++; // fin de cadena
                    return sb.toString();
                }

                if (c == '\\') {
                    pos++; // consumir backslash
                    if (fin()) break;
                    char esc = actual();
                    pos++;
                    switch (esc) {
                        case '"':  sb.append('"');  break;
                        case '\\': sb.append('\\'); break;
                        case '/':  sb.append('/');  break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        case 'b':  sb.append('\b'); break;
                        case 'f':  sb.append('\f'); break;
                        case 'u':
                            if (pos + 4 <= texto.length()) {
                                String hex = texto.substring(pos, pos + 4);
                                try {
                                    sb.append((char) Integer.parseInt(hex, 16));
                                    pos += 4;
                                } catch (NumberFormatException e) {
                                    sb.append("\\u").append(hex);
                                    pos += 4;
                                }
                            }
                            break;
                        default:
                            sb.append(esc);
                    }
                } else {
                    sb.append(c);
                    pos++;
                }
            }

            throw new IllegalArgumentException(
                    "Cadena JSON no cerrada. Posición: " + pos);
        }

        // ── Parseo de número ──────────────────────────────────────────────

        Number parsearNumero() {
            int inicio = pos;
            boolean esDecimal = false;

            if (!fin() && actual() == '-') pos++;

            while (!fin() && Character.isDigit(actual())) pos++;

            if (!fin() && actual() == '.') {
                esDecimal = true;
                pos++;
                while (!fin() && Character.isDigit(actual())) pos++;
            }

            if (!fin() && (actual() == 'e' || actual() == 'E')) {
                esDecimal = true;
                pos++;
                if (!fin() && (actual() == '+' || actual() == '-')) pos++;
                while (!fin() && Character.isDigit(actual())) pos++;
            }

            String numStr = texto.substring(inicio, pos);
            try {
                if (esDecimal) {
                    return Double.parseDouble(numStr);
                }
                long val = Long.parseLong(numStr);
                // Devolver int si cabe, long en caso contrario
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        "Número JSON inválido: '" + numStr + "'");
            }
        }
    }
}