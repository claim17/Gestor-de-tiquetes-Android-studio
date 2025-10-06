package es.umh.dadm.mistickets20519201g.adaptadores;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.widget.PopupMenu;
import androidx.fragment.app.FragmentManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.net.Uri;
import android.content.ContentResolver;
import android.os.ParcelFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayInputStream;

import es.umh.dadm.mistickets20519201g.R;
import es.umh.dadm.mistickets20519201g.objetos.Ticket;
import es.umh.dadm.mistickets20519201g.fragment_formulario_ticket;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {
    private ArrayList<Ticket> tickets;
    private FragmentManager fragmentManager;
    private SimpleDateFormat dateFormat;

    public TicketAdapter(ArrayList<Ticket> tickets, FragmentManager fragmentManager) {
        this.tickets = tickets;
        this.fragmentManager = fragmentManager;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    
    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.tvDescripcion.setText(ticket.getDescripcionCorta());
        holder.tvPrecio.setText(String.format("%.2f €", ticket.getPrecio()));
        
        // Verificar que la fecha no sea null
        if (ticket.getFechaAlta() != null) {
            holder.tvFecha.setText(dateFormat.format(ticket.getFechaAlta()));
        } else {
            holder.tvFecha.setText("Fecha no disponible");
        }
        
        // Verificar que la categoría no sea null antes de acceder a sus métodos
        if (ticket.getCategoria() != null) {
            holder.tvCategoria.setText(ticket.getCategoria().getTitulo());
        } else {
            holder.tvCategoria.setText("Sin categoría");
        }
    
        // Primero intentar cargar desde BLOB
        byte[] imagenBlob = ticket.getImagenBlob();
        if (imagenBlob != null && imagenBlob.length > 0) {
            // Convertir BLOB a Bitmap
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBlob, 0, imagenBlob.length);
            if (bitmap != null) {
                holder.ivFoto.setImageBitmap(bitmap);
            } else {
                holder.ivFoto.setImageResource(R.drawable.ic_photo_placeholder);
            }
        }
        // Si no hay BLOB, intentar cargar desde URI como antes (para compatibilidad)
        else if (ticket.getFotoPath() != null && !ticket.getFotoPath().isEmpty()) {
            try {
                // Intentar cargar la imagen desde URI
                Uri imageUri = Uri.parse(ticket.getFotoPath());
                
                // Verificar si podemos acceder al contenido
                boolean canRead = false;
                try {
                    ContentResolver resolver = holder.itemView.getContext().getContentResolver();
                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(imageUri, "r");
                    if (pfd != null) {
                        pfd.close();
                        canRead = true;
                    }
                } catch (Exception e) {
                    canRead = false;
                }
                
                if (canRead) {
                    holder.ivFoto.setImageURI(imageUri);
                } else {
                    // Si no podemos acceder, usar imagen por defecto
                    holder.ivFoto.setImageResource(R.drawable.ic_photo_placeholder);
                }
            } catch (Exception e) {
                // Si hay error al cargar la imagen, mostrar imagen por defecto
                holder.ivFoto.setImageResource(R.drawable.ic_photo_placeholder);
            }
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_photo_placeholder);
        }
    
        // Configurar el listener para el clic corto (editar)
        holder.itemView.setOnClickListener(view -> editarTicket(position));
        
        // Configurar el listener para el clic largo (eliminar)
        holder.itemView.setOnLongClickListener(view -> {
            eliminarTicket(position, view.getContext());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    private void mostrarMenuOpciones(View view, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.menu_ticket_opciones);
        
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_editar) {
                editarTicket(position);
                return true;
            } else if (item.getItemId() == R.id.menu_eliminar) {
                eliminarTicket(position, view.getContext());
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    private void editarTicket(int position) {
        Ticket ticket = tickets.get(position);
        fragment_formulario_ticket fragment = new fragment_formulario_ticket();
        
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putInt("id", ticket.getId()); // Asegurarse de pasar el ID
        args.putString("descripcionCorta", ticket.getDescripcionCorta());
        args.putString("descripcionLarga", ticket.getDescripcionLarga());
        args.putDouble("precio", ticket.getPrecio());
        args.putString("fotoPath", ticket.getFotoPath());
        args.putString("ubicacion", ticket.getUbicacionFoto());
        // No podemos pasar el BLOB directamente en el Bundle, se recuperará de arrayTickets
        fragment.setArguments(args);
    
        fragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void eliminarTicket(int position, Context context) {
        Ticket ticket = tickets.get(position);
        new AlertDialog.Builder(context)
                .setTitle("Eliminar ticket")
                .setMessage("¿Estás seguro de que quieres eliminar este ticket?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Eliminar de la base de datos
                    Ticket.deleteTicket(ticket.getId(), context);
                    
                    // Actualizar la lista de tickets desde la base de datos
                    Ticket.loadTickets(context);
                    
                    // Eliminar de la lista local y notificar al adaptador
                    tickets.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, tickets.size());
                })
                .setNegativeButton("No", null)
                .show();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvDescripcion;
        TextView tvPrecio;
        TextView tvFecha;
        TextView tvCategoria;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFotoTicket);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTicket);
            tvPrecio = itemView.findViewById(R.id.tvPrecioTicket);
            tvFecha = itemView.findViewById(R.id.tvFechaTicket);
            tvCategoria = itemView.findViewById(R.id.tvCategoriaTicket);
        }
    }
}