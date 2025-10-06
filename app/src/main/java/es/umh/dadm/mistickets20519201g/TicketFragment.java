package es.umh.dadm.mistickets20519201g;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.umh.dadm.mistickets20519201g.adaptadores.TicketAdapter;
import es.umh.dadm.mistickets20519201g.objetos.Ticket;
import es.umh.dadm.mistickets20519201g.objetos.Categoria; // Añadir esta importación

public class TicketFragment extends Fragment {
    private TicketAdapter adapter;
    private TextView tvNoTickets;
    private RecyclerView recyclerView;

    public TicketFragment() {
        // Required empty public constructor
    }

    // Solo necesitamos modificar el método onCreateView y onResume
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View vista = inflater.inflate(R.layout.fragment_tickets, container, false);
    
        // Inicializar vistas
        tvNoTickets = vista.findViewById(R.id.tvNoTickets);
        recyclerView = vista.findViewById(R.id.recyclerViewTickets);
        FloatingActionButton fab = vista.findViewById(R.id.fabAddTicket);
    
        // Primero cargar las categorías desde la base de datos
        // Esto asegura que las categorías estén disponibles antes de cargar los tickets
        Categoria.loadCategorias(getContext());
        
        // Luego cargar tickets desde la base de datos
        Ticket.loadTickets(getContext());
    
        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TicketAdapter(Ticket.arrayTickets, getParentFragmentManager());
        recyclerView.setAdapter(adapter);
    
        // Configurar FAB
        fab.setOnClickListener(v -> {
            fragment_formulario_ticket formularioFragment = new fragment_formulario_ticket();
            getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, formularioFragment)
                .addToBackStack(null)
                .commit();
        });
    
        // Actualizar visibilidad inicial
        actualizarVisibilidad();
    
        return vista;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar categorías primero
        Categoria.loadCategorias(getContext());
        // Luego recargar tickets
        Ticket.loadTickets(getContext());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            actualizarVisibilidad();
        }
    }

    private void actualizarVisibilidad() {
        if (Ticket.arrayTickets.isEmpty()) {
            tvNoTickets.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvNoTickets.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }
}