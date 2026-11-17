package cl.iglesia.gestion

import io.github.jan.supabase.postgrest.query.Columns

/**
 * Columnas que la app pide a cada tabla, igual que la web (CMAA): nunca `*`.
 *
 * Postgres rechaza con 42501 (permission denied) cualquier consulta que incluya una columna sin
 * permiso para el rol `authenticated`. Pedir `*` falla apenas una tabla tiene permisos por columnas
 * (`usuario` y `proyecto` ya los tienen en staging), mientras que pedir solo lo necesario funciona
 * igual en ambos casos. Cada lista corresponde a los campos de su clase `*Row` en
 * [SupabaseBackend]: si se agrega un campo a la clase, se agrega aquí.
 *
 * Referencia de la web: los archivos `*-service.ts` de `src/app/core/services` del repo CMAA.
 */
internal object DataColumns {
    /** Para escrituras que devuelven la fila (`.select(...)` tras update/delete): basta el id. */
    val ID = Columns.list("id")

    /**
     * `usuario`: staging solo da permiso sobre estas columnas. El correo sale de la sesión
     * (auth) y el teléfono no se puede leer por esta vía.
     */
    val USUARIO = Columns.list("id", "nombres", "apellidos", "rol", "estado")

    val MINISTERIO = Columns.list("id", "nombre", "descripcion", "estado", "es_alabanza")

    val MIEMBRO_MINISTERIO = Columns.list("ministerio_id", "usuario_id", "es_lider")

    val ACTIVIDAD = Columns.list(
        "id", "titulo", "tipo", "clasificacion", "proyecto_id", "ministerio_id",
        "descripcion", "inicio", "fin", "lugar", "estado",
    )

    val ACTIVIDAD_RESPONSABLE = Columns.list("actividad_id", "usuario_id")

    val DETALLE_CULTO = Columns.list("actividad_id", "predicador_id", "director_alabanza_id")

    val CONVOCATORIA = Columns.list("actividad_id", "alcance", "ministerio_id", "usuario_id")

    val TAREA = Columns.list(
        "id", "actividad_id", "titulo", "instrucciones", "responsable_id",
        "fecha_limite", "prioridad", "estado",
    )

    val COMENTARIO_TAREA = Columns.list("id", "tarea_id", "autor_id", "texto", "created_at")

    /** `proyecto`: el presupuesto queda fuera a propósito (migración SCRUM-155 de CMAA). */
    val PROYECTO = Columns.list(
        "id", "nombre", "objetivo", "descripcion", "responsable_id", "fecha_inicio",
        "fecha_cierre_estimada", "estado", "created_by", "created_at",
    )

    val CANCION = Columns.list("id", "titulo", "artista", "tonalidad", "enlace_version")

    val REPERTORIO = Columns.list("id", "updated_at")

    val REPERTORIO_CANCION = Columns.list(
        "repertorio_id", "cancion_id", "orden", "tonalidad", "enlace_version", "director_id",
    )

    val NOTIFICACION = Columns.list(
        "id", "tipo", "mensaje", "entidad_tipo", "entidad_id", "leida", "created_at",
    )

    val ARCHIVO = Columns.list(
        "id", "nombre", "ruta", "tipo", "tamano_bytes", "cancion_id", "reunion_id",
    )

    val REUNION = Columns.list("id", "actividad_id", "nombre", "fecha", "resumen")

    val VISIBILIDAD_REUNION = Columns.list("reunion_id", "alcance", "ministerio_id", "usuario_id")
}
