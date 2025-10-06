package es.umh.dadm.mistickets20519201g.adaptadores;

// Actualizar el paquete y añadir imports necesarios
import es.umh.dadm.mistickets20519201g.R;
import es.umh.dadm.mistickets20519201g.fragment_formulario_categoria;
// ... resto de imports existentes ...



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import es.umh.dadm.mistickets20519201g.objetos.Categoria;


import android.app.AlertDialog;
import android.content.Context;
import android.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import android.net.Uri;
import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import es.umh.dadm.mistickets20519201g.persistencia.CategoriaPersistencia;
import es.umh.dadm.mistickets20519201g.objetos.Ticket;

public class CatAdapter extends RecyclerView.Adapter<CatAdapter.CategoriaViewHolder> {
    
    // Atributos
    private ArrayList<Categoria> categorias;
    // Nuevo atributo para el FragmentManager
    private FragmentManager fragmentManager;

    // Constructor
    public CatAdapter(ArrayList<Categoria> categorias, FragmentManager fragmentManager) {
        this.categorias = categorias;
        this.fragmentManager = fragmentManager;
    }

    
    @Override
    //metodo para enlazar los datos con el viewholder
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);
        holder.tvTitulo.setText(categoria.getTitulo());
        holder.tvDescripcionCorta.setText(categoria.getDescorta());

        // Configurar imagen si existe
        if (categoria.getFotoPath() != null && !categoria.getFotoPath().isEmpty()) {
            try {
                // Cargar la imagen desde el almacenamiento interno
                Uri imageUri = CategoriaPersistencia.cargarImagen(holder.itemView.getContext(), categoria.getFotoPath());
                if (imageUri != null) {
                    holder.ivFoto.setImageURI(imageUri);
                } else {
                    holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            } catch (Exception e) {
                Log.e("CatAdapter", "Error al cargar la imagen: " + e.getMessage());
                holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Configurar el listener para clic simple (editar)
        holder.itemView.setOnClickListener(view -> editarCategoria(position));

        // Configurar el listener para clic largo (eliminar)
        holder.itemView.setOnLongClickListener(view -> {
            eliminarCategoria(position, view.getContext());
            return true;
        });
    }

    //metodo para editar una categoria
    private void editarCategoria(int position) {
        Categoria categoria = categorias.get(position);
        fragment_formulario_categoria fragment = new fragment_formulario_categoria();
        
        Bundle args = new Bundle();
        args.putString("titulo", categoria.getTitulo());
        args.putString("descorta", categoria.getDescorta());
        args.putString("deslarga", categoria.getDeslarga());
        args.putString("detalles", categoria.getDetalles());
        args.putString("fotoPath", categoria.getFotoPath());
        args.putInt("position", position);
        fragment.setArguments(args);

        fragmentManager.beginTransaction().replace(R.id.nav_host_fragment, fragment).addToBackStack(null).commit();
    }


    //metodo para eliminar una categoria
    private void eliminarCategoria(int position, Context context) {
        Categoria categoria = categorias.get(position);
        new AlertDialog.Builder(context)
                .setTitle("Eliminar categoría")
                .setMessage("¿Estás seguro de que quieres eliminar esta categoría? Se eliminarán todos los tickets asociados.")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Primero eliminar los tickets asociados
                    Ticket.deleteTicketsByCategoria(categoria.getId(), context);
                    
                    // Luego eliminar la categoría
                    categorias.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, categorias.size());
                    
                    // Guardar los cambios
                    CategoriaPersistencia.guardarCategorias(context, categorias);
                })
                .setNegativeButton("No", null)
                .show();
    }

    
    @NonNull
    @Override //metodo para crear el viewholder
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_categoria, parent, false);

        return new CategoriaViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    //clase para el viewholder
    public static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvTitulo;
        TextView tvDescripcionCorta;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFotoCategoria);
            tvTitulo = itemView.findViewById(R.id.tvTituloCategoria);
            tvDescripcionCorta = itemView.findViewById(R.id.tvDescripcionCorta);
        }
    }

    //metodo para actualizar las categorias
    public void actualizarCategorias(ArrayList<Categoria> nuevasCategorias) {
        this.categorias = nuevasCategorias;
        notifyDataSetChanged();
    }
}