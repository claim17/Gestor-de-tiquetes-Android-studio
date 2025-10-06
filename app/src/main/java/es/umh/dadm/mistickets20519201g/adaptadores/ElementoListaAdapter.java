package es.umh.dadm.mistickets20519201g.adaptadores;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import es.umh.dadm.mistickets20519201g.R;
import es.umh.dadm.mistickets20519201g.objetos.Categoria;

public class ElementoListaAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Categoria> arrayCat;

    //Constructor par asignar el Contexto y el origen de datos
    public ElementoListaAdapter(Context context, ArrayList<Categoria> array)
    {
        super();
        mContext=context;   //Asignamos el contexto para poder acceder a la actividad
        arrayCat=array;   //Establecemos el origen de datos
    }

    public int getCount()
    {
        // devuelve el número de elementos del origen de datos para saber cuantas veces llama a getView
        return arrayCat.size();
    }

    // Este elemento se llama por cada elemento de datasource
    public View getView(int position, View view, ViewGroup parent)
    {

        // Cargamos el layout
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.fragment_categorias, null);

//        // Obtenemos la referencia d elos elementos del layout
//        TextView textViewId=(TextView)view.findViewById(R.id.textViewIDOculto);
//        TextView textViewTitulo=(TextView)view.findViewById(R.id.textViewTitulo);

        // Asignamos los valores correspondientes
//        textViewId.setText(Integer.toString(arrayPelis.get(position).getId()));
//        textViewTitulo.setText(arrayPelis.get(position).getTitulo());

        return view;
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

}
