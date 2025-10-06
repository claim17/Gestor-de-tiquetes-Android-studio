package es.umh.dadm.mistickets20519201g.objetos;

import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import es.umh.dadm.mistickets20519201g.database.DatabaseHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Ticket {
    private int id;
    private String fotoPath;
    private byte[] imagenBlob; // Campo para datos BLOB
    private Categoria categoria;
    private double precio;
    private Date fechaAlta;
    private String descripcionCorta;
    private String descripcionLarga;
    private String ubicacionFoto;
    
    public static ArrayList<Ticket> arrayTickets = new ArrayList<>();
    
    public Ticket() {
        this.fechaAlta = new Date();
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getFotoPath() { return fotoPath; }
    public void setFotoPath(String fotoPath) { this.fotoPath = fotoPath; }
    
    // Asegúrate de que estos métodos estén correctamente implementados
    public byte[] getImagenBlob() { return imagenBlob; }
    public void setImagenBlob(byte[] imagenBlob) { this.imagenBlob = imagenBlob; }
    
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    
    public Date getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(Date fechaAlta) { this.fechaAlta = fechaAlta; }
    
    public String getDescripcionCorta() { return descripcionCorta; }
    public void setDescripcionCorta(String descripcionCorta) { this.descripcionCorta = descripcionCorta; }
    
    public String getDescripcionLarga() { return descripcionLarga; }
    public void setDescripcionLarga(String descripcionLarga) { this.descripcionLarga = descripcionLarga; }
    
    public String getUbicacionFoto() { return ubicacionFoto; }
    public void setUbicacionFoto(String ubicacionFoto) { this.ubicacionFoto = ubicacionFoto; }
    
    // Métodos para la base de datos
    public static void addTicket(Ticket ticket, Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        long id = db.insertTicket(ticket);
        ticket.setId((int) id);
        arrayTickets.add(ticket);
    }
    
    public static void updateTicket(Ticket ticket, Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        db.updateTicket(ticket);
        
        for (int i = 0; i < arrayTickets.size(); i++) {
            if (arrayTickets.get(i).getId() == ticket.getId()) {
                arrayTickets.set(i, ticket);
                break;
            }
        }
    }
    
    public static void deleteTicket(int id, Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        db.deleteTicket(id);
        
        for (int i = 0; i < arrayTickets.size(); i++) {
            if (arrayTickets.get(i).getId() == id) {
                arrayTickets.remove(i);
                break;
            }
        }
    }

    public static void deleteTicketsByCategoria(int categoriaId, Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        db.deleteTicketsByCategoria(categoriaId);
        
        // Eliminar de la lista local
        arrayTickets.removeIf(ticket -> ticket.getCategoria() != null && ticket.getCategoria().getId() == categoriaId);
    }
    
    public static void loadTickets(Context context) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);
        arrayTickets = db.getAllTickets();
    }
    
    // Helper method to convert URI to byte array
    public static byte[] uriToByteArray(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream inputStream = contentResolver.openInputStream(uri);
            if (inputStream != null) {
                // Decodificar el bitmap original
                Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();

                if (originalBitmap != null) {
                    // Calcular el tamaño máximo permitido (2MB)
                    int maxSize = 2 * 1024 * 1024;
                    int quality = 100;
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    
                    // Comprimir la imagen hasta que sea menor que maxSize
                    do {
                        outputStream.reset();
                        originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                        quality -= 10;
                    } while (outputStream.size() > maxSize && quality > 0);

                    // Si la calidad llegó a 0 y sigue siendo muy grande, redimensionar
                    if (outputStream.size() > maxSize) {
                        int width = originalBitmap.getWidth();
                        int height = originalBitmap.getHeight();
                        float scale = (float) Math.sqrt((double) maxSize / (double) outputStream.size());
                        
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            (int) (width * scale),
                            (int) (height * scale),
                            true
                        );
                        
                        outputStream.reset();
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
                        scaledBitmap.recycle();
                    }
                    
                    originalBitmap.recycle();
                    return outputStream.toByteArray();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // ... existing methods ...
}