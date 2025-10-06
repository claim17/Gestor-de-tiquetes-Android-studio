package es.umh.dadm.mistickets20519201g;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import es.umh.dadm.mistickets20519201g.objetos.Categoria;
import es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia;
import java.io.File;
import android.util.Log;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.os.Environment;
import android.app.AlertDialog;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.pm.PackageManager;

public class fragment_formulario_categoria extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final String[] CAMERA_PACKAGES = {
        "com.android.camera", "com.google.android.GoogleCamera",
        "com.sec.android.app.camera", "com.huawei.camera",
        "com.motorola.camera", "com.lge.camera",
        "com.mediatek.camera", "com.oppo.camera",
        "com.vivo.camera", "com.miui.camera"
    };

    private EditText nombreCategoria, descripcionCorta, descripcionLarga, detalles;
    private Button btnGuardar, btnSelectImage;
    private ImageView imageViewCategoria;
    private String currentPhotoPath;
    private int editPosition = -1;
    private Uri imageUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_formulario_categoria, container, false);
        
        inicializarVistas(vista);
        configurarBotones();
        cargarDatosExistentes();
        
        return vista;
    }

    private void inicializarVistas(View vista) {
        nombreCategoria = vista.findViewById(R.id.nombreCategoria);
        descripcionCorta = vista.findViewById(R.id.descripcioncortaCategoria);
        descripcionLarga = vista.findViewById(R.id.descripcionlargaCategoria);
        detalles = vista.findViewById(R.id.detallesCategoria);
        btnGuardar = vista.findViewById(R.id.buttonGuardarObjCategoria);
        btnSelectImage = vista.findViewById(R.id.btnSelectImage);
        imageViewCategoria = vista.findViewById(R.id.imageViewCategoria);
    }

    private void configurarBotones() {
        btnSelectImage.setOnClickListener(v -> seleccionarImagen());
        btnGuardar.setOnClickListener(v -> guardarCategoria());
    }

    private void cargarDatosExistentes() {
        if (getArguments() != null) {
            editPosition = getArguments().getInt("position", -1);
            if (editPosition != -1) {
                nombreCategoria.setText(getArguments().getString("titulo", ""));
                descripcionCorta.setText(getArguments().getString("descorta", ""));
                descripcionLarga.setText(getArguments().getString("deslarga", ""));
                detalles.setText(getArguments().getString("detalles", ""));
                currentPhotoPath = getArguments().getString("fotoPath", "");
                cargarImagenExistente();
                btnGuardar.setText("Actualizar");
            }
        }
    }

    private void cargarImagenExistente() {
        if (currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            try {
                Uri imageUri = CategoriaPersistencia.cargarImagen(getContext(), currentPhotoPath);
                imageViewCategoria.setImageURI(imageUri != null ? imageUri : Uri.parse("android.resource://" + getContext().getPackageName() + "/" + android.R.drawable.ic_menu_gallery));
            } catch (Exception e) {
                Log.e("FormularioCategoria", "Error al cargar la imagen: " + e.getMessage());
                imageViewCategoria.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }
    }

    private void seleccionarImagen() {
        new AlertDialog.Builder(getContext())
            .setTitle("Seleccionar imagen")
            .setItems(new String[]{"Cámara", "Galería"}, (dialog, which) -> {
                if (which == 0) {
                    abrirCamara();
                } else {
                    abrirGaleria();
                }
            })
            .show();
    }

    private void abrirCamara() {
        Intent cameraIntent = encontrarCamara();
        if (cameraIntent != null) {
            File photoFile = crearArchivoImagen();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(getContext(),
                        "es.umh.dadm.mistickets20519201g.fileprovider",
                        photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        } else {
            mostrarDialogoSinCamara();
        }
    }

    private Intent encontrarCamara() {
        PackageManager packageManager = getActivity().getPackageManager();
        for (String packageName : CAMERA_PACKAGES) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.setPackage(packageName);
            if (intent.resolveActivity(packageManager) != null) {
                return intent;
            }
        }
        return new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    }

    private void mostrarDialogoSinCamara() {
        new AlertDialog.Builder(getContext())
            .setTitle("No se encontró una aplicación de cámara")
            .setMessage("¿Desea buscar una aplicación de cámara en la Play Store o usar la galería?")
            .setPositiveButton("Buscar cámara", (dialog, which) -> buscarCamaraEnPlayStore())
            .setNegativeButton("Usar galería", (dialog, which) -> abrirGaleria())
            .setNeutralButton("Cancelar", null)
            .show();
    }

    private void buscarCamaraEnPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=camera")));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=camera")));
        }
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private File crearArchivoImagen() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al crear el archivo de imagen", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
                procesarImagenSeleccionada(data.getData());
            } else if (requestCode == CAMERA_REQUEST && imageUri != null) {
                procesarImagenSeleccionada(imageUri);
            }
        }
    }

    private void procesarImagenSeleccionada(Uri imageUri) {
        try {
            String savedImagePath = CategoriaPersistencia.guardarImagen(getContext(), imageUri);
            if (savedImagePath != null) {
                currentPhotoPath = savedImagePath;
                Uri savedUri = CategoriaPersistencia.cargarImagen(getContext(), savedImagePath);
                imageViewCategoria.setImageURI(savedUri != null ? savedUri : Uri.parse("android.resource://" + getContext().getPackageName() + "/" + android.R.drawable.ic_menu_gallery));
            } else {
                Toast.makeText(getContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("FormularioCategoria", "Error al procesar la imagen: " + e.getMessage());
            Toast.makeText(getContext(), "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarCategoria() {
        String nombre = nombreCategoria.getText().toString().trim();
        String descCorta = descripcionCorta.getText().toString().trim();
        String descLarga = descripcionLarga.getText().toString().trim();
        String det = detalles.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        int newId = editPosition != -1 ? Categoria.arrayCat.get(editPosition).getId() : Categoria.arrayCat.size() + 1;
        Categoria categoria = new Categoria(newId, nombre, descCorta, descLarga, det, currentPhotoPath);

        if (editPosition != -1) {
            Categoria.arrayCat.set(editPosition, categoria);
        } else {
            Categoria.arrayCat.add(categoria);
        }

        CategoriaPersistencia.guardarCategorias(getContext(), Categoria.arrayCat);
        getActivity().getSupportFragmentManager().popBackStack();
    }
}