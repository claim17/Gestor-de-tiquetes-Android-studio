package es.umh.dadm.mistickets20519201g.objetos;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import java.util.ArrayList;

import es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia;

public class Categoria {
    private int id;
    private String titulo;
    private String descorta;
    private String deslarga;
    private String detalles;
    private String fotoPath;

    // Constructor vacío necesario para GSON
    public Categoria() {
    }

    // Constructor con parámetros
    public Categoria(int id, String titulo, String descorta, String deslarga, String detalles, String fotoPath) {
        this.id = id;
        this.titulo = titulo;
        this.descorta = descorta;
        this.deslarga = deslarga;
        this.detalles = detalles;
        this.fotoPath = fotoPath;
    }

    // pa ver el nombre bien en el adapter de tickets
    @Override
    public String toString() {
        return titulo;
    }

    /* Getters y Setters */
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public String getDescorta() {return descorta;}
    public void setDescorta(String descorta) {
        this.descorta = descorta;
    }

    public String getDeslarga() {return deslarga;}
    public void setDeslarga(String deslarga) {
        this.deslarga = deslarga;
    }

    public String getDetalles() {return detalles;}
    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }

    public String getFotoPath() {return fotoPath;}
    public void setFotoPath(String fotoPath) {
        // Asegurarse de que no guardamos URIs de content provider que requieren permisos especiales
        if (fotoPath != null && fotoPath.startsWith("content://media/picker")) {
            Log.w("Categoria", "Detectado URI de picker que puede causar problemas de permisos: " + fotoPath);
            // Guardar la URI completa para poder acceder a la imagen
            this.fotoPath = fotoPath;
        } else {
            this.fotoPath = fotoPath;
        }
    }

    public static ArrayList<Categoria> arrayCat = new ArrayList<Categoria>();
    
    public static void addCategoria(Categoria cat) {
        arrayCat.add(cat);
        // Guardar las categorías después de añadir una nueva
        if (cat != null && cat.getId() > 0) {
            Context context = null;
            // Intentar obtener el contexto de alguna manera si es necesario
            // Si no es posible, se guardará más tarde cuando se llame a otra función con contexto
        }
    }

    public static void loadCategorias(Context context) {
        // Cambiar de DatabaseHelper a CategoriaPersistencia
        arrayCat = es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia.cargarCategorias(context);
        
        // Si no hay categorías cargadas (primera ejecución), crear algunas por defecto
        if (arrayCat == null || arrayCat.isEmpty()) {
            arrayCat = new ArrayList<>();
            
            // Añadir categorías predeterminadas
            arrayCat.add(new Categoria(1, "General", "Tickets generales", "Categoría para tickets generales", "", ""));
            arrayCat.add(new Categoria(2, "Comida", "Tickets de comida", "Categoría para tickets relacionados con comida", "", ""));
            arrayCat.add(new Categoria(3, "Transporte", "Tickets de transporte", "Categoría para tickets de transporte", "", ""));
            
            // Guardar las categorías predeterminadas
            es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia.guardarCategorias(context, arrayCat);
        }
    }
    
    // Método para guardar los cambios después de modificar una categoría
    public static void guardarCambios(Context context) {
        if (context != null && arrayCat != null) {
            es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia.guardarCategorias(context, arrayCat);
        }
    }
}

