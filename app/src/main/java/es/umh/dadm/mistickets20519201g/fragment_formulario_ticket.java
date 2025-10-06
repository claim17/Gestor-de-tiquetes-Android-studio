package es.umh.dadm.mistickets20519201g;

 import android.widget.*;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.os.Environment;
import androidx.core.content.FileProvider;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.List;
import android.util.Log;

import es.umh.dadm.mistickets20519201g.objetos.Categoria;
import es.umh.dadm.mistickets20519201g.objetos.Ticket;

public class fragment_formulario_ticket extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri imageUri;

    private ImageView imageViewTicket;
    private EditText editTextPrecio, editTextDescCorta, editTextDescLarga;
    private Spinner spinnerCategoria;
    private TextView textViewUbicacion;
    private Button btnSelectImage, btnGuardarTicket;
    private String currentPhotoPath;
    private int editPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_formulario_ticket, container, false);

        // Inicializar vistas
        imageViewTicket = vista.findViewById(R.id.imageViewTicket);
        editTextPrecio = vista.findViewById(R.id.editTextPrecio);
        editTextDescCorta = vista.findViewById(R.id.editTextDescCorta);
        editTextDescLarga = vista.findViewById(R.id.editTextDescLarga);
        spinnerCategoria = vista.findViewById(R.id.spinnerCategoria);
        textViewUbicacion = vista.findViewById(R.id.textViewUbicacion);
        btnSelectImage = vista.findViewById(R.id.btnSelectImage);
        btnGuardarTicket = vista.findViewById(R.id.btnGuardarTicket);

        // Configurar Spinner de categorías
        ArrayAdapter<Categoria> categoriaAdapter = new ArrayAdapter<>(
            getContext(),
            android.R.layout.simple_spinner_item,
            Categoria.arrayCat
        );
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        // Configurar botón de selección de imagen
        btnSelectImage.setOnClickListener(v -> seleccionarImagen());

        // Configurar botón de guardar
        btnGuardarTicket.setOnClickListener(v -> guardarTicket());

        // Cargar datos si estamos en modo edición
        if (getArguments() != null) {
            editPosition = getArguments().getInt("position", -1);
            if (editPosition != -1) {
                cargarDatosTicket();
            }
        }

        return vista;
    }

    private void seleccionarImagen() {
        // Crear un diálogo para elegir entre cámara y galería
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Seleccionar imagen");
        builder.setItems(new String[]{"Cámara", "Galería"}, (dialog, which) -> {
            if (which == 0) {
                // Intentar abrir la cámara del sistema
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                
                // Lista de paquetes comunes de aplicaciones de cámara
                String[] cameraPackages = {
                    "com.android.camera",           // Cámara de Android estándar
                    "com.google.android.GoogleCamera", // Google Camera
                    "com.sec.android.app.camera",   // Samsung
                    "com.huawei.camera",           // Huawei
                    "com.motorola.camera",         // Motorola
                    "com.lge.camera",              // LG
                    "com.mediatek.camera",         // MediaTek
                    "com.oppo.camera",             // OPPO
                    "com.vivo.camera",             // Vivo
                    "com.miui.camera"              // Xiaomi
                };

                // Intentar encontrar una aplicación de cámara instalada
                PackageManager packageManager = getActivity().getPackageManager();
                Intent cameraIntent = null;

                for (String packageName : cameraPackages) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.setPackage(packageName);
                    if (intent.resolveActivity(packageManager) != null) {
                        cameraIntent = intent;
                        break;
                    }
                }

                if (cameraIntent == null) {
                    // Si no se encuentra una cámara específica, usar la acción genérica
                    cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                }

                if (cameraIntent.resolveActivity(packageManager) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Toast.makeText(getContext(), "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(getContext(),
                                "es.umh.dadm.mistickets20519201g.fileprovider",
                                photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                } else {
                    // Si no se encuentra ninguna aplicación de cámara, mostrar un diálogo con opciones
                    new AlertDialog.Builder(getContext())
                        .setTitle("No se encontró una aplicación de cámara")
                        .setMessage("¿Desea buscar una aplicación de cámara en la Play Store o usar la galería?")
                        .setPositiveButton("Buscar cámara", (dialog1, which1) -> {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=camera")));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=camera")));
                            }
                        })
                        .setNegativeButton("Usar galería", (dialog1, which1) -> {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent, PICK_IMAGE_REQUEST);
                        })
                        .setNeutralButton("Cancelar", null)
                        .show();
                }
            } else {
                // Abrir galería
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            }
        });
        builder.show();
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri selectedImage = data.getData();
                try {
                    // Convertir la imagen a BLOB
                    byte[] imageData = Ticket.uriToByteArray(getContext(), selectedImage);
                    if (imageData != null) {
                        currentPhotoPath = selectedImage.toString();
                        imageViewTicket.setImageURI(selectedImage);
                        textViewUbicacion.setText("Ubicación: " + currentPhotoPath);
                    } else {
                        Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("FormularioTicket", "Error al procesar la imagen: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == CAMERA_REQUEST) {
                if (imageUri != null) {
                    try {
                        // Convertir la imagen de la cámara a BLOB
                        byte[] imageData = Ticket.uriToByteArray(getContext(), imageUri);
                        if (imageData != null) {
                            currentPhotoPath = imageUri.toString();
                            imageViewTicket.setImageURI(imageUri);
                            textViewUbicacion.setText("Ubicación: " + currentPhotoPath);
                        } else {
                            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("FormularioTicket", "Error al procesar la imagen de la cámara: " + e.getMessage());
                        Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Error al capturar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Solo necesitamos modificar el método guardarTicket
    private void guardarTicket() {
        if (!validarFormulario()) {
            return;
        }

        try {
            Ticket ticket;
            if (editPosition != -1) {
                ticket = Ticket.arrayTickets.get(editPosition);
            } else {
                ticket = new Ticket();
            }

            // Verificar que la categoría no sea null antes de asignarla
            Categoria categoriaSeleccionada = (Categoria) spinnerCategoria.getSelectedItem();
            if (categoriaSeleccionada != null) {
                ticket.setCategoria(categoriaSeleccionada);
            } else {
                Toast.makeText(getContext(), "Error: No se ha seleccionado una categoría", Toast.LENGTH_SHORT).show();
                return;
            }

            // Convertir la imagen a BLOB si hay una ruta de foto
            if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(currentPhotoPath);
                    byte[] imageData = Ticket.uriToByteArray(getContext(), imageUri);
                    if (imageData != null) {
                        ticket.setImagenBlob(imageData);
                        ticket.setFotoPath(currentPhotoPath);
                        ticket.setUbicacionFoto(currentPhotoPath);
                    } else {
                        Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (Exception e) {
                    Log.e("FormularioTicket", "Error al procesar la imagen: " + e.getMessage());
                    Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            
            ticket.setPrecio(Double.parseDouble(editTextPrecio.getText().toString()));
            ticket.setDescripcionCorta(editTextDescCorta.getText().toString());
            ticket.setDescripcionLarga(editTextDescLarga.getText().toString());
        
            if (editPosition == -1) {
                // Nuevo ticket - Siempre pasar el contexto
                Ticket.addTicket(ticket, getContext());
                Toast.makeText(getContext(), "Ticket guardado con éxito", Toast.LENGTH_SHORT).show();
            } else {
                // Actualizar ticket existente
                if (getArguments() != null && getArguments().containsKey("id")) {
                    ticket.setId(getArguments().getInt("id"));
                }
                
                Ticket.updateTicket(ticket, getContext());
                Toast.makeText(getContext(), "Ticket actualizado con éxito", Toast.LENGTH_SHORT).show();
            }
        
            // Volver al fragmento anterior
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        } catch (Exception e) {
            Log.e("FormularioTicket", "Error al guardar el ticket: " + e.getMessage());
            Toast.makeText(getContext(), "Error al guardar el ticket: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean validarFormulario() {
        if (currentPhotoPath == null) {
            Toast.makeText(getContext(), "Por favor, seleccione una imagen", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextPrecio.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Por favor, introduzca un precio", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (editTextDescCorta.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Por favor, introduzca una descripción corta", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Añadir validación para la categoría
        if (spinnerCategoria.getSelectedItem() == null) {
            Toast.makeText(getContext(), "Por favor, seleccione una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void cargarDatosTicket() {
        if (editPosition != -1 && getArguments() != null) {
            Ticket ticket = Ticket.arrayTickets.get(editPosition);
            
            // Cargar datos del ticket
            editTextPrecio.setText(String.valueOf(ticket.getPrecio()));
            editTextDescCorta.setText(ticket.getDescripcionCorta());
            editTextDescLarga.setText(ticket.getDescripcionLarga());
            
            // Seleccionar la categoría en el spinner
            if (ticket.getCategoria() != null) {
                for (int i = 0; i < Categoria.arrayCat.size(); i++) {
                    if (Categoria.arrayCat.get(i).getId() == ticket.getCategoria().getId()) {
                        spinnerCategoria.setSelection(i);
                        break;
                    }
                }
            }
            
            // Primero intentar cargar desde BLOB
            byte[] imagenBlob = ticket.getImagenBlob();
            if (imagenBlob != null && imagenBlob.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBlob, 0, imagenBlob.length);
                if (bitmap != null) {
                    imageViewTicket.setImageBitmap(bitmap);
                    textViewUbicacion.setText("Imagen almacenada en la base de datos");
                }
            }
            // Si no hay BLOB, intentar cargar desde URI como antes
            else if (ticket.getFotoPath() != null && !ticket.getFotoPath().isEmpty()) {
                currentPhotoPath = ticket.getFotoPath();
                try {
                    imageViewTicket.setImageURI(Uri.parse(currentPhotoPath));
                    textViewUbicacion.setText("Ubicación: " + currentPhotoPath);
                } catch (Exception e) {
                    // Si hay error al cargar la imagen, mostrar imagen por defecto
                    imageViewTicket.setImageResource(android.R.drawable.ic_menu_gallery);
                    textViewUbicacion.setText("Ubicación: No disponible (Permiso denegado)");
                    // Mantener la ruta para no perderla al guardar
                }
            }
        }
    }
}