package es.umh.dadm.mistickets20519201g.persistencia;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.content.ContentResolver;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;

import es.umh.dadm.mistickets20519201g.objetos.Categoria;

public class CategoriaPersistencia {
    private static final String FILENAME = "categorias.json";
    private static final String IMAGES_DIR = "category_images";
    private static final Gson gson = new Gson();

    public static String guardarImagen(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            Log.e("CategoriaPersistencia", "Contexto o URI nulos");
            return null;
        }

        File imagesDir = new File(context.getFilesDir(), IMAGES_DIR);
        if (!imagesDir.exists() && !imagesDir.mkdirs()) {
            Log.e("CategoriaPersistencia", "No se pudo crear el directorio de imágenes");
            return null;
        }

        String imageName = "img_" + System.currentTimeMillis() + ".jpg";
        File imageFile = new File(imagesDir, imageName);

        try (InputStream in = context.getContentResolver().openInputStream(imageUri);
             FileOutputStream out = new FileOutputStream(imageFile)) {
            
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e("CategoriaPersistencia", "Error al guardar la imagen: " + e.getMessage());
            return null;
        }
    }

    public static Uri cargarImagen(Context context, String imagePath) {
        if (context == null || imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        File imageFile = new File(imagePath);
        return imageFile.exists() ? Uri.fromFile(imageFile) : null;
    }

    public static void guardarCategorias(Context context, ArrayList<Categoria> categorias) {
        if (context == null || categorias == null) {
            Log.e("CategoriaPersistencia", "Contexto o lista de categorías nulos");
            return;
        }
        
        File directory = context.getFilesDir();
        if (!directory.exists() && !directory.mkdirs()) {
            Log.e("CategoriaPersistencia", "No se pudo crear el directorio");
            return;
        }
        
        File tempFile = new File(directory, FILENAME + ".tmp");
        File finalFile = new File(directory, FILENAME);
        
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(gson.toJson(categorias).getBytes());
            
            if (finalFile.exists()) {
                finalFile.delete();
            }
            
            if (!tempFile.renameTo(finalFile)) {
                Log.e("CategoriaPersistencia", "Error al renombrar el archivo temporal");
                tempFile.delete();
            }
            
            Log.d("CategoriaPersistencia", "Categorías guardadas: " + categorias.size());
        } catch (IOException e) {
            Log.e("CategoriaPersistencia", "Error al guardar categorías: " + e.getMessage());
        }
    }

    public static ArrayList<Categoria> cargarCategorias(Context context) {
        if (context == null) {
            Log.e("CategoriaPersistencia", "Contexto nulo");
            return new ArrayList<>();
        }
        
        File file = new File(context.getFilesDir(), FILENAME);
        if (!file.exists()) {
            Log.d("CategoriaPersistencia", "El archivo no existe, devolviendo lista vacía");
            return new ArrayList<>();
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            
            Type type = new TypeToken<ArrayList<Categoria>>(){}.getType();
            ArrayList<Categoria> categorias = gson.fromJson(sb.toString(), type);
            
            if (categorias == null) {
                Log.e("CategoriaPersistencia", "Error al deserializar el JSON");
                return new ArrayList<>();
            }
            
            Log.d("CategoriaPersistencia", "Categorías cargadas: " + categorias.size());
            return categorias;
        } catch (Exception e) {
            Log.e("CategoriaPersistencia", "Error al cargar categorías: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}