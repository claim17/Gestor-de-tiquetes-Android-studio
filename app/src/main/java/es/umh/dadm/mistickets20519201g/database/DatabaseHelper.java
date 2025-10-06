package es.umh.dadm.mistickets20519201g.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;

import es.umh.dadm.mistickets20519201g.objetos.Categoria;
import es.umh.dadm.mistickets20519201g.objetos.Ticket;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BDTickets.db";
    // Update the database version to trigger schema update
    private static final int DATABASE_VERSION = 3;
    
    public static final String TABLE_TICKETS = "Tickets";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOTO = "foto";
    public static final String COLUMN_IMAGEN_BLOB = "imagen_blob"; // New column for BLOB
    public static final String COLUMN_CATEGORIA = "categoria";
    public static final String COLUMN_PRECIO = "precio";
    public static final String COLUMN_FECHA = "fecha";
    public static final String COLUMN_DESC_CORTA = "desCorta";
    public static final String COLUMN_DESC_LARGA = "descLarga";
    public static final String COLUMN_LOCALIZACION = "localiz";
    
    private static final String SQL_CREATE_TICKETS =
            "CREATE TABLE " + TABLE_TICKETS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FOTO + " TEXT, " +
                    COLUMN_IMAGEN_BLOB + " BLOB, " + // Add BLOB column
                    COLUMN_CATEGORIA + " INTEGER NOT NULL, " +
                    COLUMN_PRECIO + " REAL NOT NULL, " +
                    COLUMN_FECHA + " INTEGER NOT NULL, " +
                    COLUMN_DESC_CORTA + " TEXT NOT NULL, " +
                    COLUMN_DESC_LARGA + " TEXT NOT NULL, " +
                    COLUMN_LOCALIZACION + " TEXT)";
    
    private static DatabaseHelper instance;
    
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }
    
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla de tickets
        String CREATE_TICKETS_TABLE = "CREATE TABLE " + TABLE_TICKETS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FOTO + " TEXT,"
                + COLUMN_IMAGEN_BLOB + " BLOB,"
                + COLUMN_CATEGORIA + " INTEGER,"
                + COLUMN_PRECIO + " REAL,"
                + COLUMN_FECHA + " INTEGER,"
                + COLUMN_DESC_CORTA + " TEXT,"
                + COLUMN_DESC_LARGA + " TEXT,"
                + COLUMN_LOCALIZACION + " TEXT"
                + ")";
        db.execSQL(CREATE_TICKETS_TABLE);
        
        // Crear tabla de categorías
        String CREATE_CATEGORIAS_TABLE = "CREATE TABLE categorias ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "titulo TEXT,"
                + "descorta TEXT,"
                + "deslarga TEXT,"
                + "detalles TEXT,"
                + "fotoPath TEXT"
                + ")";
        db.execSQL(CREATE_CATEGORIAS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Add the new BLOB column to the existing table
            db.execSQL("ALTER TABLE " + TABLE_TICKETS + " ADD COLUMN " + COLUMN_IMAGEN_BLOB + " BLOB");
        }
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle downgrade by recreating the database
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public long insertTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_FOTO, ticket.getFotoPath());
        
        // Add BLOB data if available
        byte[] imageData = ticket.getImagenBlob();
        if (imageData != null) {
            values.put(COLUMN_IMAGEN_BLOB, imageData);
        }
        
        values.put(COLUMN_CATEGORIA, ticket.getCategoria().getId());
        values.put(COLUMN_PRECIO, ticket.getPrecio());
        values.put(COLUMN_FECHA, ticket.getFechaAlta().getTime());
        values.put(COLUMN_DESC_CORTA, ticket.getDescripcionCorta());
        values.put(COLUMN_DESC_LARGA, ticket.getDescripcionLarga());
        values.put(COLUMN_LOCALIZACION, ticket.getUbicacionFoto());
        
        long id = db.insert(TABLE_TICKETS, null, values);
        db.close();
        return id;
    }
    
    public int updateTicket(Ticket ticket) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_FOTO, ticket.getFotoPath());
        
        // Add BLOB data if available
        byte[] imageData = ticket.getImagenBlob();
        if (imageData != null) {
            values.put(COLUMN_IMAGEN_BLOB, imageData);
        }
        
        values.put(COLUMN_CATEGORIA, ticket.getCategoria().getId());
        values.put(COLUMN_PRECIO, ticket.getPrecio());
        values.put(COLUMN_FECHA, ticket.getFechaAlta().getTime());
        values.put(COLUMN_DESC_CORTA, ticket.getDescripcionCorta());
        values.put(COLUMN_DESC_LARGA, ticket.getDescripcionLarga());
        values.put(COLUMN_LOCALIZACION, ticket.getUbicacionFoto());
        
        int result = db.update(TABLE_TICKETS, values, COLUMN_ID + " = ?", 
                new String[]{String.valueOf(ticket.getId())});
        db.close();
        return result;
    }
    
    public void deleteTicket(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TICKETS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteTicketsByCategoria(int categoriaId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TICKETS, COLUMN_CATEGORIA + " = ?", new String[]{String.valueOf(categoriaId)});
        db.close();
    }
    
    public ArrayList<Ticket> getAllTickets() {
        ArrayList<Ticket> ticketList = new ArrayList<>();
        
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TICKETS, null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Ticket ticket = new Ticket();
                ticket.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                ticket.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FOTO)));
                
                // Get BLOB data if available
                int blobColumnIndex = cursor.getColumnIndex(COLUMN_IMAGEN_BLOB);
                if (blobColumnIndex != -1 && !cursor.isNull(blobColumnIndex)) {
                    ticket.setImagenBlob(cursor.getBlob(blobColumnIndex));
                }
                
                int categoriaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CATEGORIA));
                for (Categoria cat : Categoria.arrayCat) {
                    if (cat.getId() == categoriaId) {
                        ticket.setCategoria(cat);
                        break;
                    }
                }
                
                ticket.setPrecio(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRECIO)));
                ticket.setFechaAlta(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_FECHA))));
                ticket.setDescripcionCorta(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC_CORTA)));
                ticket.setDescripcionLarga(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESC_LARGA)));
                ticket.setUbicacionFoto(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCALIZACION)));
                
                ticketList.add(ticket);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        
        return ticketList;
    }

    public ArrayList<Categoria> getAllCategorias() {
        ArrayList<Categoria> categorias = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(
            "categorias",
            null,
            null,
            null,
            null,
            null,
            "titulo ASC"
        );
        
        if (cursor.moveToFirst()) {
            do {
                Categoria categoria = new Categoria();
                categoria.setId(cursor.getInt(cursor.getColumnIndex("id")));
                categoria.setTitulo(cursor.getString(cursor.getColumnIndex("titulo")));
                categoria.setDescorta(cursor.getString(cursor.getColumnIndex("descorta")));
                categoria.setDeslarga(cursor.getString(cursor.getColumnIndex("deslarga")));
                categoria.setDetalles(cursor.getString(cursor.getColumnIndex("detalles")));
                categoria.setFotoPath(cursor.getString(cursor.getColumnIndex("fotoPath")));
                
                categorias.add(categoria);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        return categorias;
    }
}